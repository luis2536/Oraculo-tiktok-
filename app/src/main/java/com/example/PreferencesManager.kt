package com.example

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("oracle_prefs", Context.MODE_PRIVATE)

    var tiktokUser: String
        get() = prefs.getString("tiktok_user", "@username") ?: "@username"
        set(value) = prefs.edit().putString("tiktok_user", value).apply()

    var geminiApiKey: String
        get() = prefs.getString("gemini_api_key", "") ?: ""
        set(value) = prefs.edit().putString("gemini_api_key", value).apply()

    var systemPrompt: String
        get() = prefs.getString("system_prompt", "Eres un Oráculo místico en TikTok Live...") ?: ""
        set(value) = prefs.edit().putString("system_prompt", value).apply()

    var forceLocalAi: Boolean
        get() = prefs.getBoolean("force_local_ai", true)
        set(value) = prefs.edit().putBoolean("force_local_ai", value).apply()

    var ttsPitch: Float
        get() = prefs.getFloat("tts_pitch", 1.0f)
        set(value) = prefs.edit().putFloat("tts_pitch", value).apply()

    var ttsSpeed: Float
        get() = prefs.getFloat("tts_speed", 1.0f)
        set(value) = prefs.edit().putFloat("tts_speed", value).apply()
}
