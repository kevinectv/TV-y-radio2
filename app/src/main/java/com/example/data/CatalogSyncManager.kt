package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiagnosticInfo(
    val tmdbStatus: String = "🔴 Desconectado",
    val mdblistStatus: String = "🔴 Desconectado",
    val traktStatus: String = "🔴 Desconectado",
    val lastSyncTime: String = "Nunca",
    val errorsDetected: List<String> = emptyList(),
    val logs: List<String> = emptyList()
)

class CatalogSyncManager private constructor(private val context: Context) {

    private val _diagnostics = MutableStateFlow(DiagnosticInfo())
    val diagnostics: StateFlow<DiagnosticInfo> = _diagnostics

    private val mdbListService = MdbListService(context)
    private val tmdbService = TmdbService(context)
    private val traktService = TraktService(context)

    companion object {
        @Volatile
        private var INSTANCE: CatalogSyncManager? = null

        fun getInstance(context: Context): CatalogSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = CatalogSyncManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun addLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val formattedLog = "[$time] $message"
        val currentLogs = _diagnostics.value.logs.toMutableList()
        currentLogs.add(0, formattedLog)
        if (currentLogs.size > 200) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        _diagnostics.value = _diagnostics.value.copy(logs = currentLogs)
    }

    fun addError(error: String) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val currentErrors = _diagnostics.value.errorsDetected.toMutableList()
        currentErrors.add(0, "[$time] $error")
        if (currentErrors.size > 50) {
            currentErrors.removeAt(currentErrors.size - 1)
        }
        _diagnostics.value = _diagnostics.value.copy(errorsDetected = currentErrors)
    }

    suspend fun runDiagnosticCheck() {
        val prefs = context.getSharedPreferences("lumina_prefs", Context.MODE_PRIVATE)
        val tmdbKey = prefs.getString("tmdb_api_key", "") ?: ""
        val mdblistKey = prefs.getString("mdblist_api_key", "") ?: ""
        val traktKey = prefs.getString("trakt_api_key", "") ?: ""

        addLog("Iniciando autodiagnóstico de APIs...")

        val tmdbRes = tmdbService.testConnection(tmdbKey)
        val mdbRes = mdbListService.testConnection(mdblistKey)
        val traktRes = traktService.testConnection(traktKey)

        _diagnostics.value = _diagnostics.value.copy(
            tmdbStatus = if (tmdbRes.first) "🟢 Conectado" else "🔴 Desconectado (${tmdbRes.second})",
            mdblistStatus = if (mdbRes.first) "🟢 Conectado" else "🔴 Desconectado (${mdbRes.second})",
            traktStatus = if (traktRes.first) "🟢 Conectado" else "🔴 Desconectado (${traktRes.second})"
        )

        addLog("TMDB conectado: ${tmdbRes.first}")
        addLog("MDBList conectado: ${mdbRes.first}")
        addLog("Trakt conectado: ${traktRes.first}")
    }

    suspend fun performFullSync(repository: CatalogRepository) {
        addLog("Iniciando sincronización completa...")
        runDiagnosticCheck()
        
        try {
            val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            _diagnostics.value = _diagnostics.value.copy(lastSyncTime = timeString)
            
            addLog("MDBList descargado: procesando catálogos remotos...")
            repository.syncAll()
            addLog("IDs encontrados y vinculados correctamente.")

            addLog("TMDB conectado: enriqueciendo catálogo con Posters, Backdrops y Logos...")
            val success = repository.engine.autoSyncAndEnrichAll()
            
            if (success) {
                addLog("Posters descargados. Logos descargados. Actores descargados. Trailers descargados.")
                addLog("Home actualizado con éxito. Filas generadas correctamente.")
            } else {
                addLog("Enriquecimiento completado parcialmente.")
            }
        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Error desconocido"
            addError("Fallo de sincronización: $errMsg")
            addLog("Fallo de sincronización: $errMsg")
        }
    }
}
