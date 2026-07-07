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
    val posterUrl: String = "",
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
    
    // TMDB raw paths from backend
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val logo_path: String? = null,
    val profile_path: String? = null
) {
    /**
     * Returns the full URL for the poster. 
     * Prioritizes poster_path if available.
     */
    fun getFullPosterUrl(size: String = "w500"): String {
        return if (!poster_path.isNullOrEmpty()) {
            "https://image.tmdb.org/t/p/$size$poster_path"
        } else if (posterUrl.startsWith("/")) {
             "https://image.tmdb.org/t/p/$size$posterUrl"
        } else {
            posterUrl
        }
    }

    /**
     * Returns the full URL for the backdrop.
     * Prioritizes backdrop_path if available.
     */
    fun getFullBackdropUrl(size: String = "original"): String {
        return if (!backdrop_path.isNullOrEmpty()) {
            "https://image.tmdb.org/t/p/$size$backdrop_path"
        } else if (backdropUrl != null && backdropUrl.startsWith("/")) {
            "https://image.tmdb.org/t/p/$size$backdropUrl"
        } else {
            backdropUrl ?: ""
        }
    }

    /**
     * Returns the full URL for the logo.
     * Prioritizes logo_path if available.
     */
    fun getFullLogoUrl(size: String = "w500"): String {
        return if (!logo_path.isNullOrEmpty()) {
            "https://image.tmdb.org/t/p/$size$logo_path"
        } else if (logoUrl != null && logoUrl.startsWith("/")) {
            "https://image.tmdb.org/t/p/$size$logoUrl"
        } else {
            logoUrl ?: ""
        }
    }

    /**
     * Returns the full URL for a profile image (actor photo).
     * Prioritizes profile_path if available.
     */
    fun getFullProfileUrl(size: String = "w185"): String {
        return if (!profile_path.isNullOrEmpty()) {
            "https://image.tmdb.org/t/p/$size$profile_path"
        } else {
            ""
        }
    }
}
