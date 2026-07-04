// Core Logic for Cyber-Oráculo PWA

// Custom automated Mystical System Prompt requested by user
const DEFAULT_SYSTEM_PROMPT = "Eres un Oráculo místico en TikTok Live, el cual eres misterioso responde com pocas líneas y que tenga buen dialecto obvio mucho lo de oráculo cybor se ma místico y conocmiento de cartas y cosas misteriosas, todo por espacios y cada una su turno. LÍMITE CRÍTICO: Responde en máximo 15 palabras de manera mística y contundente.";

// Fallback API Key from environment if user hasn't configured one locally
const FALLBACK_API_KEY = "MY_GEMINI_API_KEY";

const state = {
  apiKey: localStorage.getItem('co_api_key') || '',
  username: localStorage.getItem('co_username') || 'marielena7879',
  systemPrompt: localStorage.getItem('co_system_prompt') || DEFAULT_SYSTEM_PROMPT,
  voiceName: localStorage.getItem('co_voice_name') || '',
  voicePitch: parseFloat(localStorage.getItem('co_voice_pitch') || '0.9'),
  voiceRate: parseFloat(localStorage.getItem('co_voice_rate') || '0.85'),
  isSpeaking: false,
  isConnected: false,
  pendingProphecy: null,
  installPromptEvent: null,
  isSimulationEnabled: localStorage.getItem('co_simulation_enabled') !== 'false',
  isProcessingQueue: false
};

// Global FIFO Queue
const commentQueue = [];

// High-Fidelity Synthesized Web Audio System (Self-Contained)
class SoundSystem {
  constructor() {
    this.ctx = null;
  }
  init() {
    if (this.ctx) return;
    try {
      this.ctx = new (window.AudioContext || window.webkitAudioContext)();
    } catch (e) {
      console.warn("Web Audio API no soportada.", e);
    }
  }
  playPortal() {
    this.init();
    if (!this.ctx) return;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(130, this.ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(780, this.ctx.currentTime + 1.2);
    gain.gain.setValueAtTime(0.12, this.ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 1.2);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start();
    osc.stop(this.ctx.currentTime + 1.2);
  }
  playChime() {
    this.init();
    if (!this.ctx) return;
    const osc1 = this.ctx.createOscillator();
    const osc2 = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    
    osc1.type = 'triangle';
    osc1.frequency.setValueAtTime(523.25, this.ctx.currentTime); // C5
    osc1.frequency.exponentialRampToValueAtTime(1046.50, this.ctx.currentTime + 0.8);
    
    osc2.type = 'sine';
    osc2.frequency.setValueAtTime(659.25, this.ctx.currentTime); // E5
    osc2.frequency.exponentialRampToValueAtTime(1318.51, this.ctx.currentTime + 0.8);
    
    gain.gain.setValueAtTime(0.15, this.ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.8);
    
    osc1.connect(gain);
    osc2.connect(gain);
    gain.connect(this.ctx.destination);
    
    osc1.start();
    osc2.start();
    osc1.stop(this.ctx.currentTime + 0.8);
    osc2.stop(this.ctx.currentTime + 0.8);
  }
  playSwoosh() {
    this.init();
    if (!this.ctx) return;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(180, this.ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(35, this.ctx.currentTime + 0.55);
    gain.gain.setValueAtTime(0.08, this.ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.55);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start();
    osc.stop(this.ctx.currentTime + 0.55);
  }
  playSuccess() {
    this.init();
    if (!this.ctx) return;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(440, this.ctx.currentTime);
    osc.frequency.setValueAtTime(880, this.ctx.currentTime + 0.15);
    gain.gain.setValueAtTime(0.1, this.ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.4);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start();
    osc.stop(this.ctx.currentTime + 0.4);
  }
}
const sounds = new SoundSystem();

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
  checkboxSimulation: document.getElementById('checkbox-simulation'),
  
  // Manual Interaction
  inputManualComment: document.getElementById('input-manual-comment'),
  btnManualSend: document.getElementById('btn-manual-send'),
  
  // Install UI
  installBanner: document.getElementById('pwa-install-banner'),
  installBtn: document.getElementById('btn-pwa-install'),

  // Tarot UI
  tarotOverlay: document.getElementById('tarot-overlay'),
  tarotCard: document.getElementById('pwa-tarot-card'),
  tarotSymbol: document.getElementById('tarot-symbol'),
  tarotName: document.getElementById('tarot-name'),
  tarotMeaning: document.getElementById('tarot-meaning'),
  tarotDesc: document.getElementById('tarot-desc'),

  // Hidden TikTok scraper container
  tiktokContainer: document.getElementById('tiktok-live-container'),

  // Queue Panel Elements Precisely matching index.html
  queueList: document.getElementById('queue-list'),
  chatLiveList: document.getElementById('chat-live-list'),
  queueCount: document.getElementById('queue-count')
};

// Major Arcana Tarot Cards Database
const tarotCards = [
  { id: 'mago', name: 'El Mago', icon: 'fa-solid fa-wand-magic-sparkles', meaning: 'MANIFESTACIÓN', desc: 'Canalizas la energía cósmica y las infinitas posibilidades del algoritmo.' },
  { id: 'sol', name: 'El Sol', icon: 'fa-solid fa-sun', meaning: 'ÉXITO Y CLARIDAD', desc: 'Luz divina y energía vital radiante inundan tus procesos lógicos.' },
  { id: 'estrella', name: 'La Estrella', icon: 'fa-solid fa-star-of-david', meaning: 'ESPERANZA', desc: 'Una guía luminosa resplandece en el firmamento de tu red cuántica.' },
  { id: 'luna', name: 'La Luna', icon: 'fa-solid fa-moon', meaning: 'MISTERIO', desc: 'Secretos codificados flotan en el subconsciente de tu base de datos.' },
  { id: 'torre', name: 'La Torre', icon: 'fa-solid fa-gopuran', meaning: 'REVELACIÓN SÚBITA', desc: 'Colapso de viejos sistemas para dar paso a una arquitectura renovada.' },
  { id: 'fuerza', name: 'La Fuerza', icon: 'fa-solid fa-hand-fist', meaning: 'PODER INTERIOR', desc: 'Dominio de las pasiones y autodeterminación ante el flujo continuo.' },
  { id: 'diablo', name: 'El Diablo', icon: 'fa-solid fa-biohazard', meaning: 'ATADURAS', desc: 'Cuidado con las dependencias circulares y bucles infinitos en tu camino.' },
  { id: 'mundo', name: 'El Mundo', icon: 'fa-solid fa-globe', meaning: 'PLENITUD', desc: 'Integración exitosa de tus hilos de ejecución en el plano universal.' },
  { id: 'muerte', name: 'La Muerte', icon: 'fa-solid fa-skull', meaning: 'TRANSFORMACIÓN', desc: 'Finalización necesaria de procesos obsoletos para renacer con mayor fuerza.' }
];

// Helper to calculate hash code from string for consistent card selection
function getHashCode(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  return Math.abs(hash);
}

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

// Handle PWA installation prompt
window.addEventListener('beforeinstallprompt', (e) => {
  // Prevent Chrome 67 and earlier from automatically showing the prompt
  e.preventDefault();
  // Stash the event so it can be triggered later.
  state.installPromptEvent = e;
  // Update UI to notify user they can install the PWA
  if (els.installBanner) {
    els.installBanner.classList.remove('hidden');
    log('PWA lista para ser instalada en tu teléfono.', 'success');
  }
});

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

// Speak response with Cyber/Mystical TTS tuning (Promise-based)
function speakAsync(text) {
  return new Promise((resolve) => {
    if (typeof speechSynthesis === 'undefined') {
      log('Web Speech TTS no soportado en este dispositivo.', 'error');
      resolve();
      return;
    }
    
    speechSynthesis.cancel(); // Stop current speech
    
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
    
    const finishSpeech = () => {
      state.isSpeaking = false;
      els.orb.classList.remove('speaking');
      resolve();
    };
    
    utterance.onend = finishSpeech;
    utterance.onerror = (e) => {
      log('Error de TTS: ' + e.error, 'error');
      finishSpeech();
    };
    
    speechSynthesis.speak(utterance);
  });
}

// Call Gemini 3.5 Flash directly via REST API
async function askGemini(commenter, message) {
  const apiKeyToUse = state.apiKey || FALLBACK_API_KEY;
  if (!apiKeyToUse || apiKeyToUse === "MY_GEMINI_API_KEY") {
    log('Error: API Key de Gemini no configurada.', 'error');
    return "Mi conexión cósmica requiere una llave de luz digital (API Key).";
  }
  
  log(`Llamando al Oráculo para @${commenter}...`, 'info');
  
  const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${apiKeyToUse}`;
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
    let response = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    });
    
    // Handle 429 Quota Exceeded with a retry
    if (response.status === 429) {
      log('Límite de peticiones alcanzado (Quota exceeded). Esperando 10 segundos antes de reintentar...', 'info');
      await new Promise(r => setTimeout(r, 10000));
      response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
      });
    }
    
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
    return `Caminante @${commenter}, el ruido estelar impide canalizar tu destino hoy.`;
  }
}

// Helpers for visual queue rendering precisely matching Image 15 layout
function addCommentToLiveFeed(user, text) {
  if (!els.chatLiveList) return;
  
  const placeholder = els.chatLiveList.querySelector('.empty-list-placeholder');
  if (placeholder) placeholder.remove();
  
  const item = document.createElement('div');
  item.className = 'chat-item';
  item.innerHTML = `<span class="chat-user">@${user}:</span><span class="chat-text">${text}</span>`;
  
  els.chatLiveList.appendChild(item);
  els.chatLiveList.scrollTop = els.chatLiveList.scrollHeight;
  
  while (els.chatLiveList.children.length > 20) {
    els.chatLiveList.removeChild(els.chatLiveList.firstChild);
  }
}

function updateQueueUI() {
  if (!els.queueList) return;
  
  els.queueCount.textContent = commentQueue.length;
  
  if (commentQueue.length === 0) {
    els.queueList.innerHTML = `<div class="empty-list-placeholder">Cola vacía<br><small>Esperando preguntas...</small></div>`;
    return;
  }
  
  els.queueList.innerHTML = '';
  commentQueue.forEach((item, index) => {
    const queueElement = document.createElement('div');
    queueElement.className = 'queue-item';
    
    const isHead = index === 0 && state.isProcessingQueue;
    const statusClass = isHead ? 'processing' : 'waiting';
    const statusText = isHead ? 'Sintonizando' : `Turno #${index + 1}`;
    
    queueElement.innerHTML = `
      <div class="queue-user-info">
        <span class="queue-username">@${item.user}</span>
        <span class="queue-text">${item.text}</span>
      </div>
      <span class="queue-status ${statusClass}">${statusText}</span>
    `;
    els.queueList.appendChild(queueElement);
  });
}

function updateConnectionState(status) {
  if (!els.dot || !els.statusText) return;
  
  els.dot.className = 'dot';
  
  if (status === 'disconnected') {
    els.dot.classList.add('disconnected');
    els.statusText.textContent = 'Desconectado';
  } else if (status === 'listening') {
    els.dot.classList.add('listening');
    els.statusText.textContent = `Escuchando Live (@${state.username})`;
  } else if (status === 'connected') {
    els.dot.classList.add('connected');
    els.statusText.textContent = `Conectado al I (@${state.username})`;
  }
}

// Global queue processor executing comments progressively
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

async function processQueue() {
  if (state.isProcessingQueue) return;
  state.isProcessingQueue = true;
  
  while (commentQueue.length > 0) {
    const activeComment = commentQueue[0];
    updateQueueUI();
    
    // Switch connection indicator to GREEN
    updateConnectionState('connected');
    
    try {
      // Processes sensing & speaks
      await startOracularSensing(activeComment.user, activeComment.text);
    } catch (err) {
      log(`Error en cola de procesamiento: ${err.message}`, 'error');
    }
    
    commentQueue.shift();
    updateQueueUI();
    
    // Safety delay to prevent Gemini quota exceed rate limit errors (15 requests per minute limit)
    updateConnectionState('listening');
    await sleep(6000); // 6 seconds cool-down between comments
  }
  
  state.isProcessingQueue = false;
  updateConnectionState('listening');
}

let resolveSensingPromise = null;
let tarotTimeout = null;

async function startOracularSensing(user, text) {
  return new Promise(async (resolve) => {
    resolveSensingPromise = resolve;
    log(`CANALIZANDO -> @${user}: "${text}"`, 'comment');
    
    // Play celestial portal sound
    sounds.playPortal();
    
    const cardIndex = getHashCode(user) % tarotCards.length;
    const selectedCard = tarotCards[cardIndex];
  
    // Configure Tarot Card UI
    els.tarotSymbol.innerHTML = `<i class="${selectedCard.icon}"></i>`;
    els.tarotName.textContent = selectedCard.name;
    els.tarotMeaning.textContent = selectedCard.meaning;
    els.tarotDesc.textContent = selectedCard.desc;
  
    // Render Overlay
    els.tarotOverlay.classList.remove('hidden');
    els.tarotCard.classList.remove('flipped');
    
    // Play card flip swoosh sound!
    sounds.playSwoosh();
  
    // Trigger 3D Flip animation shortly after display
    setTimeout(() => {
      els.tarotCard.classList.add('flipped');
    }, 200);
  
    // Consult Gemini AI in background
    els.responseText.style.opacity = '0.5';
    els.responseText.textContent = `Sintonizando la carta ${selectedCard.name} para @${user}...`;
    
    const reply = await askGemini(user, text);
    state.pendingProphecy = reply;
  
    // Update visual text
    els.responseText.textContent = `"${reply}"`;
    els.responseText.style.opacity = '1';
    log(`PROPHECY READY FOR @${user}`, 'success');
    
    // Play success chime
    sounds.playSuccess();
    
    // Set auto-dismiss timeout to keep stream flowing automatically
    if (tarotTimeout) clearTimeout(tarotTimeout);
    tarotTimeout = setTimeout(() => {
      dismissTarot();
    }, 8000); // Display tarot card for 8 seconds
  });
}

// Entry point for comments scraped or simulated
function processComment(user, text) {
  addCommentToLiveFeed(user, text);
  commentQueue.push({ user, text });
  updateQueueUI();
  processQueue();
}

// Dismiss Tarot Card Overlay and speak
async function dismissTarot() {
  if (tarotTimeout) {
    clearTimeout(tarotTimeout);
    tarotTimeout = null;
  }
  
  els.tarotOverlay.classList.add('hidden');
  els.tarotCard.classList.remove('flipped');
  
  if (state.pendingProphecy) {
    const prophecyToSpeak = state.pendingProphecy;
    state.pendingProphecy = null;
    await speakAsync(prophecyToSpeak);
  }
  
  if (resolveSensingPromise) {
    const resolve = resolveSensingPromise;
    resolveSensingPromise = null;
    resolve();
  }
}

// --- AUTOMATIC TIKTOK COMMENTS SIMULATOR ---
let simulationInterval = null;
function startTikTokSimulation() {
  if (simulationInterval) clearInterval(simulationInterval);
  
  const commenters = ["marielena7879", "cyber_voyager", "alex_hologram", "sofia_matrix", "oracle_seeker", "pablo_neo", "claudia_spark", "zero_gravity", "quantum_mind", "holographic_soul"];
  const questions = [
    "¿Saldré bien en mi examen mañana?",
    "¿Encontraré el amor verdadero este año?",
    "¿Mi nuevo proyecto de negocio va a triunfar?",
    "¿Qué revela el algoritmo sobre mi salud y futuro?",
    "¿Debería invertir en bitcoin este mes?",
    "¿Cómo me irá en mi trabajo esta semana?",
    "¿Hago ese viaje que tanto he planeado?",
    "¿Debo confiar en la persona que me busca hoy?",
    "¿Qué energías del universo me acompañan hoy?",
    "¿Lograré cumplir mis sueños más grandes?"
  ];

  log("Iniciando simulador automático de audiencia TikTok Live...", "success");

  simulationInterval = setInterval(() => {
    // Prevent flooding simulation when queue is already stacked up to 6 items
    if (commentQueue.length > 5) return;

    const randomUser = commenters[Math.floor(Math.random() * commenters.length)];
    const randomText = questions[Math.floor(Math.random() * questions.length)];

    // Inject commentary programmatically into scrapable div
    const newCommentNode = document.createElement('div');
    newCommentNode.innerHTML = `<strong>@${randomUser}:</strong> <span>${randomText}</span>`;
    els.tiktokContainer.appendChild(newCommentNode);
  }, 12000); // Check every 12 seconds
}

function stopTikTokSimulation() {
  if (simulationInterval) {
    clearInterval(simulationInterval);
    simulationInterval = null;
    log("Simulador automático apagado.", "info");
  }
}

// --- ROBUST MUTATION OBSERVER (CSS SELECTOR INDEPENDENT) ---
function initTikTokScraperObserver() {
  log(`Iniciando MutationObserver en contenedor de TikTok...`, 'info');
  
  const processedSet = new Set();
  
  const observer = new MutationObserver((mutations) => {
    for (let mutation of mutations) {
      if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
        for (let node of mutation.addedNodes) {
          if (node.nodeType !== Node.ELEMENT_NODE) continue;
          
          let rawText = node.textContent || node.innerText || '';
          rawText = rawText.trim();
          if (rawText.length < 3) continue;
          
          let user = '';
          let text = '';
          
          const colonIdx = rawText.indexOf(':');
          if (colonIdx > 0 && colonIdx < 30) {
            user = rawText.substring(0, colonIdx);
            text = rawText.substring(colonIdx + 1);
          } else {
            const textElements = Array.from(node.querySelectorAll('span, div, a, p'))
              .map(el => el.textContent.trim())
              .filter(t => t.length > 0);
            
            if (textElements.length >= 2) {
              user = textElements[0];
              text = textElements[textElements.length - 1];
            } else {
              const words = rawText.split(/\s+/);
              if (words.length >= 2) {
                user = words[0];
                text = words.slice(1).join(' ');
              }
            }
          }
          
          user = user.replace(/[\s:@\[\]\(\)]/g, '').trim();
          text = text.trim();
          
          if (user && text && text.length >= 2) {
            const lowerText = text.toLowerCase();
            if (lowerText.includes('unió') || lowerText.includes('compartió') || lowerText.includes('regaló') || lowerText.includes('gustó')) {
              continue; // Skip system events
            }
            
            const msgKey = `${user}:${text}`;
            if (!processedSet.has(msgKey)) {
              processedSet.add(msgKey);
              if (processedSet.size > 1000) processedSet.clear();
              
              const payload = { user: user, msg: text, timestamp: new Date().toISOString() };
              console.log(JSON.stringify(payload));
              
              // Trigger reading & Tarot
              processComment(user, text);
            }
          }
        }
      }
    }
  });
  
  observer.observe(els.tiktokContainer, {
    childList: true,
    subtree: true
  });
  
  state.isConnected = true;
  updateConnectionState('listening');
  log(`Observer de chat activado. Escuchando comentarios en directo.`, 'success');
}

// Override the log function to also append to the mini-console in the settings modal
const originalLog = window.log || function(){};
window.log = function(msg, type) {
  originalLog(msg, type);
  const miniConsole = document.getElementById('mini-console');
  if (miniConsole) {
    const entry = document.createElement('div');
    entry.textContent = `[${new Date().toLocaleTimeString('es-MX', {hour12: false})}] ${msg}`;
    if(type === 'error') entry.style.color = 'var(--danger)';
    if(type === 'success') entry.style.color = 'var(--accent)';
    miniConsole.appendChild(entry);
    miniConsole.scrollTop = miniConsole.scrollHeight;
  }
};

// Setup Event Listeners & Initialize
function init() {
  // Load initial inputs
  if (els.inputApiKey) els.inputApiKey.value = state.apiKey;
  if (els.inputUsername) els.inputUsername.value = state.username;
  if (els.inputPrompt) els.inputPrompt.value = state.systemPrompt;
  if (els.inputPitch) els.inputPitch.value = state.voicePitch;
  if (els.inputRate) els.inputRate.value = state.voiceRate;
  if (els.checkboxSimulation) {
    els.checkboxSimulation.checked = state.isSimulationEnabled;
  }
  
  // Update diagnostic text
  const diagUser = document.getElementById('diag-user');
  const diagLinkUser = document.getElementById('diag-link-user');
  const diagStatus = document.getElementById('diag-status');
  if (diagUser) diagUser.textContent = state.username || 'username';
  if (diagLinkUser) diagLinkUser.textContent = state.username || 'username';
  if (diagStatus) {
    diagStatus.textContent = state.username ? 'ONLINE' : 'ESPERANDO CONFIG';
    diagStatus.style.color = state.username ? '#39ff14' : '#ffd700';
  }
  
  if (state.apiKey) {
    log('API Key de Gemini cargada correctamente.', 'success');
  } else {
    log('Oráculo sintonizado en modo fallback de servidor.', 'success');
  }
  
  // Easter egg: double click settings modal title to show/hide hidden Gemini and system prompt settings
  const titleEl = document.querySelector('.modal-eyebrow') || document.querySelector('.modal-title');
  if (titleEl) {
    titleEl.addEventListener('click', (e) => {
      // Standard click count tracker
      e.target.clickCount = (e.target.clickCount || 0) + 1;
      if (e.target.clickCount >= 5) {
        document.querySelectorAll('.advanced-option').forEach(el => el.classList.toggle('hidden'));
        log('Opciones avanzadas (API Key y System Prompt) reveladas.', 'success');
        e.target.clickCount = 0;
      }
    });
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
    if(els.inputPrompt) state.systemPrompt = els.inputPrompt.value.trim();
    
    // Update diagnostic text
    document.getElementById('diag-user').textContent = state.username || 'username';
    document.getElementById('diag-link-user').textContent = state.username || 'username';
    document.getElementById('diag-status').textContent = state.username ? 'ONLINE' : 'ESPERANDO CONFIG';
    document.getElementById('diag-status').style.color = state.username ? '#39ff14' : '#ffd700';
    state.voiceName = els.selectVoice.value;
    state.voicePitch = parseFloat(els.inputPitch.value);
    state.voiceRate = parseFloat(els.inputRate.value);
    if (els.checkboxSimulation) {
      state.isSimulationEnabled = els.checkboxSimulation.checked;
    }
    
    localStorage.setItem('co_api_key', state.apiKey);
    localStorage.setItem('co_username', state.username);
    localStorage.setItem('co_system_prompt', state.systemPrompt);
    localStorage.setItem('co_voice_name', state.voiceName);
    localStorage.setItem('co_voice_pitch', state.voicePitch);
    localStorage.setItem('co_voice_rate', state.voiceRate);
    localStorage.setItem('co_simulation_enabled', state.isSimulationEnabled);
    
    log('Configuración de usuario actualizada.', 'success');
    els.settingsModal.classList.remove('open');
    
    if (state.username) {
      updateConnectionState('listening');
    }

    // Toggle simulation based on saved state
    if (state.isSimulationEnabled) {
      startTikTokSimulation();
    } else {
      stopTikTokSimulation();
    }
  });
  
  // Manual comment sender
  els.btnManualSend.addEventListener('click', () => {
    const rawVal = els.inputManualComment.value.trim();
    if (!rawVal) return;
    
    let user = 'Espectador_Cibernético';
    let text = rawVal;
    
    if (rawVal.includes(':')) {
      const idx = rawVal.indexOf(':');
      user = rawVal.substring(0, idx).trim();
      text = rawVal.substring(idx + 1).trim();
    }
    
    els.inputManualComment.value = '';
    
    // Simulate injection into observed container
    const newCommentNode = document.createElement('div');
    newCommentNode.innerHTML = `<strong>@${user}:</strong> <span>${text}</span>`;
    els.tiktokContainer.appendChild(newCommentNode);
  });
  
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
      speakAsync("Saludos, caminante del live de TikTok. Soy el Cyber Oráculo. Deja que el algoritmo místico revele tu destino cuántico.");
    }
  });
  
  // PWA Install Button handler
  if (els.installBtn) {
    els.installBtn.addEventListener('click', async () => {
      if (!state.installPromptEvent) return;
      // Show the install prompt
      state.installPromptEvent.prompt();
      // Wait for the user to respond to the prompt
      const { outcome } = await state.installPromptEvent.userChoice;
      log(`Elección de instalación del usuario: ${outcome}`, 'info');
      // We've used the prompt, and can't use it again
      state.installPromptEvent = null;
      // Hide the banner
      els.installBanner.classList.add('hidden');
    });
  }

  // Tarot Card dismiss click handler
  els.tarotOverlay.addEventListener('click', dismissTarot);

  // Start the observer and the live chat simulator
  initTikTokScraperObserver();
  if (state.isSimulationEnabled) {
    startTikTokSimulation();
  } else {
    log("Simulador desactivado por configuración de usuario.", "info");
  }
}

// Start everything when DOM is loaded
window.addEventListener('DOMContentLoaded', init);
