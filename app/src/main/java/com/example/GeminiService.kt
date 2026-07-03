package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApi by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApi::class.java)
    }
}

class GeminiService {
    suspend fun generateResponse(prompt: String, customApiKey: String? = null, systemPrompt: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrEmpty()) customApiKey else BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key no configurada. Añádela en Configuración."
        }

        val baseSystem = "Eres un Oráculo místico y cyber-futurista en TikTok Live. Responde de forma muy misteriosa e impactante en español. LÍMITE CRÍTICO: Tu respuesta DEBE ser extremadamente breve, de máximo 15 palabras en una sola oración fluida y directa, para mantener la fluidez en el stream."
        val actualSystemPrompt = if (systemPrompt.isNullOrEmpty()) {
            baseSystem
        } else {
            "$systemPrompt. NOTA: Responde en máximo 15 palabras en una sola oración mística y contundente."
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = actualSystemPrompt))
            )
        )
        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "El oráculo se quedó sin palabras."
        } catch (e: Exception) {
            "Error de conexión con el Oráculo: ${e.message}"
        }
    }
}
