package com.example

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class QueueItem(val id: String, val name: String)
data class Contributor(val id: String, val name: String, val score: Int)

data class TarotCard(
    val name: String,
    val meaning: String,
    val runicSymbolName: String, // "sol", "mago", "muerte", "estrella", "torre", "luna", "fuerza", "diablo", "mundo", "loco"
    val mainColorHex: String,
    val secondaryColorHex: String,
    val description: String
)

class OracleViewModel(
    private val preferencesManager: PreferencesManager,
    private val geminiService: GeminiService
) : ViewModel() {
    private var socket: Socket? = null
    private var tts: TextToSpeech? = null

    val tarotDeck = listOf(
        TarotCard("El Sol", "Éxito, vitalidad, brillo divino e iluminación", "sol", "#FFD700", "#FF8C00", "La claridad absoluta penetra la niebla cibernética."),
        TarotCard("La Muerte", "Transmutación radical, cierres, renacer y transición", "muerte", "#9C27B0", "#FF1744", "El fin de un ciclo de datos y el renacimiento de un nuevo núcleo."),
        TarotCard("El Mago", "Manifestación de voluntad absoluta, poder y recursos", "mago", "#00E5FF", "#00E676", "Canaliza energías cósmicas para moldear la realidad virtual."),
        TarotCard("La Estrella", "Guía estelar, esperanza, renovación espiritual", "estrella", "#00B0FF", "#E040FB", "Un faro de esperanza brillando en el vasto abismo digital."),
        TarotCard("La Torre", "Disrupción súbita, colapso de estructuras, revelación", "torre", "#FF3D00", "#FFEA00", "La destrucción inevitable de barreras obsoletas para liberar la verdad."),
        TarotCard("La Luna", "Intuición profunda, misterio, subconsciente, ilusiones", "luna", "#1A237E", "#00E5FF", "Filtra reflejos engañosos y despierta las percepciones psíquicas."),
        TarotCard("La Fuerza", "Dominio interno, coraje, paciencia, compasión", "fuerza", "#FF9100", "#D50000", "Domina las bestias de silicio con la pura vibración de tu espíritu."),
        TarotCard("El Diablo", "Apegos materiales, impulsos inconscientes, tentación", "diablo", "#FF1744", "#212121", "Esclavitud a las ilusiones de la red y el deseo carnal inconsciente."),
        TarotCard("El Mundo", "Realización total, completitud, integración absoluta", "mundo", "#AA00FF", "#00E676", "Culminación perfecta del viaje a través del macrocosmos virtual."),
        TarotCard("El Loco", "Nuevos comienzos, saltos de fe, potencial ilimitado", "loco", "#E040FB", "#00E5FF", "Un paso al vacío con confianza ciega en la providencia cuántica.")
    )

    private val _currentTarotCard = MutableStateFlow<TarotCard?>(null)
    val currentTarotCard: StateFlow<TarotCard?> = _currentTarotCard.asStateFlow()

    private val _isTarotFlipped = MutableStateFlow(false)
    val isTarotFlipped: StateFlow<Boolean> = _isTarotFlipped.asStateFlow()

    private val _serverUrl = MutableStateFlow("http://192.168.1.26:3000")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _connectionLogs = MutableStateFlow<List<String>>(emptyList())
    val connectionLogs: StateFlow<List<String>> = _connectionLogs.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    fun addLog(message: String) {
        viewModelScope.launch {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            _connectionLogs.update { current ->
                val newLogs = current.toMutableList()
                newLogs.add(0, "[$timestamp] $message")
                if (newLogs.size > 100) newLogs.removeLast()
                newLogs
            }
            Log.d("OracleApp", message)
        }
    }

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _queue = MutableStateFlow<List<QueueItem>>(emptyList())
    val queue: StateFlow<List<QueueItem>> = _queue.asStateFlow()

    private val _contributors = MutableStateFlow<List<Contributor>>(emptyList())
    val contributors: StateFlow<List<Contributor>> = _contributors.asStateFlow()

    private val _currentResponse = MutableStateFlow<Pair<String, String>?>(null) // Username, Response
    val currentResponse: StateFlow<Pair<String, String>?> = _currentResponse.asStateFlow()

    private val _isOracleTalking = MutableStateFlow(false)
    val isOracleTalking: StateFlow<Boolean> = _isOracleTalking.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()
    
    private val _ttsPitch = MutableStateFlow(1.0f)
    val ttsPitch: StateFlow<Float> = _ttsPitch.asStateFlow()
    
    private val _ttsSpeed = MutableStateFlow(1.05f)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()
    
    private val _systemPrompt = MutableStateFlow("Eres un Oráculo místico y cyber-futurista en TikTok Live. Das respuestas breves, misteriosas, impactantes y divertidas. Habla en español, pausado y con tono de deidad o entidad digital avanzada. Tus respuestas no deben superar las 3 oraciones cortas.")
    val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()
    
    private val _tiktokUser = MutableStateFlow("syntropylabs")
    val tiktokUser: StateFlow<String> = _tiktokUser.asStateFlow()
    
    private val _history = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val history: StateFlow<List<Pair<String, String>>> = _history.asStateFlow()

    private val _useGeminiLocal = MutableStateFlow(false)
    val useGeminiLocal: StateFlow<Boolean> = _useGeminiLocal.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()

    fun clearError() {
        _errorFlow.value = null
    }

    init {
        viewModelScope.launch {
            launch {
                preferencesManager.serverUrlFlow.collect { url ->
                    _serverUrl.value = url
                }
            }
            launch {
                preferencesManager.geminiApiKeyFlow.collect { key ->
                    _geminiApiKey.value = key
                }
            }
            launch {
                preferencesManager.ttsPitchFlow.collect { pitch ->
                    _ttsPitch.value = pitch
                    tts?.setPitch(pitch)
                }
            }
            launch {
                preferencesManager.ttsSpeedFlow.collect { speed ->
                    _ttsSpeed.value = speed
                    tts?.setSpeechRate(speed)
                }
            }
            launch {
                preferencesManager.systemPromptFlow.collect { prompt ->
                    _systemPrompt.value = prompt
                }
            }
            launch {
                preferencesManager.tiktokUserFlow.collect { user ->
                    _tiktokUser.value = user
                }
            }
        }
    }

    fun updateServerUrl(url: String) {
        _serverUrl.value = url
        viewModelScope.launch {
            preferencesManager.saveServerUrl(url)
        }
    }
    
    fun updateGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        viewModelScope.launch {
            preferencesManager.saveGeminiApiKey(key)
        }
    }
    
    fun updateSystemPrompt(prompt: String) {
        _systemPrompt.value = prompt
        viewModelScope.launch {
            preferencesManager.saveSystemPrompt(prompt)
        }
    }
    
    fun updateTiktokUser(user: String) {
        _tiktokUser.value = user
        viewModelScope.launch {
            preferencesManager.saveTiktokUser(user)
        }
    }
    
    fun updateTtsPitch(pitch: Float) {
        _ttsPitch.value = pitch
        tts?.setPitch(pitch)
        viewModelScope.launch {
            preferencesManager.saveTtsPitch(pitch)
        }
    }
    
    fun updateTtsSpeed(speed: Float) {
        _ttsSpeed.value = speed
        tts?.setSpeechRate(speed)
        viewModelScope.launch {
            preferencesManager.saveTtsSpeed(speed)
        }
    }

    fun toggleGeminiLocal(useLocal: Boolean) {
        _useGeminiLocal.value = useLocal
    }

    fun initTts(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale.Builder().setLanguage("es").setRegion("ES").build()
                tts?.language = locale
                tts?.setSpeechRate(_ttsSpeed.value)
                tts?.setPitch(_ttsPitch.value)
            }
        }
    }

    fun connect() {
        if (socket?.connected() == true) return
        
        _isConnecting.value = true
        try {
            val opts = IO.Options()
            opts.reconnection = true
            opts.query = "tiktokUser=${_tiktokUser.value}"
            socket = IO.socket(_serverUrl.value, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                _isConnected.value = true
                _isConnecting.value = false
                addLog("Conexión establecida con éxito.")
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                _isConnected.value = false
                _isConnecting.value = false
                addLog("Desconectado del servidor relay.")
            }
            
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                _isConnecting.value = false
                val errorMsg = args.firstOrNull()?.toString() ?: "Error desconocido"
                addLog("Error de conexión Socket.IO: $errorMsg")
                // Silenced error flow for local-only mode to prevent UI spam
                Log.e("Oracle", "Error de conexión Socket.IO: $errorMsg (Modo Local)")
            }

            socket?.on("queue_update") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONArray) {
                        val newQueue = mutableListOf<QueueItem>()
                        for (i in 0 until data.length()) {
                            val obj = data.getJSONObject(i)
                            newQueue.add(QueueItem(obj.getString("id"), obj.getString("name")))
                        }
                        _queue.value = newQueue
                    }
                }
            }

            socket?.on("gift_received") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONArray) {
                        val newContributors = mutableListOf<Contributor>()
                        for (i in 0 until data.length()) {
                            val obj = data.getJSONObject(i)
                            newContributors.add(Contributor(obj.getString("id"), obj.getString("name"), obj.getInt("score")))
                        }
                        _contributors.value = newContributors
                    }
                }
            }

            socket?.on("oracle_response") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val name = data.optString("name", "Usuario")
                    val isQuestion = data.has("question")
                    val input = if (isQuestion) data.optString("question", "") else data.optString("response", "")
                    
                    viewModelScope.launch {
                        try {
                            val drawnCard = tarotDeck.random()
                            _currentTarotCard.value = drawnCard
                            _isTarotFlipped.value = false
                            
                            val response = if (isQuestion || _useGeminiLocal.value) {
                                val customPrompt = "El usuario $name pregunta: $input. " +
                                                 "La carta del Tarot revelada de tu baraja cibernética es '${drawnCard.name}' (Significado místico: ${drawnCard.meaning}. Descripción: ${drawnCard.description}). " +
                                                 "Como Deidad del Oráculo de Silicio, incorpora directamente el nombre y el simbolismo de esta carta en tu predicción mística cyber-futurista de forma fluida."
                                geminiService.generateResponse(customPrompt, _geminiApiKey.value, _systemPrompt.value)
                            } else {
                                input
                            }
                            
                            val newEntry = Pair(name, response)
                            _currentResponse.value = newEntry
                            _history.update { listOf(newEntry) + it.take(49) } // Keep last 50
                            
                            _isTarotFlipped.value = true // Revelar la carta mística
                            _isOracleTalking.value = true
                            speakText(response)
                            
                            delay((response.length * (80f / _ttsSpeed.value)).toLong().coerceAtLeast(3000L))
                            _isOracleTalking.value = false
                        } catch (e: Exception) {
                            _errorFlow.value = "Error IA: ${e.message}"
                            _isOracleTalking.value = false
                        }
                    }
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            _isConnecting.value = false
            _errorFlow.value = "Error: ${e.message}"
            Log.e("Oracle", "Connection error", e)
        }
    }
    
    fun manualAsk(question: String, name: String = "Admin") {
        viewModelScope.launch {
            try {
                val drawnCard = tarotDeck.random()
                _currentTarotCard.value = drawnCard
                _isTarotFlipped.value = false
                
                val customPrompt = "El usuario $name pregunta: $question. " +
                                 "La carta del Tarot revelada de tu baraja cibernética es '${drawnCard.name}' (Significado místico: ${drawnCard.meaning}. Descripción: ${drawnCard.description}). " +
                                 "Como Deidad del Oráculo de Silicio, incorpora directamente el nombre y el simbolismo de esta carta en tu predicción mística cyber-futurista de forma fluida."
                
                val response = geminiService.generateResponse(customPrompt, _geminiApiKey.value, _systemPrompt.value)
                val newEntry = Pair(name, response)
                _currentResponse.value = newEntry
                _history.update { listOf(newEntry) + it.take(49) }
                
                _isTarotFlipped.value = true // Revelar la carta mística
                _isOracleTalking.value = true
                speakText(response)
                
                delay((response.length * (80f / _ttsSpeed.value)).toLong().coerceAtLeast(3000L))
                _isOracleTalking.value = false
            } catch (e: Exception) {
                _errorFlow.value = "Error IA: ${e.message}"
                _isOracleTalking.value = false
            }
        }
    }

    fun dismissTarotCard() {
        _currentTarotCard.value = null
    }

    fun sanitizeTextForSpeech(input: String): String {
        var text = input
        
        // Remove markdown bold/italic/code formatting
        text = text.replace("**", "")
        text = text.replace("*", "")
        text = text.replace("_", "")
        text = text.replace("`", "")
        
        // Replace hashtags with a comma to act as a respiratory/breathing pause
        // E.g. "#fortuna #exito" -> ", fortuna , exito"
        text = text.replace("#", ", ")
        
        // Remove typical emojis and non-verbal symbols
        text = text.replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), " ")
        
        // Remove special characters that TTS might spell out
        text = text.replace("[", "")
        text = text.replace("]", "")
        text = text.replace("{", "")
        text = text.replace("}", "")
        text = text.replace("<", "")
        text = text.replace(">", "")
        text = text.replace("~", " ")
        text = text.replace("\"", "")
        
        // Avoid duplicate punctuation or spaces
        text = text.replace(Regex("\\s+"), " ")
        text = text.replace(Regex(",\\s*,"), ", ")
        text = text.replace(Regex("\\.+\\s*\\.+"), ". ")
        text = text.replace("...", ". ")
        
        return text.trim()
    }

    private fun speakText(text: String) {
        val cleanText = sanitizeTextForSpeech(text)
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        _isConnected.value = false
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        disconnect()
    }
}
