package com.example.data.database

import androidx.room.Entity
import com.example.data.model.CatalogItem

@Entity(
    tableName = "catalog_items",
    primaryKeys = ["catalogId", "id"]
)
data class CatalogItemEntity(
    val catalogId: String,
    val id: String,
    val title: String,
    val posterUrl: String,
    val year: String,
    val rating: String,
    val genre: String,
    val description: String,
    val streamUrl: String?,
    val tmdbId: String?,
    val isTvShow: Boolean,
    val logoUrl: String?,
    val platform: String? = null,
    val platformLogo: String? = null,
    val backdropUrl: String?,
    val trailerUrl: String?,
    val director: String?,
    val producer: String?,
    val duration: String?,
    val castJson: String?,
    val imdbRating: String?,
    val languages: String?,
    val subtitles: String?,
    val extraImagesJson: String?,
    val country: String? = null,
    val classification: String? = null
)

fun CatalogItemEntity.toDomain(): CatalogItem {
    android.util.Log.d("LuminaFlow_EntityToDomain", "toDomain() mapped - Title: $title, Logo: $logoUrl, PlatformLogo: $platformLogo, Backdrop: $backdropUrl, Cast: $castJson")
    return CatalogItem(
        id = id,
        title = title,
        posterUrl = posterUrl,
        year = year,
        rating = rating,
        genre = genre,
        description = description,
        streamUrl = streamUrl,
        tmdbId = tmdbId,
        isTvShow = isTvShow,
        logoUrl = logoUrl,
        platform = platform,
        platformLogo = platformLogo,
        backdropUrl = backdropUrl,
        trailerUrl = trailerUrl,
        director = director,
        producer = producer,
        duration = duration,
        castJson = castJson,
        imdbRating = imdbRating,
        languages = languages,
        subtitles = subtitles,
        extraImagesJson = extraImagesJson,
        country = country,
        classification = classification
    )
}

fun CatalogItem.toEntity(catalogId: String): CatalogItemEntity {
    android.util.Log.d("LuminaFlow_DomainToEntity", "toEntity() mapped - Title: $title, Logo: $logoUrl, PlatformLogo: $platformLogo, Backdrop: $backdropUrl, Cast: $castJson")
    return CatalogItemEntity(
        catalogId = catalogId,
        id = id,
        title = title,
        posterUrl = posterUrl,
        year = year,
        rating = rating,
        genre = genre,
        description = description,
        streamUrl = streamUrl,
        tmdbId = tmdbId,
        isTvShow = isTvShow,
        logoUrl = logoUrl,
        platform = platform,
        platformLogo = platformLogo,
        backdropUrl = backdropUrl,
        trailerUrl = trailerUrl,
        director = director,
        producer = producer,
        duration = duration,
        castJson = castJson,
        imdbRating = imdbRating,
        languages = languages,
        subtitles = subtitles,
        extraImagesJson = extraImagesJson,
        country = country,
        classification = classification
    )
}
