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

import com.example.data.util.ApiConfig

class MdbListService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun testConnection(rawApiKey: String? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            // Backend handles API key, so we just check if it works through the proxy
            val body = BackendApi.getInstance().getCatalog("test")
            Pair(true, "🟢 Conectado exitosamente")
        } catch (e: Exception) {
            Pair(false, "Fallo: ${e.localizedMessage}")
        }
    }

    suspend fun fetchListItems(listUrl: String, rawApiKey: String? = null): List<CatalogItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CatalogItem>()
        try {
            // Route MDBList URLs through the backend proxy
            val proxyUrl = if (listUrl.contains("mdblist/")) {
                listUrl
            } else {
                "mdblist/search?q=${URLEncoder.encode(listUrl, "UTF-8")}"
            }
            
            val bodyStr = BackendApi.getInstance().getCatalog(proxyUrl)
            
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }
}
