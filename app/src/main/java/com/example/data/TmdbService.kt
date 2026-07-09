package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.data.BackendApi

class TmdbService(private val context: Context) {

    suspend fun testConnection(rawApiKey: String? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val body = BackendApi.getInstance().getTrending()
            Pair(true, "🟢 Conectado exitosamente")
        } catch (e: Exception) {
            Pair(false, "Fallo: ${e.localizedMessage}")
        }
    }
}
