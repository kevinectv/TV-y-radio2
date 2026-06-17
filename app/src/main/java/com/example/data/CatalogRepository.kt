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

class CatalogRepository(private val context: Context) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val catalogListType = Types.newParameterizedType(List::class.java, Catalog::class.java)
    private val jsonAdapter = moshi.adapter<List<Catalog>>(catalogListType)
    private val catalogsFile = File(context.filesDir, "installed_catalogs.json")

    private val _catalogs = MutableStateFlow<List<Catalog>>(emptyList())
    val catalogs: StateFlow<List<Catalog>> = _catalogs

    init {
        loadCatalogs()
        // Run lazy sync on startup so any empty remote catalogs can fetch actual data in background
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            var needsSave = false
            val current = _catalogs.value.map { cat ->
                val hasNoDefaultMock = cat.url.isNotEmpty() && 
                    !cat.url.contains("lumina.app/catalogs") && 
                    !cat.url.contains("api.themoviedb.org/3/catalog/") &&
                    !cat.url.contains("mdblist.com/lists/public/") &&
                    !cat.url.contains("api.trakt.tv/lists/")
                
                if (cat.items.isEmpty() && cat.url.startsWith("http") && hasNoDefaultMock) {
                    needsSave = true
                    val items = fetchItemsForCatalog(cat)
                    cat.copy(items = items, status = "Sincronizado", lastUpdated = "Al iniciar")
                } else cat
            }
            if (needsSave) {
                saveCatalogsList(current)
            }
            refreshLocalCatalogs()
        }
    }

    private fun loadCatalogs() {
        try {
            if (catalogsFile.exists()) {
                val json = catalogsFile.readText()
                val list = jsonAdapter.fromJson(json)
                if (list != null && list.isNotEmpty()) {
                    val healed = list.map { cat ->
                        val currentLayout = try { cat.layoutType } catch (e: Exception) { "Horizontal Poster Row" } ?: "Horizontal Poster Row"
                        val correctedLayout = when (currentLayout) {
                            "Horizontal" -> "Horizontal Poster Row"
                            "Vertical" -> "Vertical Poster Row"
                            else -> currentLayout
                        }
                        cat.copy(layoutType = correctedLayout)
                    }
                    _catalogs.value = healed.sortedBy { it.orderIndex }
                    saveCatalogsList(healed)
                    return
                }
            }
            // If file does not exist or empty list, populate the 17 default premium catalogs
            val defaults = createDefaultCatalogs()
            saveCatalogsList(defaults)
        } catch (e: Exception) {
            e.printStackTrace()
            val defaults = createDefaultCatalogs()
            _catalogs.value = defaults
        }
    }

    private fun saveCatalogsList(list: List<Catalog>) {
        try {
            val sortedList = list.mapIndexed { idx, catalog -> catalog.copy(orderIndex = idx) }
            val json = jsonAdapter.toJson(sortedList)
            catalogsFile.writeText(json)
            _catalogs.value = sortedList
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllCatalogs(): List<Catalog> = _catalogs.value

    suspend fun addCatalog(catalog: Catalog): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.toMutableList()
        val index = current.size
        val items = fetchItemsForCatalog(catalog)
        val newCatalog = catalog.copy(
            id = if (catalog.id.isEmpty()) UUID.randomUUID().toString() else catalog.id,
            orderIndex = index,
            items = items,
            status = "Sincronizado",
            lastUpdated = "Hoy"
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
                val realItems = if (urlChanged || itemsEmpty) {
                    fetchItemsForCatalog(updated)
                } else {
                    updated.items
                }
                updated.copy(
                    items = realItems,
                    status = "Sincronizado",
                    lastUpdated = "Ahora mismo"
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
                val realItems = fetchItemsForCatalog(it)
                it.copy(
                    status = "Sincronizado",
                    lastUpdated = "Último minuto",
                    items = realItems
                )
            } else it
        }
        saveCatalogsList(updated)
        true
    }

    suspend fun syncAll(): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.map {
            val realItems = fetchItemsForCatalog(it)
            it.copy(
                status = "Sincronizado",
                lastUpdated = "Hace unos instantes",
                items = realItems
            )
        }
        saveCatalogsList(current)
        true
    }

    suspend fun refreshLocalCatalogs() = withContext(Dispatchers.IO) {
        val current = _catalogs.value
        val updated = current.map { cat ->
            if (cat.sourceType == "Local" || cat.sourceType == "Custom" || cat.url.contains("lumina.app/catalogs") || cat.url.isEmpty()) {
                val realItems = fetchItemsForCatalog(cat)
                cat.copy(items = realItems, status = "Sincronizado", lastUpdated = "Recién Recargado")
            } else cat
        }
        saveCatalogsList(updated)
    }

    private fun createDefaultCatalogs(): List<Catalog> {
        val categories = listOf(
            "Trending Movies" to "TMDB",
            "Trending TV Shows" to "TMDB",
            "Popular Movies" to "TMDB",
            "Popular Series" to "TMDB",
            "Top Rated Movies" to "TMDB",
            "Top Rated Series" to "TMDB",
            "Anime Trending" to "Local",
            "Anime Popular" to "Local",
            "Acción" to "Local",
            "Comedia" to "Local",
            "Terror" to "Local",
            "Ciencia Ficción" to "Local",
            "Documentales" to "Local",
            "Familiar" to "Local"
        )
        return categories.mapIndexed { idx, (name, src) ->
            Catalog(
                id = "default_${idx + 1}",
                name = name,
                sourceType = src,
                url = when (src) {
                    "TMDB" -> "https://api.themoviedb.org/3/catalog/default_${idx + 1}"
                    "MDBList" -> "https://mdblist.com/lists/public/default_${idx + 1}"
                    "Trakt" -> "https://api.trakt.tv/lists/default_${idx + 1}"
                    else -> "http://lumina.app/catalogs/genre_${idx + 1}"
                },
                isVisible = true,
                showInHome = true,
                showInRecommendations = idx % 3 == 0,
                showInSearch = true,
                numItems = 15,
                status = "Sincronizado",
                lastUpdated = "Hoy",
                orderIndex = idx,
                layoutType = when {
                    name.contains("Trending Movies", ignoreCase = true) -> "Horizontal Poster Row"
                    name.contains("Trending TV Shows", ignoreCase = true) || name.contains("Trending Series", ignoreCase = true) || name.contains("Popular Series", ignoreCase = true) -> "Landscape Row"
                    name.contains("Anime Trending", ignoreCase = true) -> "Vertical Poster Row"
                    name.contains("Top Rated", ignoreCase = true) -> "Large Featured Row"
                    name.contains("New Releases", ignoreCase = true) || name.contains("Popular Movies", ignoreCase = true) -> "Banner Row"
                    name.contains("Familiar", ignoreCase = true) || name.contains("Documentales", ignoreCase = true) -> "Compact Row"
                    else -> "Horizontal Poster Row"
                },
                items = getDeepFallbacksForCategory(name)
            )
        }
    }

    private suspend fun fetchItemsForCatalog(catalog: Catalog): List<CatalogItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CatalogItem>()

        // 1. Try to fetch from HTTP URL if specified and non-default
        if (catalog.url.isNotEmpty() && 
            catalog.url.startsWith("http") && 
            !catalog.url.contains("lumina.app/catalogs") && 
            !catalog.url.contains("api.themoviedb.org/3/catalog/") &&
            !catalog.url.contains("mdblist.com/lists/public/") &&
            !catalog.url.contains("api.trakt.tv/lists/")) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder()
                    .url(catalog.url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: ""
                        if (bodyString.trim().startsWith("#EXTM3U")) {
                            // It's an M3U file
                            val m3uItems = parseM3uCatalog(bodyString, catalog.id)
                            if (m3uItems.isNotEmpty()) {
                                list.addAll(m3uItems)
                            }
                        } else if (bodyString.trim().startsWith("{") || bodyString.trim().startsWith("[")) {
                            // It's JSON (TMDB or Trakt or Custom API)
                            val jsonItems = parseJsonCatalog(bodyString, catalog)
                            if (jsonItems.isNotEmpty()) {
                                list.addAll(jsonItems)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Query matching local VOD/Movie/Series channels parsed from IPTV Playlists
        val dbItems = mutableListOf<CatalogItem>()
        try {
            val db = com.example.data.database.AppDatabase.getDatabase(context)
            val channels = db.mediaDao().getAllChannelEntitiesList()
            if (channels.isNotEmpty()) {
                val catNameLower = catalog.name.lowercase()
                
                val filteredChannels = channels.filter { chan ->
                    val category = chan.category.lowercase()
                    val nameLower = chan.name.lowercase()
                    
                    // Detect if this channel represents a video/movie stream
                    val isMovieCategory = category.contains("pelicula") || category.contains("película") ||
                            category.contains("movie") || category.contains("vod") ||
                            category.contains("cine") || category.contains("cinema") ||
                            category.contains("series") || category.contains("serie") ||
                            category.contains("show") || category.contains("temporada") ||
                            category.contains("documental") || category.contains("premium")
                    
                    val matchesGenre = when {
                        catNameLower.contains("acción") || catNameLower.contains("accion") -> category.contains("accion") || category.contains("acción") || category.contains("action") || nameLower.contains("acción") || nameLower.contains("accion")
                        catNameLower.contains("comedia") -> category.contains("comedia") || category.contains("comedy") || nameLower.contains("comedia")
                        catNameLower.contains("terror") -> category.contains("terror") || category.contains("horror") || nameLower.contains("terror") || nameLower.contains("horror")
                        catNameLower.contains("ciencia") || catNameLower.contains("sci-fi") -> category.contains("ciencia") || category.contains("sci") || category.contains("fiction") || category.contains("ficción")
                        catNameLower.contains("anime") -> category.contains("anime") || category.contains("manga") || nameLower.contains("anime")
                        catNameLower.contains("deportes") -> category.contains("deporte") || category.contains("sport") || category.contains("fútbol") || category.contains("football")
                        catNameLower.contains("documentales") -> category.contains("documental") || category.contains("docu")
                        catNameLower.contains("familiar") || catNameLower.contains("kids") -> category.contains("familiar") || category.contains("kids") || category.contains("famil") || category.contains("infantil")
                        catNameLower.contains("suspenso") -> category.contains("suspenso") || category.contains("suspense") || category.contains("thriller")
                        catNameLower.contains("romance") -> category.contains("romance") || category.contains("román") || category.contains("amor")
                        else -> false
                    }
                    
                    val matchesDirectGroup = catNameLower.isNotEmpty() && (
                        category.contains(catNameLower) || 
                        catNameLower.contains(category) || 
                        nameLower.contains(catNameLower)
                    )
                    
                    matchesGenre || matchesDirectGroup || (isMovieCategory && (
                        catNameLower.contains("tendencias") || catNameLower.contains("mejor valorada") ||
                        catNameLower.contains("estreno") || catNameLower.contains("top") ||
                        catNameLower.contains("recomendadas") || catNameLower.contains("default")
                    ))
                }
                
                filteredChannels.forEachIndexed { index, chan ->
                    val yearRegex = Regex("\\b(19|20)\\d{2}\\b")
                    val yearMatch = yearRegex.find(chan.name)
                    val year = yearMatch?.value ?: "2024"
                    val cleanTitle = chan.name.replace(Regex("\\s*[(]?\\b(19|20)\\d{2}\\b[)]?\\s*"), "").trim()
                    
                    dbItems.add(
                        CatalogItem(
                            id = chan.id,
                            title = cleanTitle,
                            posterUrl = chan.logoUrl.ifEmpty { "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=300" },
                            year = year,
                            rating = String.format(java.util.Locale.US, "%.1f", 7.2 + (chan.name.hashCode().coerceAtLeast(0) % 25) / 10.0),
                            genre = chan.category,
                            description = "Contenido sintonizado en vivo desde tu lista IPTV. Presiona reproducir para iniciar la transmisión real.",
                            streamUrl = chan.streamUrl
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Merge results: URL results take priority, supplemented by matching IPTV items
        val mergedList = (list + dbItems).distinctBy { it.title.lowercase().trim() }
        
        // 3. Fallback to highly-detailed, beautiful, real Hollywood reference items if list is still empty (or too short)
        if (mergedList.size < 5) {
            val fallbacks = getDeepFallbacksForCategory(catalog.name)
            (mergedList + fallbacks).distinctBy { it.title.lowercase().trim() }.take(catalog.numItems.coerceAtLeast(15))
        } else {
            mergedList.take(catalog.numItems.coerceAtLeast(15))
        }
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
                    val lastComma = trimmed.lastIndexOf(',')
                    currentName = if (lastComma != -1) {
                        trimmed.substring(lastComma + 1).trim()
                    } else {
                        "Película $index"
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
                        
                        list.add(
                            CatalogItem(
                                id = "${catalog.id}_t_${obj.optString("id", i.toString())}",
                                title = title,
                                posterUrl = poster,
                                year = year,
                                rating = rating,
                                genre = catalog.name,
                                description = desc
                            )
                        )
                    }
                } else {
                    // Try to scan for any array inside the root object and parse it generically
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = root.get(key)
                        if (value is org.json.JSONArray) {
                            val items = parseGenericJsonArray(value, catalog)
                            if (items.isNotEmpty()) {
                                return items
                            }
                        }
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
                listOf("poster", "poster_url", "poster_path", "image", "cover", "thumbnail", "logo").forEach { key ->
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
                var tmdbId = ""
                if (obj.has("tmdb_id") && obj.optString("tmdb_id").isNotEmpty() && obj.optString("tmdb_id") != "null") {
                    tmdbId = obj.optString("tmdb_id")
                } else if (obj.has("tmdbId") && obj.optString("tmdbId").isNotEmpty() && obj.optString("tmdbId") != "null") {
                    tmdbId = obj.optString("tmdbId")
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
                        }
                    }
                }
                
                if (poster.isEmpty() && tmdbId.isNotEmpty() && tmdbId != "null" && tmdbId != "0") {
                    poster = "https://img.vidsrc.to/poster/$tmdbId"
                }
                
                if (poster.isNotEmpty() && !poster.startsWith("http") && poster.startsWith("/")) {
                    poster = "https://image.tmdb.org/t/p/w500$poster"
                }
                if (poster.isEmpty()) {
                    poster = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=300"
                }
                
                var year = obj.optString("year").ifEmpty {
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
                        if (obj.has("movie")) {
                            val mObj = obj.optJSONObject("movie")
                            mObj?.optString("overview") ?: "Contenido sintonizado."
                        } else if (obj.has("show")) {
                            val sObj = obj.optJSONObject("show")
                            sObj?.optString("overview") ?: "Contenido sintonizado."
                        } else {
                            "Contenido sintonizado en Lumina."
                        }
                    }
                }
                
                val streamUrl = obj.optString("url").ifEmpty {
                    obj.optString("stream_url", "")
                }
                
                list.add(
                    CatalogItem(
                        id = "${catalog.id}_g_${i}",
                        title = title,
                        posterUrl = poster,
                        year = year,
                        rating = rating,
                        genre = catalog.name,
                        description = desc,
                        streamUrl = if (streamUrl.isNotEmpty()) streamUrl else null
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
            nameL.contains("trending movies") -> {
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
            nameL.contains("trending tv") || nameL.contains("trending series") -> {
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
            nameL.contains("popular movies") -> {
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
            nameL.contains("popular series") || nameL.contains("popular tv") -> {
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
