package com.example.data

import android.content.Context
import com.example.data.model.MdbListSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URLEncoder

class MdbListSearchService(private val context: Context) {

    // Predefined premium lists as a fallback
    private val predefinedLists = listOf(
        MdbListSearchResult("marvel_movies", "Marvel Movies", "The complete Marvel Cinematic Universe.", 324, "mdblist/garycrawfordgc/marvel-cinematic-universe"),
        MdbListSearchResult("mcu_collection", "MCU Collection", "All Marvel movies and shows in chronological order.", 187, "mdblist/movist-app/marvel-cinematic-universe-chronological"),
        MdbListSearchResult("marvel_universe", "Marvel Universe", "Everything Marvel.", 512, "mdblist/marvel-animation"),
        MdbListSearchResult("marvel_series", "Marvel Series", "Live action and animated Marvel series.", 65, "mdblist/marvel-shows"),
        
        MdbListSearchResult("batman", "Batman Collection", "The Dark Knight movies, animations and series.", 85, "mdblist/batman"),
        
        MdbListSearchResult("disney", "Disney Classics", "Classic Disney animations.", 150, "mdblist/disney-classics"),
        MdbListSearchResult("pixar", "Pixar Masterpieces", "All Pixar animated films.", 28, "mdblist/pixar-movies"),
        
        MdbListSearchResult("anime_trending", "Anime Trending", "Currently trending anime.", 50, "mdblist/new-anime"),
        MdbListSearchResult("anime_popular", "Anime Popular", "Most popular anime of all time.", 250, "mdblist/popular-anime"),
        
        MdbListSearchResult("netflix_movies", "Netflix Originals", "Top Netflix original movies.", 200, "mdblist/garycrawfordgc/netflix-movies"),
        MdbListSearchResult("netflix_series", "Netflix Series", "Top Netflix original series.", 150, "mdblist/garycrawfordgc/netflix-shows"),
        
        MdbListSearchResult("zombie", "Zombie Apocalypse", "Best zombie movies and shows.", 120, "mdblist/zombie-movies"),
        MdbListSearchResult("action", "Action Blockbusters", "High octane action movies.", 300, "mdblist/garycrawfordgc/action"),
        MdbListSearchResult("horror", "Horror Classics", "Spooky and terrifying films.", 150, "mdblist/garycrawfordgc/horror"),
        MdbListSearchResult("scifi", "Sci-Fi Essentials", "Must watch Science Fiction.", 200, "mdblist/garycrawfordgc/sci-fi")
    )

    suspend fun searchCatalogs(query: String): List<MdbListSearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<MdbListSearchResult>()
        val queryLower = query.lowercase().trim()
        
        try {
            val body = BackendApi.getInstance().getCatalog("mdblist/search?q=${URLEncoder.encode(query, "UTF-8")}")
            if (body.startsWith("[")) {
                val arr = JSONArray(body)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id", System.currentTimeMillis().toString() + i)
                    val name = obj.optString("name").ifEmpty { obj.optString("title", "Unknown List") }
                    val desc = obj.optString("description", "Lista obtenida de MDBList")
                    val items = obj.optInt("items", obj.optInt("item_count", 0))
                    
                    var listUrl = obj.optString("url")
                    // Already formatted
                    
                    if (name.isNotEmpty() && listUrl.isNotEmpty()) {
                        results.add(MdbListSearchResult(id, name, desc, items, listUrl))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // If API search returned nothing or failed, fallback to local curated lists
        if (results.isEmpty()) {
            val curatedMatches = predefinedLists.filter { 
                it.name.lowercase().contains(queryLower) || 
                it.description.lowercase().contains(queryLower) ||
                it.url.lowercase().contains(queryLower)
            }
            results.addAll(curatedMatches)
            
            // If still empty, construct a dynamic intelligent catalog like the prompt example
            if (results.isEmpty()) {
                val dynamicName = query.replaceFirstChar { it.uppercase() }
                results.add(
                    MdbListSearchResult(
                        id = "dynamic_${System.currentTimeMillis()}",
                        name = "$dynamicName Movies",
                        description = "Generado dinámicamente para la búsqueda de '$dynamicName'.",
                        itemCount = 50,
                        url = "mdblist/$queryLower" 
                    )
                )
                results.add(
                    MdbListSearchResult(
                        id = "dynamic_col_${System.currentTimeMillis()}",
                        name = "$dynamicName Collection",
                        description = "Colección completa de $dynamicName.",
                        itemCount = 120,
                        url = "mdblist/collection/$queryLower"
                    )
                )
            }
        }
        
        results
    }
}
