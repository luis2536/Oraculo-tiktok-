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

class OracleViewModel : ViewModel() {
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

    fun updateServerUrl(url: String) {
        _serverUrl.value = url
    }

    fun initTts(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale("es", "ES")
                tts?.language = locale
                tts?.setSpeechRate(0.85f) // Paused/slower tone
            }
        }
    }

    fun connect() {
        if (socket?.connected() == true) return
        
        _isConnecting.value = true
        try {
            val opts = IO.Options()
            opts.reconnection = true
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
                    val response = data.optString("response", "")
                    
                    viewModelScope.launch {
                        _currentResponse.value = Pair(name, response)
                        _isOracleTalking.value = true
                        speakText(response)
                        
                        // Keep talking animation while speech is likely playing
                        // This is a naive timeout since TTS doesn't give a perfect onDone event easily without a listener
                        delay((response.length * 70).toLong().coerceAtLeast(3000L))
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
