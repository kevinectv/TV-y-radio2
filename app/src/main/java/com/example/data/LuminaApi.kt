package com.example.data

import com.example.data.model.Catalog
import com.example.data.model.CatalogItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LuminaApiService {
    @GET("api/home")
    suspend fun getHome(): List<Catalog>

    @GET("api/catalogs")
    suspend fun getCatalogs(): List<Catalog>

    @GET("api/trending")
    suspend fun getTrending(): List<CatalogItem>

    @GET("api/search")
    suspend fun search(@Query("query") query: String): List<CatalogItem>

    @GET("api/details")
    suspend fun getDetails(
        @Query("id") id: String,
        @Query("type") type: String
    ): CatalogItem

    @GET("api/movie")
    suspend fun getMovie(@Query("id") id: String): CatalogItem

    @GET("api/mdblist")
    suspend fun getMdbList(@Query("id") id: String): CatalogItem
}

object LuminaApi {
    private const val BASE_URL = "https://lumina-api-coral.vercel.app/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val service: LuminaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(httpClient)
            .build()
            .create(LuminaApiService::class.java)
    }
}
