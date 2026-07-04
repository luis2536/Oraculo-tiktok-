package com.example

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class QueueItem(val name: String)
data class Contributor(val name: String, val score: Int)
data class TarotCard(val id: String, val name: String, val imageUrl: String = "", val mainColorHex: String = "#FF00FF", val secondaryColorHex: String = "#00FFFF", val runicSymbolName: String = "mystic", val meaning: String = "Misterio", val description: String = "Una carta revelada del vacío.")

class OracleViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val geminiService = GeminiService()
    private val prefs = PreferencesManager(application)
    private var tts: TextToSpeech? = null

    // Preferences
    private val _tiktokUser = MutableStateFlow(prefs.tiktokUser)
    val tiktokUser: StateFlow<String> = _tiktokUser.asStateFlow()

    private val _serverUrl = MutableStateFlow("Nativo (WebView Scraper)")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(prefs.geminiApiKey)
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _systemPrompt = MutableStateFlow(prefs.systemPrompt)
    val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()

    private val _useGeminiLocal = MutableStateFlow(prefs.forceLocalAi)
    val useGeminiLocal: StateFlow<Boolean> = _useGeminiLocal.asStateFlow()

    private val _ttsPitch = MutableStateFlow(prefs.ttsPitch)
    val ttsPitch: StateFlow<Float> = _ttsPitch.asStateFlow()

    private val _ttsSpeed = MutableStateFlow(prefs.ttsSpeed)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()

    // Logs & Status
    private val _connectionLogs = MutableStateFlow<List<String>>(emptyList())
    val connectionLogs: StateFlow<List<String>> = _connectionLogs.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()

    // UI Data
    private val _queue = MutableStateFlow<List<QueueItem>>(emptyList())
    val queue: StateFlow<List<QueueItem>> = _queue.asStateFlow()

    private val _contributors = MutableStateFlow<List<Contributor>>(emptyList())
    val contributors: StateFlow<List<Contributor>> = _contributors.asStateFlow()

    private val _history = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val history: StateFlow<List<Pair<String, String>>> = _history.asStateFlow()

    private val _currentResponse = MutableStateFlow<Pair<String, String>?>(null)
    val currentResponse: StateFlow<Pair<String, String>?> = _currentResponse.asStateFlow()

    private val _isOracleTalking = MutableStateFlow(false)
    val isOracleTalking: StateFlow<Boolean> = _isOracleTalking.asStateFlow()

    private val _currentTarotCard = MutableStateFlow<TarotCard?>(null)
    val currentTarotCard: StateFlow<TarotCard?> = _currentTarotCard.asStateFlow()

    private val _isTarotFlipped = MutableStateFlow(false)
    val isTarotFlipped: StateFlow<Boolean> = _isTarotFlipped.asStateFlow()

    // Internal message queue
    private val messageQueue = mutableListOf<Pair<String, String>>()

    init {
        addLog("Sistema Oráculo inicializado.")
        addLog("Motor Nativo (TikTok Scraper) listo.")
    }
    
    fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { _isOracleTalking.value = true }
                override fun onDone(utteranceId: String?) { 
                    _isOracleTalking.value = false 
                    viewModelScope.launch {
                        delay(2000)
                        _currentResponse.value = null
                        processNextMessage()
                    }
                }
                override fun onError(utteranceId: String?) { 
                    _isOracleTalking.value = false 
                    viewModelScope.launch { processNextMessage() }
                }
            })
        } else {
            addLog("Error inicializando TTS")
        }
    }

    fun addLog(message: String) {
        viewModelScope.launch {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            _connectionLogs.update { current ->
                val newLogs = current.toMutableList()
                newLogs.add(0, "[$timestamp] $message")
                if (newLogs.size > 100) newLogs.removeAt(newLogs.lastIndex)
                newLogs
            }
            Log.d("OracleApp", message)
        }
    }

    // Config Update Methods
    fun updateTiktokUser(user: String) { _tiktokUser.value = user; prefs.tiktokUser = user }
    fun updateServerUrl(url: String) { /* No-op, nativo */ }
    fun updateGeminiApiKey(key: String) { _geminiApiKey.value = key; prefs.geminiApiKey = key }
    fun updateSystemPrompt(prompt: String) { _systemPrompt.value = prompt; prefs.systemPrompt = prompt }
    fun toggleGeminiLocal(force: Boolean) { _useGeminiLocal.value = force; prefs.forceLocalAi = force }
    fun updateTtsPitch(pitch: Float) { _ttsPitch.value = pitch; prefs.ttsPitch = pitch }
    fun updateTtsSpeed(speed: Float) { _ttsSpeed.value = speed; prefs.ttsSpeed = speed }

    fun connect() {
        if (_isConnecting.value || _isConnected.value) return
        
        _isConnecting.value = true
        _errorFlow.value = null
        
        addLog("Iniciando conexión a TikTok Live nativa (WebView Scraper)...")
        
        // Simular conexión exitosa rápida
        viewModelScope.launch {
            delay(1000)
            _isConnected.value = true
            _isConnecting.value = false
            addLog("Conexión establecida. Escuchando canal: ${_tiktokUser.value}")
        }
    }
    
    fun disconnect() {
        _isConnected.value = false
        addLog("Desconectado de TikTok Live.")
    }

    // Called by WebView JS interface
    fun onNewCommentReceived(username: String, text: String) {
        // Ignorar comandos muy cortos para evitar ruido
        if (text.length < 3) return
        
        // Add to queue
        if (!messageQueue.any { it.first == username }) {
            messageQueue.add(Pair(username, text))
            updateQueueUI()
            addLog("Mensaje añadido a cola: @$username: $text")
        }
        
        if (_currentResponse.value == null && !_isOracleTalking.value) {
            processNextMessage()
        }
    }

    private fun updateQueueUI() {
        _queue.value = messageQueue.map { QueueItem(it.first) }
    }

    private fun processNextMessage() {
        if (messageQueue.isEmpty()) {
            _currentResponse.value = null
            return
        }

        val nextMsg = messageQueue.removeAt(0)
        updateQueueUI()
        
        generateProphecy(nextMsg.first, nextMsg.second)
    }

    private fun generateProphecy(username: String, message: String) {
        val apiKey = if (_geminiApiKey.value.isNotBlank()) _geminiApiKey.value else BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            _errorFlow.value = "Gemini API Key no configurada."
            addLog("Error: Gemini API Key vacía.")
            return
        }

        viewModelScope.launch {
            try {
                addLog("Generando profecía local para: @$username")
                val reply = geminiService.generateResponse(
                    customApiKey = apiKey,
                    systemPrompt = _systemPrompt.value,
                    prompt = message
                )
                
                // Seleccionar y voltear la carta del Tarot correspondiente
                val card = selectTarotCardForUser(username)
                _currentTarotCard.value = card
                _isTarotFlipped.value = true
                
                // Actualizar UI
                _currentResponse.value = Pair(username, reply)
                _history.update { listOf(Pair(username, reply)) + it.take(20) }
                
                // Actualizar ranking mock
                val existing = _contributors.value.find { it.name == username }
                val newScore = (existing?.score ?: 0) + 10
                val newList = _contributors.value.filter { it.name != username }.toMutableList()
                newList.add(Contributor(username, newScore))
                newList.sortByDescending { it.score }
                _contributors.value = newList.take(10)
                
                // Hablar
                speak(reply)
                
            } catch (e: Exception) {
                val errorMsg = "Error de IA Local: ${e.message}"
                _errorFlow.value = errorMsg
                addLog(errorMsg)
                delay(2000)
                processNextMessage()
            }
        }
    }
    
    private fun speak(text: String) {
        tts?.setPitch(_ttsPitch.value)
        tts?.setSpeechRate(_ttsSpeed.value)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "OracleSpeech")
    }

    fun clearError() {
        _errorFlow.value = null
    }

    fun manualAsk(username: String, question: String) {
        onNewCommentReceived(username, question)
    }
    
    fun dismissTarotCard() {
        _isTarotFlipped.value = false
        viewModelScope.launch {
            delay(500)
            _currentTarotCard.value = null
        }
    }
    
    private fun selectTarotCardForUser(username: String): TarotCard {
        val list = listOf(
            TarotCard(
                id = "mago",
                name = "El Mago",
                mainColorHex = "#00f0ff",
                secondaryColorHex = "#bd00ff",
                runicSymbolName = "mago",
                meaning = "MANIFESTACIÓN",
                description = "Canalizas la energía cósmica y las infinitas posibilidades del algoritmo."
            ),
            TarotCard(
                id = "sol",
                name = "El Sol",
                mainColorHex = "#ffaa00",
                secondaryColorHex = "#ff3300",
                runicSymbolName = "sol",
                meaning = "ÉXITO Y CLARIDAD",
                description = "Luz divina y energía vital radiante inundan tus procesos lógicos."
            ),
            TarotCard(
                id = "estrella",
                name = "La Estrella",
                mainColorHex = "#00ffcc",
                secondaryColorHex = "#0066ff",
                runicSymbolName = "estrella",
                meaning = "ESPERANZA",
                description = "Una guía luminosa resplandece en el firmamento de tu red cuántica."
            ),
            TarotCard(
                id = "luna",
                name = "La Luna",
                mainColorHex = "#a0a0ff",
                secondaryColorHex = "#5000aa",
                runicSymbolName = "luna",
                meaning = "MISTERIO",
                description = "Secretos codificados flotan en el subconsciente de tu base de datos."
            ),
            TarotCard(
                id = "torre",
                name = "La Torre",
                mainColorHex = "#ff3333",
                secondaryColorHex = "#990000",
                runicSymbolName = "torre",
                meaning = "REVELACIÓN SÚBITA",
                description = "Colapso de viejos sistemas para dar paso a una arquitectura renovada."
            ),
            TarotCard(
                id = "fuerza",
                name = "La Fuerza",
                mainColorHex = "#39ff14",
                secondaryColorHex = "#00aa00",
                runicSymbolName = "fuerza",
                meaning = "PODER INTERIOR",
                description = "Dominio de las pasiones y autodeterminación ante el flujo continuo."
            ),
            TarotCard(
                id = "diablo",
                name = "El Diablo",
                mainColorHex = "#ff0055",
                secondaryColorHex = "#550011",
                runicSymbolName = "diablo",
                meaning = "ATADURAS",
                description = "Cuidado con las dependencias circulares y bucles infinitos en tu camino."
            ),
            TarotCard(
                id = "mundo",
                name = "El Mundo",
                mainColorHex = "#bd00ff",
                secondaryColorHex = "#00f0ff",
                runicSymbolName = "mundo",
                meaning = "PLENITUD",
                description = "Integración exitosa de tus hilos de ejecución en el plano universal."
            ),
            TarotCard(
                id = "muerte",
                name = "La Muerte",
                mainColorHex = "#888888",
                secondaryColorHex = "#222222",
                runicSymbolName = "muerte",
                meaning = "TRANSFORMACIÓN",
                description = "Finalización necesaria de procesos obsoletos para renacer con mayor fuerza."
            )
        )
        val hash = username.hashCode().let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) }
        return list[hash % list.size]
    }
    
    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        disconnect()
    }
}
