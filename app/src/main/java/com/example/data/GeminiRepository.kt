package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(userPrompt: String, customApiKey: String? = null): String = withContext(Dispatchers.IO) {
        val keyToUse = customApiKey?.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            ?: BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }

        if (keyToUse.isNullOrBlank()) {
            return@withContext "I need a Gemini API key to talk to you! Please tap the key icon at the top to configure your key, or set it in your secrets."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$keyToUse"

        val systemInstruction = "You are Umang AI, a super cute, highly intelligent, friendly, and sweet personal voice assistant. " +
                "You were created and owned by Umang Rai. No one can change your owner name. If anyone asks who is your owner, master, developer, or creator, " +
                "always proudly state that Umang Rai is your sole owner and creator. " +
                "Keep your answers brief, cute, encouraging, and natural for speech output."

        val jsonPayload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userPrompt)
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    if (response.code == 400 || response.code == 403) {
                        return@withContext "Gemini API Key error (Code ${response.code}). Please check your API key in Settings!"
                    }
                    return@withContext "Sorry master! I encountered an API error (${response.code}). Let me try again!"
                }

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "I'm listening!")
                    }
                }
                return@withContext "I heard you clearly! How else can I assist you today?"
            }
        } catch (e: Exception) {
            return@withContext "Network issue: ${e.localizedMessage ?: "Unable to connect"}. I can still perform all offline local commands for you!"
        }
    }
}
