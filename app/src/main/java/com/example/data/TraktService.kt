package com.example.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.data.util.ApiConfig

class TraktService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun testConnection(rawClientId: String? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val clientId = rawClientId?.trim()?.ifEmpty { ApiConfig.TRAKT_CLIENT_ID } ?: ApiConfig.TRAKT_CLIENT_ID
        if (clientId.isEmpty()) return@withContext Pair(false, "Client ID vacío")
        try {
            val url = "https://api.trakt.tv/genres/movies"
            val request = Request.Builder()
                .url(url)
                .header("trakt-api-version", "2")
                .header("trakt-api-key", clientId.trim())
                .build()
                
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Pair(true, "🟢 Conectado exitosamente")
                } else {
                    Pair(false, "HTTP ${response.code}: Client ID no autorizado")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Fallo: ${e.localizedMessage}")
        }
    }
}
