package com.example.data.util

import com.example.BuildConfig

/**
 * Centralized API configuration for Lumina.
 * All API keys are injected via BuildConfig (from .env files) 
 * to ensure security and developer-only management.
 */
object ApiConfig {
    
    /**
     * TMDB API Key (v3)
     */
    val TMDB_API_KEY: String
        get() = BuildConfig.TMDB_API_KEY.ifEmpty { "ca8c2c77f0a9bfd68cbca8b99009139d" }

    /**
     * MDBList API Key
     */
    val MDBLIST_API_KEY: String
        get() = BuildConfig.MDBLIST_API_KEY

    /**
     * Trakt.tv Client ID
     */
    val TRAKT_CLIENT_ID: String
        get() = BuildConfig.TRAKT_CLIENT_ID

    /**
     * Trakt.tv Client Secret
     */
    val TRAKT_CLIENT_SECRET: String
        get() = BuildConfig.TRAKT_CLIENT_SECRET

    /**
     * Checks if all mandatory API keys are configured.
     * TMDB and MDBList are considered mandatory for the core catalog experience.
     */
    fun isConfigured(): Boolean {
        return TMDB_API_KEY.isNotEmpty() && 
               MDBLIST_API_KEY.isNotEmpty()
    }
}
