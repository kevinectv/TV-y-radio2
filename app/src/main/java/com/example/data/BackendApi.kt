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

    suspend fun getTrendingMoviesWeek(): String = fetch("$BASE_URL/trending/movie/week")
    suspend fun getTrendingTvWeek(): String = fetch("$BASE_URL/trending/tv/week")
    suspend fun getTrendingAllDay(): String = fetch("$BASE_URL/trending/all/day")
    
    suspend fun search(query: String): String = fetch("$BASE_URL/search?q=${URLEncoder.encode(query, "UTF-8")}")
    
    suspend fun getPopularMovies(): String = fetch("$BASE_URL/movie/popular")
    suspend fun getPopularTv(): String = fetch("$BASE_URL/tv/popular")
    
    suspend fun getTopRatedMovies(): String = fetch("$BASE_URL/movie/top_rated")
    suspend fun getTopRatedTv(): String = fetch("$BASE_URL/tv/top_rated")

    suspend fun discoverTv(params: String): String = fetch("$BASE_URL/discover/tv?$params")
    suspend fun discoverMovie(params: String): String = fetch("$BASE_URL/discover/movie?$params")

    suspend fun getTv(id: String): String = fetch("$BASE_URL/tv/$id")
    suspend fun getMovie(id: String): String = fetch("$BASE_URL/movie/$id")
    
    suspend fun getWatchProviders(mediaType: String, id: String): String = fetch("$BASE_URL/$mediaType/$id/watch/providers")
    suspend fun getVideos(mediaType: String, id: String): String = fetch("$BASE_URL/$mediaType/$id/videos")

    suspend fun getCatalog(category: String): String = fetch("$BASE_URL/catalogs/$category")

    suspend fun getMovieImages(id: String): String = fetch("$BASE_URL/movie/$id/images")
    suspend fun getTvImages(id: String): String = fetch("$BASE_URL/tv/$id/images")
    suspend fun getMovieVideos(id: String): String = fetch("$BASE_URL/movie/$id/videos")
    suspend fun getTvVideos(id: String): String = fetch("$BASE_URL/tv/$id/videos")
    suspend fun getMovieCredits(id: String): String = fetch("$BASE_URL/movie/$id/credits")
    suspend fun getTvCredits(id: String): String = fetch("$BASE_URL/tv/$id/credits")

    private fun fetch(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Error: ${response.code}")
            return response.body?.string() ?: ""
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
