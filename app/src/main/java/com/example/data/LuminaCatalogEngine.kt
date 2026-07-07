package com.example.data

import android.content.Context
import com.example.data.model.Catalog
import com.example.data.model.CatalogItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.util.UUID

import com.example.data.util.ApiConfig
import com.example.data.LuminaApi

data class EngineActorInfo(val name: String, val role: String, val photoUrl: String)

class LuminaCatalogEngine(private val context: Context, private val repository: CatalogRepository) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val _isEnriching = MutableStateFlow(false)
    val isEnriching: StateFlow<Boolean> = _isEnriching

    companion object {
        fun serializeCast(cast: List<EngineActorInfo>): String {
            return cast.joinToString(separator = ";;") { "${it.name}|${it.role}|${it.photoUrl}" }
        }

        fun deserializeCast(castStr: String?): List<EngineActorInfo> {
            if (castStr.isNullOrEmpty()) return emptyList()
            return castStr.split(";;").mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size >= 2) {
                    val name = parts[0]
                    val role = parts[1]
                    var photo = parts.getOrNull(2) ?: ""
                    if (photo.startsWith("/")) {
                        photo = "https://image.tmdb.org/t/p/w185$photo"
                    }
                    EngineActorInfo(name, role, photo)
                } else null
            }
        }
    }

    /**
     * Enriches a single Catalog Item with all TMDB Metadata (Logo, Backdrop, Trailer, Cast, Director, etc.)
     * and returns the enriched item.
     */
    suspend fun enrichCatalogItem(item: CatalogItem, rawApiKey: String? = null): CatalogItem = withContext(Dispatchers.IO) {
        // If it's already enriched completely, we don't need to re-query
        if (!item.backdropUrl.isNullOrEmpty() && !item.castJson.isNullOrEmpty() && !item.logoUrl.isNullOrEmpty() && !item.imdbRating.isNullOrEmpty()) {
            return@withContext item
        }

        try {
            val tmdbId = item.tmdbId ?: item.id.replace(Regex("[^0-9]"), "")
            if (tmdbId.isEmpty()) return@withContext item

            val mediaType = if (item.isTvShow) "tv" else "movie"
            
            // Call the new centralized backend
            val enriched = LuminaApi.service.getDetails(tmdbId, mediaType)
            
            // Merge with existing item to keep stream URLs or other local data
            return@withContext item.copy(
                poster_path = enriched.poster_path?.ifEmpty { item.poster_path } ?: item.poster_path,
                backdrop_path = enriched.backdrop_path?.ifEmpty { item.backdrop_path } ?: item.backdrop_path,
                logo_path = enriched.logo_path?.ifEmpty { item.logo_path } ?: item.logo_path,
                profile_path = enriched.profile_path?.ifEmpty { item.profile_path } ?: item.profile_path,
                logoUrl = enriched.logoUrl?.ifEmpty { item.logoUrl } ?: item.logoUrl,
                backdropUrl = enriched.backdropUrl?.ifEmpty { item.backdropUrl } ?: item.backdropUrl,
                trailerUrl = enriched.trailerUrl?.ifEmpty { item.trailerUrl } ?: item.trailerUrl,
                director = enriched.director?.ifEmpty { item.director } ?: item.director,
                producer = enriched.producer?.ifEmpty { item.producer } ?: item.producer,
                duration = enriched.duration?.ifEmpty { item.duration } ?: item.duration,
                castJson = enriched.castJson?.ifEmpty { item.castJson } ?: item.castJson,
                imdbRating = enriched.imdbRating?.ifEmpty { item.imdbRating } ?: item.imdbRating,
                languages = enriched.languages?.ifEmpty { item.languages } ?: item.languages,
                subtitles = enriched.subtitles?.ifEmpty { item.subtitles } ?: item.subtitles,
                extraImagesJson = enriched.extraImagesJson?.ifEmpty { item.extraImagesJson } ?: item.extraImagesJson,
                description = enriched.description.ifEmpty { item.description },
                rating = enriched.rating.ifEmpty { item.rating },
                genre = enriched.genre.ifEmpty { item.genre }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext item
        }
    }
    /**
     * Loops through all catalogs, background-syncs and enriches their items.
     * This acts as the automated local catalog caching engine.
     */
    suspend fun autoSyncAndEnrichAll(): Boolean = withContext(Dispatchers.IO) {
        if (_isEnriching.value) return@withContext false
        _isEnriching.value = true

        try {
            val allCatalogs = repository.getAllCatalogs()
            val enrichedCatalogs = allCatalogs.map { catalog ->
                if (catalog.items.isEmpty()) return@map catalog

                val enrichedItems = catalog.items.map { item ->
                    enrichCatalogItem(item)
                }
                catalog.copy(items = enrichedItems)
            }
            
            // Save enriched items into our localized caches
            repository.saveCatalogsList(enrichedCatalogs)
            _isEnriching.value = false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _isEnriching.value = false
            false
        }
    }
}
