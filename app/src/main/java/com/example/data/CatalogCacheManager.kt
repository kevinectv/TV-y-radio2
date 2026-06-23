package com.example.data

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatalogCacheManager(private val context: Context) {

    fun getCacheSizeString(): String {
        val totalBytes = getFolderSize(context.cacheDir) + getFolderSize(File(context.filesDir, "installed_catalogs.json"))
        return formatSize(totalBytes)
    }

    private fun getFolderSize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        var size = 0L
        file.listFiles()?.forEach {
            size += getFolderSize(it)
        }
        return size
    }

    private fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1] + "B"
        return String.format(java.util.Locale.US, "%.2f %s", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }

    suspend fun clearCache(repository: CatalogRepository) = withContext(Dispatchers.IO) {
        val current = repository.catalogs.value
        val cleared = current.map { catalog ->
            val clearedItems = catalog.items.map { item ->
                item.copy(
                    backdropUrl = null,
                    logoUrl = null,
                    trailerUrl = null,
                    director = null,
                    producer = null,
                    castJson = null
                )
            }
            catalog.copy(items = clearedItems)
        }
        repository.saveCatalogsList(cleared)
        deleteFolderContents(context.cacheDir)
    }

    private fun deleteFolderContents(folder: File) {
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    deleteFolderContents(file)
                }
                file.delete()
            }
        }
    }
}
