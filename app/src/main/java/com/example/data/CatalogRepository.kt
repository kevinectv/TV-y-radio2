package com.example.data

import android.content.Context
import com.example.data.model.Catalog
import com.example.data.model.CatalogItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import com.example.data.database.toDomain
import com.example.data.database.toEntity

import com.example.data.util.ApiConfig

class CatalogRepository(private val context: Context) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val catalogListType = Types.newParameterizedType(List::class.java, Catalog::class.java)
    private val jsonAdapter = moshi.adapter<List<Catalog>>(catalogListType)
    private val catalogsFile = File(context.filesDir, "installed_catalogs.json")

    private val database = com.example.data.database.AppDatabase.getDatabase(context)
    private val catalogDao = database.catalogDao()

    private val _catalogs = MutableStateFlow<List<Catalog>>(emptyList())
    val catalogs: StateFlow<List<Catalog>> = _catalogs
    val engine by lazy { LuminaCatalogEngine(context, this) }

    // Expose stats states
    private val _lastSyncTime = MutableStateFlow<String>("Nunca")
    val lastSyncTime: StateFlow<String> = _lastSyncTime

    private val _storedItemsCount = MutableStateFlow<Int>(0)
    val storedItemsCount: StateFlow<Int> = _storedItemsCount

    private val _cacheSize = MutableStateFlow<String>("0.00 B")
    val cacheSize: StateFlow<String> = _cacheSize

    init {
        // Run any disk operations (loadCatalogs) and sync asynchronously so we don't block the main thread on startup
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            loadCatalogs()
            
            var needsSave = false
            val current = _catalogs.value.map { cat ->
                val hasNoDefaultMock = cat.url.isNotEmpty() && 
                    !cat.url.contains("lumina.app/catalogs") && 
                    !cat.url.contains("api.themoviedb.org/3/catalog/") &&
                    !cat.url.contains("mdblist.com/lists/public/") &&
                    !cat.url.contains("api.trakt.tv/lists/")
                
                if (cat.items.isEmpty() && cat.url.startsWith("http") && hasNoDefaultMock) {
                    needsSave = true
                    val result = fetchItemsForCatalog(cat)
                    cat.copy(items = result.items, status = result.status, lastUpdated = "Al iniciar")
                } else cat
            }
            if (needsSave) {
                saveCatalogsToDbSync(current)
            }
            
            val totalItems = catalogDao.getStoredItemsCount()
            if (totalItems <= 140) {
                refreshLocalCatalogs()
            }
            
            try {
                engine.autoSyncAndEnrichAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            updateStats()
        }
    }

    private suspend fun loadCatalogs() {
        try {
            val dbCatalogs = catalogDao.getAllCatalogsList()
            if (dbCatalogs.isNotEmpty()) {
                val domainList = dbCatalogs.map { entity ->
                    val itemEntities = catalogDao.getItemsForCatalog(entity.id)
                    val domainItems = itemEntities.map { it.toDomain() }
                    entity.toDomain(domainItems)
                }
                _catalogs.value = domainList.sortedBy { it.orderIndex }
                updateStats()
                return
            }
            
            // If DB is empty, populate default catalogs
            val defaults = createDefaultCatalogs()
            saveCatalogsToDbSync(defaults)
        } catch (e: Exception) {
            e.printStackTrace()
            val defaults = createDefaultCatalogs()
            _catalogs.value = defaults
        }
    }

    private suspend fun saveCatalogsToDbSync(list: List<Catalog>) = withContext(Dispatchers.IO) {
        val sortedList = list.mapIndexed { idx, catalog -> catalog.copy(orderIndex = idx) }
        val catalogEntities = sortedList.map { it.toEntity() }
        catalogDao.insertCatalogs(catalogEntities)
        
        for (catalog in sortedList) {
            val itemEntities = catalog.items.map { it.toEntity(catalog.id) }
            catalogDao.deleteItemsForCatalog(catalog.id)
            catalogDao.insertCatalogItems(itemEntities)
        }
        _catalogs.value = sortedList
        updateStats()
    }

    fun saveCatalogsList(list: List<Catalog>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                saveCatalogsToDbSync(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateStats() {
        CoroutineScope(Dispatchers.IO).launch {
            val count = catalogDao.getStoredItemsCount()
            _storedItemsCount.value = count
            
            val prefs = context.getSharedPreferences("lumina_prefs", Context.MODE_PRIVATE)
            val lastSync = prefs.getString("last_catalog_sync", "Nunca") ?: "Nunca"
            _lastSyncTime.value = lastSync
            
            val totalBytes = getFolderSize(context.cacheDir) + getFolderSize(context.codeCacheDir) + getFolderSize(File(context.filesDir, "../databases"))
            _cacheSize.value = formatSize(totalBytes)
        }
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

    private fun saveLastSyncTime() {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        val nowStr = sdf.format(java.util.Date())
        val prefs = context.getSharedPreferences("lumina_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_catalog_sync", nowStr).apply()
        updateStats()
    }

    fun getAllCatalogs(): List<Catalog> = _catalogs.value

    suspend fun addCatalog(catalog: Catalog): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.toMutableList()
        val index = current.size
        val result = fetchItemsForCatalog(catalog)
        val newCatalog = catalog.copy(
            id = if (catalog.id.isEmpty()) UUID.randomUUID().toString() else catalog.id,
            orderIndex = index,
            items = result.items,
            status = result.status,
            lastUpdated = result.lastUpdated
        )
        current.add(newCatalog)
        saveCatalogsList(current)
        true
    }

    suspend fun updateCatalog(updated: Catalog): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.map {
            if (it.id == updated.id) {
                val urlChanged = it.url != updated.url
                val itemsEmpty = updated.items.isEmpty()
                val (realItems, status, lastUpdated) = if (urlChanged || itemsEmpty) {
                    val res = fetchItemsForCatalog(updated)
                    val items = if (res.items.isEmpty() && updated.items.isNotEmpty()) updated.items else res.items
                    Triple(items, res.status, res.lastUpdated)
                } else {
                    Triple(updated.items, "Sincronizado", "Ahora mismo")
                }
                updated.copy(
                    items = realItems,
                    status = status,
                    lastUpdated = lastUpdated
                )
            } else it
        }
        saveCatalogsList(current)
        true
    }

    suspend fun deleteCatalog(id: String): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.filter { it.id != id }
        saveCatalogsList(current)
        true
    }

    suspend fun moveUp(id: String): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index > 0) {
            val temp = current[index]
            current[index] = current[index - 1]
            current[index - 1] = temp
            saveCatalogsList(current)
            true
        } else false
    }

    suspend fun moveDown(id: String): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1 && index < current.size - 1) {
            val temp = current[index]
            current[index] = current[index + 1]
            current[index + 1] = temp
            saveCatalogsList(current)
            true
        } else false
    }

    suspend fun syncNow(id: String): Boolean = withContext(Dispatchers.IO) {
        val updated = _catalogs.value.map {
            if (it.id == id) {
                val result = fetchItemsForCatalog(it)
                val updatedItems = if (result.items.isEmpty() && it.items.isNotEmpty()) it.items else result.items
                it.copy(
                    status = result.status,
                    lastUpdated = result.lastUpdated,
                    items = updatedItems
                )
            } else it
        }
        saveCatalogsList(updated)
        saveLastSyncTime()
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                engine.autoSyncAndEnrichAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        true
    }

    suspend fun syncAll(): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.map {
            val result = fetchItemsForCatalog(it)
            val updatedItems = if (result.items.isEmpty() && it.items.isNotEmpty()) it.items else result.items
            it.copy(
                status = result.status,
                lastUpdated = result.lastUpdated,
                items = updatedItems
            )
        }
        saveCatalogsList(current)
        saveLastSyncTime()
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                engine.autoSyncAndEnrichAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        true
    }

    suspend fun refreshLocalCatalogs(force: Boolean = false) = withContext(Dispatchers.IO) {
        android.util.Log.d("CatalogRepository", "Starting refreshLocalCatalogs, force: $force")
        try {
            // Priority 1: Sync from Lumina Home Backend
            android.util.Log.d("CatalogRepository", "Calling LuminaApi.service.getHome()")
            val homeCatalogsMap = LuminaApi.service.getHome()
            android.util.Log.d("CatalogRepository", "Received homeCatalogsMap, size: ${homeCatalogsMap.size}")
            
            if (homeCatalogsMap.isNotEmpty()) {
                val current = _catalogs.value.toMutableList()
                
                homeCatalogsMap.forEach { (key, items) ->
                    android.util.Log.d("CatalogRepository", "Processing category: $key, items: ${items.size}")
                    val existingIdx = current.indexOfFirst { it.name == key || it.id == key }
                    if (existingIdx != -1) {
                        current[existingIdx] = current[existingIdx].copy(
                            items = items.toList(), // Ensure a copy
                            status = "Sincronizado",
                            lastUpdated = "Ahora"
                        )
                    } else {
                        current.add(Catalog(
                            id = key,
                            name = key,
                            sourceType = "Lumina",
                            items = items.toList(),
                            orderIndex = current.size
                        ))
                    }
                }
                android.util.Log.d("CatalogRepository", "Saving ${current.size} catalogs to DB")
                saveCatalogsToDbSync(current)
            } else {
                android.util.Log.w("CatalogRepository", "homeCatalogsMap is empty")
            }
        } catch (e: Exception) {
            android.util.Log.e("CatalogRepository", "Error in refreshLocalCatalogs", e)
        }

        val current = _catalogs.value
        if (current.isEmpty()) return@withContext

        // Map through each catalog and either update synchronously or trigger background synchronization
        val updated = current.map { cat ->
            val isCustomUrl = cat.url.isNotEmpty() && !cat.url.contains("lumina.app") && cat.url.startsWith("http", ignoreCase = true)
            if (cat.sourceType == "Local" || cat.sourceType == "Custom" || isCustomUrl || cat.url.isEmpty()) {
                if (cat.items.isNotEmpty() && !force) {
                    // Smart background synchronization: don't block, fetch and update only if items have modified/new metadata
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val result = fetchItemsForCatalog(cat)
                            if (result.items.isNotEmpty()) {
                                val currentItemIds = cat.items.map { it.id }.toSet()
                                val newItemIds = result.items.map { it.id }.toSet()
                                
                                val hasChanges = result.items.any { newItem ->
                                    val existing = cat.items.find { it.id == newItem.id }
                                    existing == null || existing.title != newItem.title || existing.posterUrl != newItem.posterUrl
                                } || (newItemIds != currentItemIds)
                                
                                if (hasChanges) {
                                    val updatedCat = cat.copy(
                                        items = result.items,
                                        status = result.status,
                                        lastUpdated = result.lastUpdated
                                    )
                                    val updatedList = _catalogs.value.map { if (it.id == cat.id) updatedCat else it }
                                    saveCatalogsToDbSync(updatedList)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    cat // return current immediately
                } else {
                    // Fetch synchronously if empty or forced
                    val result = fetchItemsForCatalog(cat)
                    val updatedItems = if (result.items.isEmpty() && cat.items.isNotEmpty()) cat.items else result.items
                    cat.copy(items = updatedItems, status = result.status, lastUpdated = result.lastUpdated)
                }
            } else cat
        }
        
        // If any catalogs were updated synchronously (e.g. empty ones or forced ones), save them
        if (updated != current) {
            saveCatalogsToDbSync(updated)
        }
    }

    private fun createDefaultCatalogs(): List<Catalog> {
        val categories = listOf(
            Triple("Películas en Tendencia", "Lumina", "/api/trending"),
            Triple("Series en Tendencia", "Lumina", "/api/trending"),
            Triple("Populares", "Lumina", "/api/home"),
            Triple("Estrenos", "Lumina", "/api/home"),
            Triple("Anime", "Lumina", "/api/home"),
            Triple("Acción", "Lumina", "/api/home"),
            Triple("Comedia", "Lumina", "/api/home"),
            Triple("Terror", "Lumina", "/api/home"),
            Triple("Ciencia Ficción", "Lumina", "/api/home")
        )
        val baseUrl = "https://lumina-api-coral.vercel.app"
        return categories.mapIndexed { idx, triple ->
            val (name, src, path) = triple
            Catalog(
                id = "lumina_home_${idx + 1}",
                name = name,
                sourceType = src,
                url = if (path.startsWith("http")) path else "$baseUrl/${path.removePrefix("/")}",
                isVisible = true,
                showInHome = true,
                showInRecommendations = idx % 3 == 0,
                showInSearch = true,
                numItems = 20,
                status = "Sincronizado",
                lastUpdated = "Hoy",
                orderIndex = idx,
                layoutType = "Horizontal Poster Row"
            )
        }
    }

data class SyncResult(
    val items: List<CatalogItem>,
    val status: String,
    val lastUpdated: String
)

    private suspend fun fetchItemsForCatalog(catalog: Catalog): SyncResult = withContext(Dispatchers.IO) {
        android.util.Log.d("CatalogRepository", "Fetching for catalog: ${catalog.name}, URL: ${catalog.url}")
        val list = mutableListOf<CatalogItem>()
        var status = "Sincronizado"
        val lastUpdated = "Recién Recargado"

        val rawUrl = catalog.url.trim()
        
        // --- REDIRECTION TO LUMINA BACKEND ---
        if (catalog.sourceType == "Lumina" || rawUrl.contains("lumina-api-coral.vercel.app")) {
            try {
                val items = when {
                    rawUrl.contains("/home") -> {
                        val homeCatalogsMap = LuminaApi.service.getHome()
                        
                        // Convert Map<String, List<CatalogItem>> to Catalog objects
                        val homeCatalogs = homeCatalogsMap.map { (key, items) ->
                            Catalog(
                                id = key,
                                name = key,
                                sourceType = "Lumina",
                                items = items
                            )
                        }
                        
                        android.util.Log.d("CatalogRepository", "Fetching /home, found ${homeCatalogs.size} catalogs")
                        
                        val foundCatalog = homeCatalogs.find { 
                            it.name.equals(catalog.name, ignoreCase = true) || it.id == catalog.id 
                        } ?: homeCatalogs.maxByOrNull {
                            val n1 = it.name.lowercase().trim()
                            val n2 = catalog.name.lowercase().trim()
                            if (n1 == n2) 100
                            else if (n1.contains(n2) || n2.contains(n1)) 50
                            else 0
                        }
                        
                        val finalCatalog = if (foundCatalog != null) {
                             val n1 = foundCatalog.name.lowercase().trim()
                             val n2 = catalog.name.lowercase().trim()
                             if (n1 == n2 || n1.contains(n2) || n2.contains(n1)) foundCatalog else null
                        } else null
                        
                        android.util.Log.d("CatalogRepository", "Catalog ${catalog.name} matched with ${finalCatalog?.name ?: "null"}, items size: ${finalCatalog?.items?.size ?: 0}")
                        finalCatalog?.items?.toList() ?: emptyList()
                    }
                    rawUrl.contains("/trending") -> {
                        val trending = LuminaApi.service.getTrending()
                        val result = if (catalog.name.lowercase().contains("serie") || catalog.name.lowercase().contains("tv")) {
                            trending.filter { it.isTvShow }
                        } else if (catalog.name.lowercase().contains("película") || catalog.name.lowercase().contains("pelicula") || catalog.name.lowercase().contains("movie")) {
                            trending.filter { !it.isTvShow }
                        } else {
                            trending
                        }
                        android.util.Log.d("CatalogRepository", "Fetched ${result.size} items for ${catalog.name} from /trending")
                        result.toList()
                    }
                    rawUrl.contains("/catalogs") -> {
                        val allCatalogs = LuminaApi.service.getCatalogs()
                        val found = allCatalogs.find { it.name == catalog.name || it.id == catalog.id }
                        android.util.Log.d("CatalogRepository", "Fetched ${found?.items?.size ?: 0} items for ${catalog.name} from /catalogs")
                        found?.items?.toList() ?: emptyList()
                    }
                    rawUrl.contains("/mdblist") -> {
                        val mdbId = rawUrl.substringAfter("id=").substringBefore("&").substringBefore("?")
                        if (mdbId.isNotEmpty() && mdbId != rawUrl) {
                            LuminaApi.service.getMdbList(mdbId)
                        } else {
                            emptyList()
                        }
                    }
                    else -> emptyList()
                }
                android.util.Log.d("CatalogRepository", "Fetched ${items.size} items for ${catalog.name}")
                if (items.isNotEmpty()) {
                    return@withContext SyncResult(items, "Sincronizado", "Ahora")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("CatalogRepository", "Error fetching for ${catalog.name}: ${e.message}")
            }
        }

        val tmdbKey = ApiConfig.TMDB_API_KEY
        val traktKey = ApiConfig.TRAKT_CLIENT_ID
        val mdblistKey = ApiConfig.MDBLIST_API_KEY
        
        var cleanUrl = rawUrl
        if (cleanUrl.contains("INSERT_KEY_HERE")) {
            if (tmdbKey.isNotEmpty() && cleanUrl.contains("api.themoviedb.org")) {
                if (tmdbKey.startsWith("ey")) {
                    cleanUrl = cleanUrl.replace("api_key=INSERT_KEY_HERE", "").replace("&&", "&").replace("?&", "?").trimEnd('&', '?')
                } else {
                    cleanUrl = cleanUrl.replace("INSERT_KEY_HERE", tmdbKey)
                }
            }
        }
        
        // Auto-inject MDBList API key if missing but they provided the base API endpoint
        if (cleanUrl.contains("mdblist.com/api") && mdblistKey.isNotEmpty() && !cleanUrl.contains("apikey=")) {
            cleanUrl = if (cleanUrl.contains("?")) "$cleanUrl&apikey=$mdblistKey" else "$cleanUrl/?apikey=$mdblistKey"
        }
        
        // Auto-inject JSON endpoint for mdblist public lists
        if (cleanUrl.startsWith("https://mdblist.com/lists/") && !cleanUrl.contains("/json") && !cleanUrl.contains("/api")) {
            cleanUrl = cleanUrl.removeSuffix("/") + "/json"
        }
        
        // 1. Try to fetch from HTTP URL if specified and non-default
        if (cleanUrl.isNotEmpty() && 
            cleanUrl.startsWith("http", ignoreCase = true) && 
            !cleanUrl.contains("/default_") && 
            !cleanUrl.contains("lumina.app/catalogs/genre_")) {
            
            // If the user hasn't provided a TMDB key, but we are querying TMDB using the placeholder,
            // we should not execute the request to avoid 401 Unauthorized crashes, we just return empty.
            if (cleanUrl.contains("INSERT_KEY_HERE")) {
                return@withContext SyncResult(emptyList(), "Falta API Key", "Sin clave")
            }
            
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                    
                var reqBuilder = okhttp3.Request.Builder().url(cleanUrl)
                reqBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                
                if (cleanUrl.contains("trakt.tv") && traktKey.isNotEmpty()) {
                    reqBuilder.header("trakt-api-version", "2")
                    reqBuilder.header("trakt-api-key", traktKey)
                }
                
                if (cleanUrl.contains("api.themoviedb.org") && tmdbKey.startsWith("ey")) {
                    reqBuilder.header("Authorization", "Bearer $tmdbKey")
                }
                
                val request = reqBuilder.build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: ""
                        val cleanedBody = bodyString.replace("\uFEFF", "").trim()
                        if (cleanedBody.startsWith("#EXTM3U") || cleanedBody.contains("#EXTINF:")) {
                            // It's an M3U file
                            val m3uItems = parseM3uCatalog(cleanedBody, catalog.id)
                            if (m3uItems.isNotEmpty()) {
                                list.addAll(m3uItems)
                            }
                        } else if (cleanedBody.startsWith("{") || cleanedBody.startsWith("[")) {
                            // It's JSON (TMDB or Trakt or Custom API)
                            val jsonItems = parseJsonCatalog(cleanedBody, catalog)
                            if (jsonItems.isNotEmpty()) {
                                list.addAll(jsonItems)
                            }
                        } else {
                            if (cleanedBody.contains("#EXTINF:")) {
                                val m3uItems = parseM3uCatalog(cleanedBody, catalog.id)
                                if (m3uItems.isNotEmpty()) {
                                    list.addAll(m3uItems)
                                }
                            } else if (cleanedBody.contains("{") || cleanedBody.contains("[")) {
                                val jsonItems = parseJsonCatalog(cleanedBody, catalog)
                                if (jsonItems.isNotEmpty()) {
                                    list.addAll(jsonItems)
                                }
                            }
                        }
                        status = "Sincronizado"
                    } else {
                        status = when (response.code) {
                            401 -> "Error 401: No autorizado (Verifica tu clave)"
                            403 -> "Error 403: Prohibido / Sin acceso"
                            404 -> "Error 404: No encontrado"
                            else -> "Error HTTP: ${response.code}"
                        }
                    }
                }
            } catch (e: java.net.UnknownHostException) {
                status = "Sin conexión (Host desconocido)"
            } catch (e: java.net.SocketTimeoutException) {
                status = "Tiempo de espera agotado"
            } catch (e: Exception) {
                status = "Error: ${e.localizedMessage ?: "Fallo"}"
                e.printStackTrace()
            }
        }

        // 2. Query matching local channels (Disabled as requested to keep local IPTV streams separated from general Movie/VOD Home Screen catalogs)
        val dbItems = emptyList<CatalogItem>()

        // Merge results: URL results take priority
        val rawMergedList = (list + dbItems).distinctBy { it.title.lowercase().trim() }
        
        // Smart Merge with existing items in DB to avoid losing enriched TMDB metadata
        val existingItemsMap = try {
            catalogDao.getItemsForCatalog(catalog.id).associateBy { it.id }
        } catch (e: Exception) {
            emptyMap()
        }
        val mergedList = rawMergedList.map { fetchedItem ->
            val existing = existingItemsMap[fetchedItem.id]
            if (existing != null) {
                fetchedItem.copy(
                    tmdbId = fetchedItem.tmdbId ?: existing.tmdbId,
                    logoUrl = fetchedItem.logoUrl ?: existing.logoUrl,
                    backdropUrl = fetchedItem.backdropUrl ?: existing.backdropUrl,
                    trailerUrl = fetchedItem.trailerUrl ?: existing.trailerUrl,
                    director = fetchedItem.director ?: existing.director,
                    producer = fetchedItem.producer ?: existing.producer,
                    duration = fetchedItem.duration ?: existing.duration,
                    castJson = fetchedItem.castJson ?: existing.castJson,
                    imdbRating = fetchedItem.imdbRating ?: existing.imdbRating,
                    languages = fetchedItem.languages ?: existing.languages,
                    subtitles = fetchedItem.subtitles ?: existing.subtitles,
                    extraImagesJson = fetchedItem.extraImagesJson ?: existing.extraImagesJson,
                    description = if (fetchedItem.description.startsWith("Una película espectacular") && existing.description.isNotEmpty()) existing.description else fetchedItem.description,
                    rating = if (fetchedItem.rating == "8.2" && existing.rating.isNotEmpty()) existing.rating else fetchedItem.rating,
                    genre = if (fetchedItem.genre == catalog.name && existing.genre.isNotEmpty()) existing.genre else fetchedItem.genre,
                    year = if (fetchedItem.year == "2024" && existing.year.isNotEmpty()) existing.year else fetchedItem.year
                )
            } else {
                fetchedItem
            }
        }
        
        val finalItems = if (mergedList.isEmpty()) {
            getDeepFallbacksForCategory(catalog.name)
        } else {
            mergedList
        }
        
        SyncResult(
            items = finalItems.take(maxOf(catalog.numItems, 500)),
            status = status,
            lastUpdated = lastUpdated
        )
    }

    private fun parseM3uCatalog(m3uContent: String, catalogId: String): List<CatalogItem> {
        val list = mutableListOf<CatalogItem>()
        try {
            val lines = m3uContent.lines()
            var currentName = ""
            var currentLogo = ""
            var currentCategory = "Cine"
            var index = 1
            
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("#EXTINF:")) {
                    val tvgName = extractAttribute(trimmed, "tvg-name")
                    val lastComma = trimmed.lastIndexOf(',')
                    currentName = if (lastComma != -1) {
                        trimmed.substring(lastComma + 1).trim()
                    } else {
                        tvgName.ifEmpty { "Película $index" }
                    }
                    if (currentName.isEmpty() && tvgName.isNotEmpty()) {
                        currentName = tvgName
                    }
                    
                    currentLogo = extractAttribute(trimmed, "tvg-logo")
                    currentCategory = extractAttribute(trimmed, "group-title")
                } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    if (currentName.isNotEmpty()) {
                        val yearRegex = Regex("\\b(19|20)\\d{2}\\b")
                        val yearMatch = yearRegex.find(currentName)
                        val year = yearMatch?.value ?: "2024"
                        val cleanTitle = currentName.replace(Regex("\\s*[(]?\\b(19|20)\\d{2}\\b[)]?\\s*"), "").trim()
                        
                        list.add(
                            CatalogItem(
                                id = "${catalogId}_u_$index",
                                title = cleanTitle,
                                posterUrl = currentLogo.ifEmpty { "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=300" },
                                year = year,
                                rating = String.format(java.util.Locale.US, "%.1f", 7.0 + (currentName.hashCode().coerceAtLeast(0) % 25) / 10.0),
                                genre = currentCategory.ifEmpty { "General" },
                                description = "Transmisión premium sintonizada desde enlace remoto.",
                                streamUrl = trimmed
                            )
                        )
                        index++
                    }
                    currentName = ""
                    currentLogo = ""
                    currentCategory = "Cine"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun extractAttribute(line: String, attrName: String): String {
        val key = "$attrName="
        val index = line.indexOf(key)
        if (index == -1) return ""
        val start = index + key.length
        if (start >= line.length) return ""
        val quoteChar = line[start]
        return if (quoteChar == '"' || quoteChar == '\'') {
            val end = line.indexOf(quoteChar, start + 1)
            if (end != -1) {
                line.substring(start + 1, end)
            } else ""
        } else {
            val space = line.indexOf(' ', start)
            if (space != -1) {
                line.substring(start, space)
            } else {
                line.substring(start)
            }
        }
    }

    private fun parseJsonCatalog(jsonStr: String, catalog: Catalog): List<CatalogItem> {
        val list = mutableListOf<CatalogItem>()
        try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("{")) {
                val root = org.json.JSONObject(jsonStr)
                
                // Case 1: TMDB response typically has "results" array
                if (root.has("results")) {
                    val arr = root.getJSONArray("results")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val title = obj.optString("title").ifEmpty { obj.optString("name", "Película Real") }
                        val path = obj.optString("poster_path").ifEmpty { obj.optString("backdrop_path", "") }
                        val poster = if (path.isNotEmpty()) {
                            if (path.startsWith("http")) path else "https://image.tmdb.org/t/p/w500$path"
                        } else {
                            "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=300"
                        }
                        
                        val date = obj.optString("release_date").ifEmpty { obj.optString("first_air_date", "2024") }
                        val year = if (date.length >= 4) date.substring(0, 4) else "2024"
                        val vote = obj.optDouble("vote_average", 8.2)
                        val rating = String.format(java.util.Locale.US, "%.1f", vote)
                        val desc = obj.optString("overview", "Un emocionante viaje cinematográfico real en Lumina.")
                        
                        val tmdbId = obj.optString("id")
                        val isTvShow = obj.optString("media_type") == "tv" || obj.has("first_air_date") || (obj.has("name") && !obj.has("title"))
                        
                        list.add(
                            CatalogItem(
                                id = "${catalog.id}_t_${obj.optString("id", i.toString())}",
                                title = title,
                                posterUrl = poster,
                                year = year,
                                rating = rating,
                                genre = catalog.name,
                                description = desc,
                                tmdbId = if (tmdbId.isNotEmpty()) tmdbId else null,
                                isTvShow = isTvShow
                            )
                        )
                    }
                } else {
                    // Try to scan for any array inside the root object and parse list of candidates
                    var bestList = mutableListOf<CatalogItem>()
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = root.opt(key)
                        if (value is org.json.JSONArray) {
                            val items = parseGenericJsonArray(value, catalog)
                            if (items.size > bestList.size) {
                                bestList = items.toMutableList()
                            }
                        } else if (value is org.json.JSONObject) {
                            // Sometime it is nested under root -> "data" -> "movies" array
                            val nestedKeys = value.keys()
                            while (nestedKeys.hasNext()) {
                                val nKey = nestedKeys.next()
                                val nValue = value.opt(nKey)
                                if (nValue is org.json.JSONArray) {
                                    val items = parseGenericJsonArray(nValue, catalog)
                                    if (items.size > bestList.size) {
                                        bestList = items.toMutableList()
                                    }
                                }
                            }
                        }
                    }
                    if (bestList.isNotEmpty()) {
                        return bestList
                    }
                }
            } else if (trimmed.startsWith("[")) {
                val arr = org.json.JSONArray(trimmed)
                return parseGenericJsonArray(arr, catalog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun parseGenericJsonArray(arr: org.json.JSONArray, catalog: Catalog): List<CatalogItem> {
        val list = mutableListOf<CatalogItem>()
        for (i in 0 until arr.length()) {
            try {
                val obj = arr.getJSONObject(i)
                val title = obj.optString("title").ifEmpty {
                    obj.optString("name").ifEmpty {
                        if (obj.has("movie")) {
                            val movie = obj.optJSONObject("movie")
                            movie?.optString("title") ?: ""
                        } else if (obj.has("show")) {
                            val show = obj.optJSONObject("show")
                            show?.optString("title") ?: ""
                        } else "Película ${i + 1}"
                    }
                }
                if (title.isEmpty()) continue
                
                var poster = ""
                listOf("poster", "poster_url", "posterUrl", "poster_path", "image", "image_url", "imageUrl", "cover", "cover_url", "coverUrl", "thumbnail", "thumbnail_url", "logo").forEach { key ->
                    if (obj.has(key) && obj.optString(key).isNotEmpty()) {
                        poster = obj.optString(key)
                        return@forEach
                    }
                }
                if (poster.isEmpty() && obj.has("movie")) {
                    val movie = obj.optJSONObject("movie")
                    if (movie != null) {
                        listOf("poster", "poster_url", "poster_path", "image", "cover", "thumbnail").forEach { key ->
                            if (movie.has(key) && movie.optString(key).isNotEmpty()) {
                                poster = movie.optString(key)
                                return@forEach
                            }
                        }
                    }
                }
                if (poster.isEmpty() && obj.has("show")) {
                    val show = obj.optJSONObject("show")
                    if (show != null) {
                        listOf("poster", "poster_url", "poster_path", "image", "cover", "thumbnail").forEach { key ->
                            if (show.has(key) && show.optString(key).isNotEmpty()) {
                                poster = show.optString(key)
                                return@forEach
                            }
                        }
                    }
                }
                
                // Smart TMDB Identification to fetch rich visual assets
                var logoPath = obj.optString("logo_path").ifEmpty { obj.optString("logo_url", "") }
                var tmdbId = ""
                if (obj.has("tmdb_id") && obj.optString("tmdb_id").isNotEmpty() && obj.optString("tmdb_id") != "null") {
                    tmdbId = obj.optString("tmdb_id")
                } else if (obj.has("tmdbId") && obj.optString("tmdbId").isNotEmpty() && obj.optString("tmdbId") != "null") {
                    tmdbId = obj.optString("tmdbId")
                } else if (obj.has("tmdbid") && obj.optString("tmdbid").isNotEmpty() && obj.optString("tmdbid") != "null") {
                    tmdbId = obj.optString("tmdbid")
                } else if (obj.has("movie")) {
                    val movieObj = obj.optJSONObject("movie")
                    if (movieObj != null) {
                        if (movieObj.has("ids")) {
                            val ids = movieObj.optJSONObject("ids")
                            if (ids != null && ids.has("tmdb")) {
                                tmdbId = ids.optString("tmdb")
                            }
                        } else if (movieObj.has("tmdb_id")) {
                            tmdbId = movieObj.optString("tmdb_id")
                        } else if (movieObj.has("tmdbid")) {
                            tmdbId = movieObj.optString("tmdbid")
                        }
                    }
                } else if (obj.has("show")) {
                    val showObj = obj.optJSONObject("show")
                    if (showObj != null) {
                        if (showObj.has("ids")) {
                            val ids = showObj.optJSONObject("ids")
                            if (ids != null && ids.has("tmdb")) {
                                tmdbId = ids.optString("tmdb")
                            }
                        } else if (showObj.has("tmdb_id")) {
                            tmdbId = showObj.optString("tmdb_id")
                        } else if (showObj.has("tmdbid")) {
                            tmdbId = showObj.optString("tmdbid")
                        }
                    }
                }
                
                var imdbId = ""
                if (obj.has("imdb_id") && obj.optString("imdb_id").isNotEmpty() && obj.optString("imdb_id") != "null") {
                    imdbId = obj.optString("imdb_id")
                } else if (obj.has("imdbid") && obj.optString("imdbid").isNotEmpty() && obj.optString("imdbid") != "null") {
                    imdbId = obj.optString("imdbid")
                }
                
                if (poster.isEmpty() && tmdbId.isNotEmpty() && tmdbId != "null" && tmdbId != "0") {
                    // Try to construct a high quality poster using TMDB. Without secret keys we can't fetch the paths dynamically,
                    // but there are some public visual proxy services. 
                    // To ensure reliability we will keep relying on generic placeholders if real proxies fail.
                    // For now, TMDB IDs don't guarantee images without paths, so we will use OMDB or fallback
                }
                if (poster.isEmpty() && imdbId.isNotEmpty() && imdbId != "null") {
                    poster = "https://images.metahub.space/poster/medium/$imdbId/img"
                }
                
                if (poster.isNotEmpty() && !poster.startsWith("http") && poster.startsWith("/")) {
                    poster = "https://image.tmdb.org/t/p/w500$poster"
                }
                if (poster.isEmpty()) {
                    poster = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=300"
                }
                
                var year = obj.optString("year").ifEmpty {
                    obj.optString("release_year").ifEmpty {
                        obj.optString("releaseYear").ifEmpty {
                            if (obj.has("release_date")) {
                                obj.optString("release_date")
                            } else if (obj.has("movie")) {
                                val mObj = obj.optJSONObject("movie")
                                mObj?.optString("year") ?: ""
                            } else if (obj.has("show")) {
                                val sObj = obj.optJSONObject("show")
                                sObj?.optString("year") ?: ""
                            } else {
                                "2024"
                            }
                        }
                    }
                }
                if (year.isEmpty()) {
                    year = "2024"
                }
                if (year.length > 4) {
                    year = year.take(4)
                }
                
                val rating = obj.optString("rating").ifEmpty {
                    val vote = obj.optDouble("vote_average", 8.0)
                    String.format(java.util.Locale.US, "%.1f", vote)
                }
                
                val desc = obj.optString("description").ifEmpty {
                    obj.optString("overview").ifEmpty {
                        obj.optString("summary").ifEmpty {
                            obj.optString("plot").ifEmpty {
                                if (obj.has("movie")) {
                                    val mObj = obj.optJSONObject("movie")
                                    val ov = mObj?.optString("overview") ?: ""
                                    if (ov.isNotEmpty()) ov else "Disfruta de este magnífico largometraje disponible en Lumina con la mejor calidad de sonido e imagen del sector cinematográfico."
                                } else if (obj.has("show")) {
                                    val sObj = obj.optJSONObject("show")
                                    val ov = sObj?.optString("overview") ?: ""
                                    if (ov.isNotEmpty()) ov else "Sigue los episodios de tu serie de televisión preferida en alta definición y sin interrupciones con Lumina."
                                } else {
                                    "Descubre una experiencia de entretenimiento increíble con este título cuidadosamente seleccionado para el catálogo premium de Lumina."
                                }
                            }
                        }
                    }
                }
                
                var streamUrl = ""
                listOf("url", "stream_url", "streamUrl", "file", "link", "source", "stream", "uri", "video", "playback_url", "m3u8").forEach { key ->
                    if (obj.has(key) && obj.optString(key).isNotEmpty()) {
                        streamUrl = obj.optString(key)
                        return@forEach
                    }
                }
                
                val isTvShow = obj.optString("type") == "show" || obj.has("show") || obj.has("first_air_date") || (obj.has("name") && !obj.has("title")) || obj.optString("media_type") == "tv"
                
                list.add(
                    CatalogItem(
                        id = "${catalog.id}_g_${i}",
                        title = title,
                        posterUrl = poster,
                        year = year,
                        rating = rating,
                        genre = catalog.name,
                        description = desc,
                        streamUrl = if (streamUrl.isNotEmpty()) streamUrl else null,
                        tmdbId = if (tmdbId.isNotEmpty() && tmdbId != "null") tmdbId else null,
                        isTvShow = isTvShow,
                        logo_path = logoPath.ifEmpty { null }
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list
    }

    private fun getDeepFallbacksForCategory(categoryName: String): List<CatalogItem> {
        val nameL = categoryName.lowercase()
        return when {
            nameL.contains("marvel") -> {
                listOf(
                    CatalogItem(
                        id = "m_avengers_endgame", title = "Avengers: Endgame",
                        posterUrl = "https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=600",
                        year = "2019", rating = "8.4", genre = "Acción / Sci-Fi / Aventura",
                        description = "Tras los eventos devastadores de Avengers: Infinity War, el universo está en ruinas. Con la ayuda de los aliados restantes, los Avengers se reúnen una vez más para deshacer las acciones de Thanos y restaurar el orden en el universo."
                    ),
                    CatalogItem(
                        id = "m_spiderman_nwh", title = "Spider-Man: No Way Home",
                        posterUrl = "https://images.unsplash.com/photo-1608889175123-8ec330b86f84?q=80&w=600",
                        year = "2021", rating = "8.2", genre = "Acción / Aventura / Fantasía",
                        description = "Por primera vez en la historia cinematográfica de Spider-Man, nuestro amigable héroe vecino es desenmascarado y ya no puede separar su vida normal de los altos riesgos de ser un superhéroe. Cuando pide ayuda al Doctor Strange, los riesgos se vuelven aún más peligrosos."
                    ),
                    CatalogItem(
                        id = "m_guardians_3", title = "Guardianes de la Galaxia Vol. 3",
                        posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=600",
                        year = "2023", rating = "8.0", genre = "Acción / Sci-Fi / Comedia",
                        description = "Nuestro querido grupo de inadaptados se está instalando en Knowhere. Pero no pasa mucho tiempo antes de que sus vidas se vean alteradas por los ecos del turbulento pasado de Rocket."
                    ),
                    CatalogItem(
                        id = "m_ironman", title = "Iron Man",
                        posterUrl = "https://images.unsplash.com/photo-1569003339405-ea396a5a8a90?q=80&w=600",
                        year = "2008", rating = "7.9", genre = "Acción / Sci-Fi / Aventura",
                        description = "Un empresario multimillonario y genio inventor, Tony Stark, es secuestrado en el extranjero. Usando su ingenio, construye una armadura de alta tecnología y escapa, decidiendo usar su poder para proteger al mundo."
                    )
                )
            }
            nameL.contains("anime") -> {
                listOf(
                    CatalogItem(
                        id = "a_demonslayer_mugen", title = "Demon Slayer: Mugen Train",
                        posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=600",
                        year = "2020", rating = "8.3", genre = "Animación / Fantasía / Acción",
                        description = "Tanjiro Kamado y sus amigos de la Compañía de Cazadores de Demonios acompañan a Kyojuro Rengoku, el Pilar de la Llama, para investigar una misteriosa serie de desapariciones a bordo del Tren Mugen."
                    ),
                    CatalogItem(
                        id = "a_your_name", title = "Your Name (Kimi no Na wa)",
                        posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=600",
                        year = "2016", rating = "8.4", genre = "Animación / Romance / Drama",
                        description = "Dos desconocidos, una chica de campo y un chico de Tokio, descubren de repente que intercambian cuerpos periódicamente al dormir, creando un vínculo mágico e inolvidable antes de un evento cósmico."
                    ),
                    CatalogItem(
                        id = "a_spirited_away", title = "El Viaje de Chihiro",
                        posterUrl = "https://images.unsplash.com/photo-1528360983277-13d401ccd795?q=80&w=600",
                        year = "2001", rating = "8.6", genre = "Animación / Fantasía / Aventura",
                        description = "Durante el traslado de su familia a los suburbios, una niña de 10 años de edad deambula por un mundo gobernado por dioses, brujas y espíritus, donde los humanos se transforman en bestias."
                    ),
                    CatalogItem(
                        id = "a_jujutsu_kaisen_0", title = "Jujutsu Kaisen 0",
                        posterUrl = "https://images.unsplash.com/photo-1541562232579-512a21360020?q=80&w=600",
                        year = "2021", rating = "7.9", genre = "Animación / Acción / Fantasía",
                        description = "Yuta Okkotsu es un estudiante de secundaria que gana el control de una maldición extremadamente poderosa y se inscribe en la Escuela Secundaria de Jujutsu de la Prefectura de Tokio bajo la tutela de Satoru Gojo."
                    )
                )
            }
            nameL.contains("trending movies") || (nameL.contains("tendencia") && nameL.contains("película")) || (nameL.contains("tendencia") && nameL.contains("pelicula")) -> {
                listOf(
                    CatalogItem(
                        id = "f_dune2", title = "Dune: Parte Dos",
                        posterUrl = "https://images.unsplash.com/photo-1547891306-7a89275ce36a?q=80&w=600",
                        year = "2024", rating = "8.9", genre = "Sci-Fi / Aventura",
                        description = "Paul Atreides se une a Chani y a los Fremen para emprender una campaña de venganza contra los conspiradores que destruyeron a su familia y restaurar el orden."
                    ),
                    CatalogItem(
                        id = "f_opp", title = "Oppenheimer",
                        posterUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?q=80&w=600",
                        year = "2023", rating = "8.9", genre = "Biografía / Historia / Drama",
                        description = "La historia del físico teórico J. Robert Oppenheimer, el director del laboratorio de Los Álamos durante el Proyecto Manhattan para crear el arma definitiva."
                    ),
                    CatalogItem(
                        id = "f_spiderman", title = "Spider-Man: Across the Spider-Verse",
                        posterUrl = "https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=600",
                        year = "2023", rating = "8.8", genre = "Animación / Acción",
                        description = "Miles Morales es catapultado a través del multiverso, donde se encuentra con una sociedad de Spider-People encargada de proteger la existencia misma frente a una anomalía."
                    ),
                    CatalogItem(
                        id = "f_deadpool", title = "Deadpool & Wolverine",
                        posterUrl = "https://images.unsplash.com/photo-1608889175123-8ec330b86f84?q=80&w=600",
                        year = "2024", rating = "8.3", genre = "Acción / Comedia / Sci-Fi",
                        description = "Un apático Wade Wilson se esfuerza por integrarse en la vida civil, pero el destino del multiverso lo obliga de forma inesperada a formar equipo con un reticente y feroz Lobezno."
                    )
                )
            }
            nameL.contains("trending tv") || nameL.contains("trending series") || (nameL.contains("tendencia") && nameL.contains("serie")) -> {
                listOf(
                    CatalogItem(
                        id = "f_shogun", title = "Shōgun",
                        posterUrl = "https://images.unsplash.com/photo-1533105079780-92b9be482077?q=80&w=600",
                        year = "2024", rating = "9.1", genre = "Historia / Drama / Acción",
                        description = "En el Japón feudal de 1600, Lord Yoshii Toranaga lucha ferozmente por mantener con vida a su asediado clan mientras sus rivales forman coaliciones mortales contra él."
                    ),
                    CatalogItem(
                        id = "f_boys", title = "The Boys",
                        posterUrl = "https://images.unsplash.com/photo-1626814026360-221091186039?q=80&w=600",
                        year = "2024", rating = "8.7", genre = "Acción / Sátira",
                        description = "Un grupo de intrépidos justicieros continúa su implacable cruzada clandestina para desenmascarar y derrocar a los superhéroes más corruptos e hipócritas del imperio Vought."
                    ),
                    CatalogItem(
                        id = "f_lastofus", title = "The Last of Us",
                        posterUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=600",
                        year = "2023", rating = "8.8", genre = "Drama / Terror",
                        description = "Tras la dolorosa caída de la sociedad moderna, un endurecido contrabandista llamado Joel asume la misión crítica de escoltar a Ellie, una fuerte joven de 14 años."
                    ),
                    CatalogItem(
                        id = "f_stranger", title = "Stranger Things",
                        posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600",
                        year = "2022", rating = "8.7", genre = "Fantasía / Sci-Fi / Drama",
                        description = "Al desentrañar la misteriosa desaparición de su amigo Will, un pequeño pueblo de Indiana descubre un laboratorio gubernamental secreto con portales a otra dimensión oscura."
                    )
                )
            }
            nameL.contains("popular movies") || (nameL.contains("popular") && nameL.contains("película")) || (nameL.contains("popular") && nameL.contains("pelicula")) -> {
                listOf(
                    CatalogItem(
                        id = "f_inside2", title = "Intensamente 2",
                        posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=600",
                        year = "2024", rating = "8.4", genre = "Animación / Familiar / Comedia",
                        description = "El cuartel mental de Riley experimenta una de las más grandes demoliciones al abrir espacio repentinamente a Ansiedad y otras emociones adolescentes."
                    ),
                    CatalogItem(
                        id = "f_glad2", title = "Gladiator II",
                        posterUrl = "https://images.unsplash.com/photo-1598257006458-087169a1f08d?q=80&w=600",
                        year = "2024", rating = "7.9", genre = "Drama / Acción / Historia",
                        description = "Lucio se ve obligado por la fuerza tiránica a entrar al glorioso coliseo romano para reclamar el honor mancillado del pueblo de Roma bajo opresores gemelos."
                    ),
                    CatalogItem(
                        id = "f_wick4", title = "John Wick: Capítulo 4",
                        posterUrl = "https://images.unsplash.com/photo-1508962914676-134849a727f0?q=80&w=600",
                        year = "2023", rating = "8.2", genre = "Acción / Suspenso",
                        description = "John Wick halla una rendija definitiva para vencer a la todopoderosa Mesa Alta, enfrentando a asesinos en múltiples continentes bajo una lluvia de balas."
                    )
                )
            }
            nameL.contains("popular series") || nameL.contains("popular tv") || (nameL.contains("popular") && nameL.contains("serie")) -> {
                listOf(
                    CatalogItem(
                        id = "f_house", title = "House of the Dragon",
                        posterUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=600",
                        year = "2024", rating = "8.5", genre = "Fantasía / Drama",
                        description = "La tumultuosa historia del origen y el sangriento inicio de la guerra civil dinástica conocida como la Danza de los Dragones dentro de la dinastía Targaryen."
                    ),
                    CatalogItem(
                        id = "f_fallout", title = "Fallout",
                        posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=600",
                        year = "2024", rating = "8.6", genre = "Ficción / Acción / Aventura",
                        description = "La pacífica historia de la supervivencia en los búnkeres subterráneos de lujo se estrella de frente con el páramo radiactivo del exterior."
                    )
                )
            }
            nameL.contains("top rated movies") -> {
                listOf(
                    CatalogItem(
                        id = "f_godfather", title = "El Padrino",
                        posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600",
                        year = "1972", rating = "9.2", genre = "Drama / Crimen",
                        description = "La envejecida y poderosa dinastía de un patriarca del crimen organizado transfiere el control clandestino de su imperio a su reacio hijo menor rescatando el honor familiar."
                    ),
                    CatalogItem(
                        id = "f_darkknight", title = "Batman: El Caballero de la Noche",
                        posterUrl = "https://images.unsplash.com/photo-1531259683007-016a7b628fc3?q=80&w=600",
                        year = "2008", rating = "9.0", genre = "Acción / Crimen / Drama",
                        description = "Cuando una retorcida mente criminal conocida colectivamente como el Joker desata el caos absoluto en Gotham City, Batman libra una guerra intensa."
                    ),
                    CatalogItem(
                        id = "f_inter", title = "Interstellar",
                        posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=600",
                        year = "2014", rating = "8.7", genre = "Sci-Fi / Drama",
                        description = "Un grupo de científicos y exploradores valientes viajan a través de un agujero de gusano misterioso para buscar un nuevo planeta habitable para la humanidad."
                    )
                )
            }
            nameL.contains("top rated series") -> {
                listOf(
                    CatalogItem(
                        id = "f_breaking", title = "Breaking Bad",
                        posterUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=600",
                        year = "2013", rating = "9.5", genre = "Drama / Crimen",
                        description = "Un brillante pero deprimido profesor de química es diagnosticado con cáncer terminal y decide asociarse con un exalumno para montar un laboratorio móvil."
                    ),
                    CatalogItem(
                        id = "f_chernobyl", title = "Chernobyl",
                        posterUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=600",
                        year = "2019", rating = "9.4", genre = "Drama / Historia",
                        description = "La dramática recreación fidedigna de la explosión devastadora de la planta nuclear soviética en 1986 y la enorme operación patriótica de contención subsecuente."
                    )
                )
            }
            nameL.contains("anime trending") -> {
                listOf(
                    CatalogItem(
                        id = "f_demonslayer", title = "Demon Slayer: Kimetsu no Yaiba",
                        posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=600",
                        year = "2024", rating = "8.7", genre = "Anime / Acción / Fantasía",
                        description = "Tanjiro Kamado emprende un duro y largo viaje para convertirse en cazador de demonios con el único fin de salvar de la condena a su afectada hermana Nezuko."
                    ),
                    CatalogItem(
                        id = "f_jujutsu", title = "Jujutsu Kaisen",
                        posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=600",
                        year = "2023", rating = "8.6", genre = "Anime / Acción / Sobrenatural",
                        description = "Yuji Itadori, un estudiante de secundaria con destreza física admirable, traga un accesorio maldito del temible Sukuna para resguardar a sus amigos."
                    )
                )
            }
            nameL.contains("anime popular") -> {
                listOf(
                    CatalogItem(
                        id = "f_deathnote", title = "Death Note",
                        posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=600",
                        year = "2006", rating = "9.0", genre = "Anime / Misterio / Drama",
                        description = "Un prodigioso estudiante de secundaria encuentra una libreta sobrenatural de un Shinigami con la letal capacidad de silenciar a cualquiera cuyo nombre se escriba."
                    ),
                    CatalogItem(
                        id = "f_attack", title = "Shingeki no Kyojin",
                        posterUrl = "https://images.unsplash.com/photo-1626814026360-221091186039?q=80&w=600",
                        year = "2023", rating = "9.1", genre = "Anime / Acción / Fantasía",
                        description = "Eren Yeager dedica su vida entera a exterminar a los misteriosos Titanes gigantes tras presenciar la caída violenta de los muros de su ciudad natal."
                    )
                )
            }
            nameL.contains("acción") || nameL.contains("accion") -> {
                listOf(
                    CatalogItem(
                        id = "f_wick_a", title = "John Wick 4",
                        posterUrl = "https://images.unsplash.com/photo-1508962914676-134849a727f0?q=80&w=600",
                        year = "2023", rating = "8.2", genre = "Acción / Suspenso",
                        description = "John Wick halla una rendija definitiva para vencer de una vez por todas a la todopoderosa Mesa Alta de asesinos de todo el globo."
                    ),
                    CatalogItem(
                        id = "f_madmax_a", title = "Mad Max: Fury Road",
                        posterUrl = "https://images.unsplash.com/photo-1626379616459-b2ce1d9decbc?q=80&w=600",
                        year = "2015", rating = "8.1", genre = "Acción / Post-apocalíptico",
                        description = "En un páramo estéril azotado por tormentas de arena indómitas y pandillas motorizadas, Imperator Furiosa comanda un rescate veloz."
                    )
                )
            }
            nameL.contains("comedia") -> {
                listOf(
                    CatalogItem(
                        id = "f_barbie_c", title = "Barbie",
                        posterUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?q=80&w=600",
                        year = "2023", rating = "7.2", genre = "Comedia / Fantasía",
                        description = "Barbie y Ken incursionan fuera de su idílico mundo de plástico rosa para conocer el desafiante, contradictorio e imperfecto mundo humano real."
                    ),
                    CatalogItem(
                        id = "f_mask_c", title = "La Máscara",
                        posterUrl = "https://images.unsplash.com/photo-1543269865-cbf427effbad?q=80&w=600",
                        year = "1994", rating = "8.0", genre = "Comedia / Fantasía",
                        description = "Un pusilánime bancario encuentra un artefacto sagrado consagrado a Loki, desatando una personalidad caricaturesca y cómica sin límites."
                    )
                )
            }
            nameL.contains("terror") -> {
                listOf(
                    CatalogItem(
                        id = "f_conjuro_t", title = "El Conjuro",
                        posterUrl = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?q=80&w=600",
                        year = "2013", rating = "7.5", genre = "Terror / Paranormal",
                        description = "Investigadores paranormales asisten a una asustada pareja y a sus hijas acosadas espiritualmente por entidades demoníacas en su isolated hogar."
                    ),
                    CatalogItem(
                        id = "f_hereditary_t", title = "Hereditary",
                        posterUrl = "https://images.unsplash.com/photo-1505635552518-3448ff116af3?q=80&w=600",
                        year = "2018", rating = "7.3", genre = "Drama / Terror / Misterio",
                        description = "Tras fallecer la excéntrica abuela de la familia Graham, atroces secretos ancestrales y rituales sectarios de corte bíblico empiezan a manifestarse."
                    )
                )
            }
            nameL.contains("ciencia") || nameL.contains("ficción") || nameL.contains("ficcion") || nameL.contains("sci-fi") -> {
                listOf(
                    CatalogItem(
                        id = "f_dune1_s", title = "Dune: Parte Uno",
                        posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=600",
                        year = "2021", rating = "8.1", genre = "Sci-Fi / Aventura",
                        description = "Paul Atreides, un joven aristócrata superdotado, viaja al planeta desértico Arrakis para asegurar el invaluable recurso de la especia."
                    ),
                    CatalogItem(
                        id = "f_blade_s", title = "Blade Runner 2049",
                        posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=600",
                        year = "2017", rating = "8.4", genre = "Sci-Fi / Distopía / Neo-Noir",
                        description = "Un replicante detective desentierra un enigma milenario de asombroso calibre espiritual que amenaza con convulsionar irreversiblemente el orden general."
                    )
                )
            }
            nameL.contains("documentales") -> {
                listOf(
                    CatalogItem(
                        id = "f_earth3_d", title = "Planeta Tierra III",
                        posterUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?q=80&w=600",
                        year = "2023", rating = "9.5", genre = "Documental / Naturaleza",
                        description = "La colosal y premiada saga explora el asombroso ingenio de las especies animales intentando persistir en hábitats que cambian a velocidad récord."
                    ),
                    CatalogItem(
                        id = "f_lastdance_d", title = "El Último Baile",
                        posterUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=600",
                        year = "2020", rating = "9.1", genre = "Documental / Deportes",
                        description = "La recopilación definitiva del meteórico impacto de la figura inmortal de Michael Jordan y de su última temporada gloriosa al mando de los Bulls."
                    )
                )
            }
            nameL.contains("familiar") || nameL.contains("familia") -> {
                listOf(
                    CatalogItem(
                        id = "f_inside_f", title = "Intensamente 2",
                        posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=600",
                        year = "2024", rating = "8.4", genre = "Animación / Familiar / Comedia",
                        description = "El cuartel mental de Riley experimenta una de las más grandes demoliciones al abrir espacio repentinamente a Ansiedad y otras emociones adolescentes."
                    ),
                    CatalogItem(
                        id = "f_coco_f", title = "Coco",
                        posterUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?q=80&w=600",
                        year = "2017", rating = "8.4", genre = "Animación / Familiar / Fantasía",
                        description = "El pequeño Miguel comete una ofrenda musical que lo propulsa mágicamente a la deslumbrante y alegre dimensión de los difuntos mexicanos."
                    )
                )
            }
            else -> {
                listOf(
                    CatalogItem(
                        id = "f_glad2_def", title = "Gladiator II",
                        posterUrl = "https://images.unsplash.com/photo-1598257006458-087169a1f08d?q=80&w=600",
                        year = "2024", rating = "7.9", genre = "Drama / Acción",
                        description = "Lucio se ve obligado por la fuerza tiránica a entrar al glorioso coliseo romano para reclamar el honor mancillado del pueblo de Roma bajo opresores gemelos."
                    )
                )
            }
        }
    }
}
