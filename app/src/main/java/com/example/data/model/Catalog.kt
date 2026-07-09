package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Catalog(
    val id: String,
    val name: String,
    val sourceType: String, // "MDBList", "Trakt", "Custom", "Local", "File", "TMDB"
    val url: String = "",
    val isVisible: Boolean = true,
    val showInHome: Boolean = true,
    val showInRecommendations: Boolean = true,
    val showInSearch: Boolean = true,
    val numItems: Int = 20,
    val status: String = "Sincronizado", // "Sincronizado", "Pendiente", "Error"
    val lastUpdated: String = "Hoy",
    var orderIndex: Int = 0,
    val layoutType: String = "Horizontal", // "Horizontal" or "Vertical"
    val items: List<CatalogItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CatalogItem(
    val id: String,
    val title: String,
    val posterUrl: String,
    val year: String = "2024",
    val rating: String = "8.2",
    val genre: String = "Acción",
    val description: String = "Una película espectacular llena de misterios y acción garantizada.",
    val streamUrl: String? = null,
    val tmdbId: String? = null,
    val isTvShow: Boolean = false,
    val logoUrl: String? = null,
    val backdropUrl: String? = null,
    val trailerUrl: String? = null,
    val director: String? = null,
    val producer: String? = null,
    val duration: String? = null,
    val castJson: String? = null,
    val imdbRating: String? = null,
    val languages: String? = null,
    val subtitles: String? = null,
    val extraImagesJson: String? = null,
    val country: String? = null,
    val classification: String? = null
)
