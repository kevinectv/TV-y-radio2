package com.example.data

import android.content.Context
import com.example.data.model.CatalogItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MdbListService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun testConnection(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) return@withContext Pair(false, "API Key vacía")
        try {
            val url = "https://api.mdblist.com/?apikey=$apiKey&s=matrix"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    if (body.contains("error") && !body.contains("search")) {
                        val errObj = JSONObject(body)
                        Pair(false, "API Error: " + errObj.optString("error", "No autorizado"))
                    } else {
                        Pair(true, "🟢 Conectado exitosamente")
                    }
                } else {
                    if (body.isNotEmpty() && body.startsWith("{")) {
                        val errObj = JSONObject(body)
                        Pair(false, "HTTP ${response.code}: " + errObj.optString("error", "No autorizado"))
                    } else {
                        Pair(false, "HTTP Error: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            Pair(false, "Fallo: ${e.localizedMessage}")
        }
    }

    suspend fun fetchListItems(listUrl: String, apiKey: String): List<CatalogItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CatalogItem>()
        try {
            var cleanUrl = listUrl.trim()
            if (cleanUrl.startsWith("https://mdblist.com/lists/") && !cleanUrl.contains("/json") && !cleanUrl.contains("/api")) {
                cleanUrl = cleanUrl.removeSuffix("/") + "/json"
            }
            if (cleanUrl.contains("mdblist.com/api") && apiKey.isNotEmpty() && !cleanUrl.contains("apikey=")) {
                cleanUrl = if (cleanUrl.contains("?")) "$cleanUrl&apikey=$apiKey" else "$cleanUrl/?apikey=$apiKey"
            }

            val request = Request.Builder().url(cleanUrl).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val arr = if (bodyStr.trim().startsWith("{")) {
                        val obj = JSONObject(bodyStr)
                        obj.optJSONArray("items") ?: JSONArray()
                    } else {
                        JSONArray(bodyStr)
                    }

                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val title = obj.optString("title").ifEmpty { obj.optString("name", "Película MDBList") }
                        val year = obj.optString("year", "2024").ifEmpty { "2024" }
                        val rating = String.format(java.util.Locale.US, "%.1f", obj.optDouble("rating", 7.5))
                        val tmdbId = obj.optString("tmdb_id").ifEmpty { obj.optString("tmdbid", "") }
                        val imdbId = obj.optString("imdb_id").ifEmpty { obj.optString("imdbid", "") }
                        val type = obj.optString("mediatype").ifEmpty { obj.optString("type", "movie") }
                        val isTv = type.contains("show") || type.contains("tv")
                        val poster = obj.optString("poster").ifEmpty { obj.optString("poster_url", "") }.ifEmpty {
                            "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=300"
                        }

                        list.add(
                            CatalogItem(
                                id = "mdblist_${imdbId.ifEmpty { tmdbId.ifEmpty { UUID.randomUUID().toString() } }}",
                                title = title,
                                posterUrl = poster,
                                year = year,
                                rating = rating,
                                genre = if (isTv) "Serie" else "Película",
                                description = "Obtenido de lista MDBList. ID: ${imdbId.ifEmpty { tmdbId }}",
                                tmdbId = tmdbId.ifEmpty { null },
                                isTvShow = isTv
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }
}
