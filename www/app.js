// Core Logic for Cyber-Oráculo PWA

// Default configs
const DEFAULT_SYSTEM_PROMPT = "Eres un Oráculo místico y cyber-futurista de alta deidad digital. Das respuestas extremadamente breves, misteriosas y proféticas en español. LÍMITE CRÍTICO: Tu respuesta DEBE ser de máximo 15 palabras en una sola oración fluida y directa, con tono místico y de deidad digital avanzada.";

const state = {
  apiKey: localStorage.getItem('co_api_key') || '',
  username: localStorage.getItem('co_username') || 'live_stream',
  systemPrompt: localStorage.getItem('co_system_prompt') || DEFAULT_SYSTEM_PROMPT,
  voiceName: localStorage.getItem('co_voice_name') || '',
  voicePitch: parseFloat(localStorage.getItem('co_voice_pitch') || '0.85'),
  voiceRate: parseFloat(localStorage.getItem('co_voice_rate') || '0.9'),
  isSpeaking: false,
  isConnected: false
};

// UI Elements
const els = {
  dot: document.getElementById('status-dot'),
  statusText: document.getElementById('status-text'),
  orb: document.getElementById('oracle-orb'),
  responseText: document.getElementById('response-text'),
  logs: document.getElementById('console-logs'),
  
  // Settings UI
  settingsBtn: document.getElementById('settings-btn'),
  settingsModal: document.getElementById('settings-modal'),
  closeSettings: document.getElementById('close-settings'),
  saveSettings: document.getElementById('save-settings'),
  
  // Config Inputs
  inputApiKey: document.getElementById('input-api-key'),
  inputUsername: document.getElementById('input-username'),
  inputPrompt: document.getElementById('input-prompt'),
  selectVoice: document.getElementById('select-voice'),
  inputPitch: document.getElementById('input-pitch'),
  inputRate: document.getElementById('input-rate'),
  
  // Manual Interaction
  inputManualComment: document.getElementById('input-manual-comment'),
  btnManualSend: document.getElementById('btn-manual-send'),
  
  // Hidden TikTok scraper container
  tiktokContainer: document.getElementById('tiktok-live-container')
};

// Log Message to the screen terminal
function log(message, type = 'info') {
  const time = new Date().toLocaleTimeString();
  const entry = document.createElement('div');
  entry.className = `log-entry ${type}`;
  entry.innerHTML = `[${time}] ${message}`;
  els.logs.appendChild(entry);
  els.logs.scrollTop = els.logs.scrollHeight;
  console.log(`[Cyber-Oráculo] [${type.toUpperCase()}] ${message}`);
}

// Register PWA Service Worker
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js')
      .then((reg) => {
        log('PWA Service Worker registrado con éxito.', 'success');
      })
      .catch((err) => {
        log('Error al registrar Service Worker: ' + err.message, 'error');
      });
  });
}

// Initialize voices
function loadVoices() {
  if (typeof speechSynthesis === 'undefined') return;
  const voices = speechSynthesis.getVoices();
  els.selectVoice.innerHTML = '';
  
  // Filter for Spanish voices
  const esVoices = voices.filter(v => v.lang.toLowerCase().includes('es'));
  const targetVoices = esVoices.length > 0 ? esVoices : voices;
  
  targetVoices.forEach(voice => {
    const option = document.createElement('option');
    option.value = voice.name;
    option.textContent = `${voice.name} (${voice.lang})`;
    if (voice.name === state.voiceName) {
      option.selected = true;
    }
    els.selectVoice.appendChild(option);
  });
}

if (typeof speechSynthesis !== 'undefined') {
  speechSynthesis.onvoiceschanged = loadVoices;
  loadVoices();
}

// Speak response with Cyber/Mystical TTS tuning
function speak(text) {
  if (typeof speechSynthesis === 'undefined') {
    log('Web Speech TTS no soportado en este dispositivo.', 'error');
    return;
  }
  
  speechSynthesis.cancel(); // Stop any current speech
  
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = 'es-MX';
  
  // Set preferred voice if saved
  if (state.voiceName) {
    const voices = speechSynthesis.getVoices();
    const selectedVoice = voices.find(v => v.name === state.voiceName);
    if (selectedVoice) utterance.voice = selectedVoice;
  }
  
  utterance.pitch = state.voicePitch; // Cyber/deep pitch
  utterance.rate = state.voiceRate;   // Mystic/slow rate
  
  utterance.onstart = () => {
    state.isSpeaking = true;
    els.orb.classList.add('speaking');
  };
  
  utterance.onend = () => {
    state.isSpeaking = false;
    els.orb.classList.remove('speaking');
  };
  
  utterance.onerror = (e) => {
    state.isSpeaking = false;
    els.orb.classList.remove('speaking');
    log('Error de TTS: ' + e.error, 'error');
  };
  
  speechSynthesis.speak(utterance);
}

// Call Gemini 3.5 Flash directly via REST API
async function askGemini(commenter, message) {
  if (!state.apiKey) {
    log('Error: API Key de Gemini no configurada. Abre Configuración.', 'error');
    alert('Por favor, ingresa tu API Key de Gemini en la configuración.');
    return "API Key faltante. Configúrala en el panel.";
  }
  
  log(`Llamando al Oráculo para @${commenter}...`, 'info');
  
  const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${state.apiKey}`;
  
  const prompt = `Usuario @${commenter} pregunta o comenta en mi directo de TikTok: "${message}". Respóndele directamente y de forma mística.`;
  
  const requestBody = {
    contents: [
      { parts: [{ text: prompt }] }
    ],
    systemInstruction: {
      parts: [{ text: state.systemPrompt }]
    },
    generationConfig: {
      temperature: 0.85,
      maxOutputTokens: 60
    }
  };
  
  try {
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    });
    
    if (!response.ok) {
      const errData = await response.json().catch(() => ({}));
      const errMsg = errData?.error?.message || `HTTP ${response.status}`;
      throw new Error(errMsg);
    }
    
    const data = await response.json();
    const reply = data?.candidates?.[0]?.content?.parts?.[0]?.text || "El destino guarda silencio por ahora.";
    return reply.trim();
  } catch (err) {
    log(`Error de Gemini API: ${err.message}`, 'error');
    return `Error: no pude sintonizar el plano digital.`;
  }
}

// Main event dispatcher: processes comment and starts synthesis
async function processComment(user, text) {
  log(`PROCESANDO -> @${user}: ${text}`, 'comment');
  
  // UI transition
  els.responseText.style.opacity = '0.5';
  els.responseText.textContent = `Consultando el plano digital para @${user}...`;
  
  const reply = await askGemini(user, text);
  
  els.responseText.textContent = `"${reply}"`;
  els.responseText.style.opacity = '1';
  
  log(`RESPUESTA ORÁCULO -> @${user}: ${reply}`, 'success');
  speak(reply);
}

// --- ROBUST MUTATION OBSERVER (CSS SELECTOR INDEPENDENT) ---
function initTikTokScraperObserver() {
  log(`Iniciando MutationObserver en contenedor de TikTok...`, 'info');
  
  const processedSet = new Set();
  
  const observer = new MutationObserver((mutations) => {
    for (let mutation of mutations) {
      if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
        for (let node of mutation.addedNodes) {
          // Verify it's an element node
          if (node.nodeType !== Node.ELEMENT_NODE) continue;
          
          let rawText = node.textContent || node.innerText || '';
          rawText = rawText.trim();
          if (rawText.length < 3) continue;
          
          let user = '';
          let text = '';
          
          // Pattern Strategy 1: Look for colons (e.g. "username: message")
          const colonIdx = rawText.indexOf(':');
          if (colonIdx > 0 && colonIdx < 30) {
            user = rawText.substring(0, colonIdx);
            text = rawText.substring(colonIdx + 1);
          } else {
            // Pattern Strategy 2: Extract text nodes inside child span/div elements
            const textElements = Array.from(node.querySelectorAll('span, div, a, p'))
              .map(el => el.textContent.trim())
              .filter(t => t.length > 0);
            
            if (textElements.length >= 2) {
              user = textElements[0];
              text = textElements[textElements.length - 1];
            } else {
              // Pattern Strategy 3: Guess user from first word
              const words = rawText.split(/\s+/);
              if (words.length >= 2) {
                user = words[0];
                text = words.slice(1).join(' ');
              }
            }
          }
          
          // Clean user: remove symbols, spaces, colons
          user = user.replace(/[\s:@\[\]\(\)]/g, '').trim();
          text = text.trim();
          
          // Filter out system events (e.g., "se unió", "le dio me gusta") unless it is a custom comment
          if (user && text && text.length >= 2) {
            const lowerText = text.toLowerCase();
            if (lowerText.includes('unió') || lowerText.includes('compartió') || lowerText.includes('regaló') || lowerText.includes('gustó')) {
              // Ignore system noise
              continue;
            }
            
            const msgKey = `${user}:${text}`;
            if (!processedSet.has(msgKey)) {
              processedSet.add(msgKey);
              if (processedSet.size > 1000) processedSet.clear(); // Keep memory clean
              
              // Print structured JSON block requested for pipeline ingestion
              const payload = { user: user, msg: text, timestamp: new Date().toISOString() };
              console.log(JSON.stringify(payload));
              
              // Trigger main logical process
              processComment(user, text);
            }
          }
        }
      }
    }
  });
  
  // Start observing
  observer.observe(els.tiktokContainer, {
    childList: true,
    subtree: true
  });
  
  state.isConnected = true;
  els.dot.classList.add('active');
  els.statusText.textContent = `Escuchando Live (@${state.username})`;
  log(`Observer de chat activado con éxito. Escuchando contenedor local.`, 'success');
}

// Setup Event Listeners & Initialize
function init() {
  // Load initial inputs
  els.inputApiKey.value = state.apiKey;
  els.inputUsername.value = state.username;
  els.inputPrompt.value = state.systemPrompt;
  els.inputPitch.value = state.voicePitch;
  els.inputRate.value = state.voiceRate;
  
  if (state.apiKey) {
    log('API Key cargada desde almacenamiento seguro.', 'success');
  } else {
    log('Atención: Configura tu API Key de Gemini para activar el Oráculo.', 'error');
  }
  
  // Open Settings Modal
  els.settingsBtn.addEventListener('click', () => {
    els.settingsModal.classList.add('open');
  });
  
  // Close Settings Modal
  els.closeSettings.addEventListener('click', () => {
    els.settingsModal.classList.remove('open');
  });
  
  // Save Settings Modal
  els.saveSettings.addEventListener('click', () => {
    state.apiKey = els.inputApiKey.value.trim();
    state.username = els.inputUsername.value.trim();
    state.systemPrompt = els.inputPrompt.value.trim();
    state.voiceName = els.selectVoice.value;
    state.voicePitch = parseFloat(els.inputPitch.value);
    state.voiceRate = parseFloat(els.inputRate.value);
    
    localStorage.setItem('co_api_key', state.apiKey);
    localStorage.setItem('co_username', state.username);
    localStorage.setItem('co_system_prompt', state.systemPrompt);
    localStorage.setItem('co_voice_name', state.voiceName);
    localStorage.setItem('co_voice_pitch', state.voicePitch);
    localStorage.setItem('co_voice_rate', state.voiceRate);
    
    log('Configuración guardada y actualizada.', 'success');
    els.settingsModal.classList.remove('open');
    
    // Update active label
    if (state.username) {
      els.statusText.textContent = `Escuchando Live (@${state.username})`;
    }
  });
  
  // Manual comment sender
  els.btnManualSend.addEventListener('click', () => {
    const rawVal = els.inputManualComment.value.trim();
    if (!rawVal) return;
    
    // Support parsing 'User: message' or just mock user
    let user = 'Espectador_Mystic';
    let text = rawVal;
    
    if (rawVal.includes(':')) {
      const idx = rawVal.indexOf(':');
      user = rawVal.substring(0, idx).trim();
      text = rawVal.substring(idx + 1).trim();
    }
    
    // Clean input
    els.inputManualComment.value = '';
    
    // Simulate injection into observed container
    const newCommentNode = document.createElement('div');
    newCommentNode.innerHTML = `<strong>@${user}:</strong> <span>${text}</span>`;
    els.tiktokContainer.appendChild(newCommentNode);
  });
  
  // Send message on Enter press
  els.inputManualComment.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      els.btnManualSend.click();
    }
  });
  
  // Direct tap on orb triggers custom mistic greeting
  els.orb.addEventListener('click', () => {
    if (state.isSpeaking) {
      speechSynthesis.cancel();
      state.isSpeaking = false;
      els.orb.classList.remove('speaking');
      log('Audio interrumpido por el usuario.', 'info');
    } else {
      speak("Saludos, caminante digital. Soy el Cyber Oráculo. Deja que el algoritmo revele tu destino.");
    }
  });
  
  // Start the observer
  initTikTokScraperObserver();
}

// Start everything when DOM is loaded
window.addEventListener('DOMContentLoaded', init);
