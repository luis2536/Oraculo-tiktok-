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

class OracleViewModel(
    private val preferencesManager: PreferencesManager,
    private val geminiService: GeminiService
) : ViewModel() {
    private var socket: Socket? = null
    private var tts: TextToSpeech? = null

    private val _serverUrl = MutableStateFlow("http://192.168.1.26:3000")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

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
    
    private val _ttsSpeed = MutableStateFlow(0.85f)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()
    
    private val _systemPrompt = MutableStateFlow("Eres un Oráculo místico y cyber-futurista en TikTok Live. Das respuestas breves, misteriosas, impactantes y divertidas. Habla en español, pausado y con tono de deidad o entidad digital avanzada. Tus respuestas no deben superar las 3 oraciones cortas.")
    val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()
    
    private val _tiktokUser = MutableStateFlow("syntropylabs")
    val tiktokUser: StateFlow<String> = _tiktokUser.asStateFlow()
    
    private val _history = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val history: StateFlow<List<Pair<String, String>>> = _history.asStateFlow()

    private val _useGeminiLocal = MutableStateFlow(false)
    val useGeminiLocal: StateFlow<Boolean> = _useGeminiLocal.asStateFlow()

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
                val locale = Locale("es", "ES")
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
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                _isConnected.value = false
                _isConnecting.value = false
            }
            
            socket?.on(Socket.EVENT_CONNECT_ERROR) {
                _isConnecting.value = false
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
                        val response = if (isQuestion || _useGeminiLocal.value) {
                            geminiService.generateResponse("El usuario $name pregunta: $input", _geminiApiKey.value, _systemPrompt.value)
                        } else {
                            input
                        }
                        
                        val newEntry = Pair(name, response)
                        _currentResponse.value = newEntry
                        _history.update { listOf(newEntry) + it.take(49) } // Keep last 50
                        
                        _isOracleTalking.value = true
                        speakText(response)
                        
                        // Keep talking animation while speech is likely playing
                        // This is a naive timeout since TTS doesn't give a perfect onDone event easily without a listener
                        delay((response.length * (80f / _ttsSpeed.value)).toLong().coerceAtLeast(3000L))
                        _isOracleTalking.value = false
                    }
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            _isConnecting.value = false
            Log.e("Oracle", "Connection error", e)
        }
    }
    
    fun manualAsk(question: String, name: String = "Admin") {
        viewModelScope.launch {
            val response = geminiService.generateResponse("El usuario $name pregunta: $question", _geminiApiKey.value, _systemPrompt.value)
            val newEntry = Pair(name, response)
            _currentResponse.value = newEntry
            _history.update { listOf(newEntry) + it.take(49) }
            
            _isOracleTalking.value = true
            speakText(response)
            
            delay((response.length * (80f / _ttsSpeed.value)).toLong().coerceAtLeast(3000L))
            _isOracleTalking.value = false
        }
    }

    private fun speakText(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
