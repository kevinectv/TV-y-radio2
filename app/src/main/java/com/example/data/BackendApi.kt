package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class BackendApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val BASE_URL = "https://lumina-api-coral.vercel.app/api"

    suspend fun getHome(): String = fetch("$BASE_URL/home")

    suspend fun getTrending(): String = fetch("$BASE_URL/trending")
    suspend fun getCatalogs(): String = fetch("$BASE_URL/catalogs")
    suspend fun getMdbListStatus(): String = fetch("$BASE_URL/mdblist")

    suspend fun getTrendingMoviesWeek(): String = fetch("$BASE_URL/trending")
    suspend fun getTrendingTvWeek(): String = fetch("$BASE_URL/trending")
    suspend fun getTrendingAllDay(): String = fetch("$BASE_URL/trending")
    
    suspend fun search(query: String): String = fetch("$BASE_URL/search?query=${URLEncoder.encode(query, "UTF-8")}")
    
    suspend fun getPopularMovies(): String = fetch("$BASE_URL/trending")
    suspend fun getPopularTv(): String = fetch("$BASE_URL/trending")
    
    suspend fun getTopRatedMovies(): String = fetch("$BASE_URL/trending")
    suspend fun getTopRatedTv(): String = fetch("$BASE_URL/trending")

    suspend fun discoverTv(params: String): String = fetch("$BASE_URL/trending")
    suspend fun discoverMovie(params: String): String = fetch("$BASE_URL/trending")

    suspend fun getTv(id: String): String = fetch("$BASE_URL/tv?id=$id")
    suspend fun getMovie(id: String): String = fetch("$BASE_URL/movie?id=$id")
    
    suspend fun getWatchProviders(mediaType: String, id: String): String = fetch("$BASE_URL/$mediaType?id=$id")
    suspend fun getVideos(mediaType: String, id: String): String = fetch("$BASE_URL/$mediaType?id=$id")

    suspend fun getCatalog(category: String): String = fetch("$BASE_URL/catalogs")

    suspend fun getMovieImages(id: String): String = fetch("$BASE_URL/movie?id=$id")
    suspend fun getTvImages(id: String): String = fetch("$BASE_URL/tv?id=$id")
    suspend fun getMovieVideos(id: String): String = fetch("$BASE_URL/movie?id=$id")
    suspend fun getTvVideos(id: String): String = fetch("$BASE_URL/tv?id=$id")
    suspend fun getMovieCredits(id: String): String = fetch("$BASE_URL/movie?id=$id")
    suspend fun getTvCredits(id: String): String = fetch("$BASE_URL/tv?id=$id")

    private suspend fun fetch(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Error: ${response.code}")
            return@withContext response.body?.string() ?: ""
        }
    }

    companion object {
        private var instance: BackendApi? = null
        fun getInstance(): BackendApi {
            if (instance == null) {
                instance = BackendApi()
            }
            return instance!!
        }
    }
}
