package com.example.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TmdbService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun testConnection(rawApiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val apiKey = if (rawApiKey.trim().isEmpty() || rawApiKey.trim() == "INSERT_KEY_HERE") "ca8c2c77f0a9bfd68cbca8b99009139d" else rawApiKey.trim()
        if (apiKey.isEmpty()) return@withContext Pair(false, "API Key vacía")
        try {
            // Test connection using configuration or popular movie list
            val baseUrl = "https://api.themoviedb.org/3/movie/popular"
            val reqBuilder = Request.Builder()
            if (apiKey.startsWith("ey")) {
                reqBuilder.url(baseUrl).header("Authorization", "Bearer $apiKey")
            } else {
                reqBuilder.url("$baseUrl?api_key=$apiKey")
            }
            
            client.newCall(reqBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    Pair(true, "🟢 Conectado exitosamente")
                } else {
                    Pair(false, "HTTP ${response.code}: Llave inválida o sin permisos")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Fallo: ${e.localizedMessage}")
        }
    }
}
