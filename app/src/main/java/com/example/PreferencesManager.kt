package com.example

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val SERVER_URL_KEY = stringPreferencesKey("server_url")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val TTS_PITCH_KEY = floatPreferencesKey("tts_pitch")
        val TTS_SPEED_KEY = floatPreferencesKey("tts_speed")
    }

    val serverUrlFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SERVER_URL_KEY] ?: "http://192.168.1.26:3000"
        }

    val geminiApiKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[GEMINI_API_KEY] ?: ""
        }
        
    val ttsPitchFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[TTS_PITCH_KEY] ?: 1.0f
        }
        
    val ttsSpeedFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[TTS_SPEED_KEY] ?: 0.85f
        }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = url
        }
    }
    
    suspend fun saveGeminiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[GEMINI_API_KEY] = key
        }
    }
    
    suspend fun saveTtsPitch(pitch: Float) {
        context.dataStore.edit { preferences ->
            preferences[TTS_PITCH_KEY] = pitch
        }
    }
    
    suspend fun saveTtsSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[TTS_SPEED_KEY] = speed
        }
    }
}
