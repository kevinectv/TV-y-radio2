package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.data.BackendApi

class TraktService(private val context: Context) {

    suspend fun testConnection(rawClientId: String? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val body = BackendApi.getInstance().getCatalogs()
            Pair(true, "🟢 Conectado exitosamente")
        } catch (e: Exception) {
            Pair(false, "Fallo: ${e.localizedMessage}")
        }
    }
}
