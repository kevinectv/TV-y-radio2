package com.example.data

import android.content.Context
import com.example.data.model.Catalog
import com.example.data.model.CatalogItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    }

    private fun loadCatalogs() {
        try {
            if (catalogsFile.exists()) {
                val json = catalogsFile.readText()
                val list = jsonAdapter.fromJson(json)
                if (list != null && list.isNotEmpty()) {
                    val healed = list.map { cat ->
                        val currentLayout = try { cat.layoutType } catch (e: Exception) { "Horizontal" } ?: "Horizontal"
                        val correctedLayout = if (currentLayout == "Horizontal" && (cat.name.contains("Top 250", ignoreCase = true) || cat.name.contains("Mejor Valorada", ignoreCase = true) || cat.name.contains("top", ignoreCase = true) || cat.name.contains("tops", ignoreCase = true))) {
                            "Top Numerado"
                        } else {
                            currentLayout
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
        val items = generateItemsForCategory(catalog.name)
        val newCatalog = catalog.copy(
            id = if (catalog.id.isEmpty()) UUID.randomUUID().toString() else catalog.id,
            orderIndex = index,
            items = items,
            status = "Sincronizado",
            lastUpdated = "Hoy mismo"
        )
        current.add(newCatalog)
        saveCatalogsList(current)
        true
    }

    suspend fun updateCatalog(updated: Catalog): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.map {
            if (it.id == updated.id) updated else it
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
        val current = _catalogs.value.map {
            if (it.id == id) {
                it.copy(
                    status = "Sincronizado",
                    lastUpdated = "Último minuto",
                    items = generateItemsForCategory(it.name)
                )
            } else it
        }
        saveCatalogsList(current)
        true
    }

    suspend fun syncAll(): Boolean = withContext(Dispatchers.IO) {
        val current = _catalogs.value.map {
            it.copy(
                status = "Sincronizado",
                lastUpdated = "Hace unos instantes",
                items = generateItemsForCategory(it.name)
            )
        }
        saveCatalogsList(current)
        true
    }

    private fun createDefaultCatalogs(): List<Catalog> {
        val categories = listOf(
            "🔥 Tendencias en Películas" to "TMDB",
            "📺 Tendencias en Series" to "TMDB",
            "⭐ Mejor Valoradas" to "TMDB",
            "🎬 Estrenos" to "TMDB",
            "🏆 Top 250" to "MDBList",
            "🍿 Recomendadas" to "Trakt",
            "🎭 Acción" to "Local",
            "😂 Comedia" to "Local",
            "👻 Terror" to "Local",
            "🚀 Ciencia Ficción" to "Local",
            "🎌 Anime" to "Local",
            "👨👩👧 Familiar" to "Local",
            "🕵 Suspenso" to "Local",
            "❤️ Romance" to "Local",
            "🎵 Musicales" to "Local",
            "🌍 Documentales" to "Local",
            "⚽ Deportes" to "Local"
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
                layoutType = if (name.contains("Top 250") || name.contains("Mejor Valoradas")) "Top Numerado" else "Horizontal",
                items = generateItemsForCategory(name)
            )
        }
    }

    private fun generateItemsForCategory(name: String): List<CatalogItem> {
        return when {
            name.contains("Película") || name.contains("Estrenos") || name.contains("Top 250") -> {
                listOf(
                    CatalogItem(
                        id = "m1", title = "Dune: Parte Dos",
                        posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=300",
                        year = "2024", rating = "8.7", genre = "Sci-Fi",
                        description = "Paul Atreides se une a Chani y los Fremen mientras busca venganza contra quienes destruyeron a su familia."
                    ),
                    CatalogItem(
                        id = "m2", title = "Oppenheimer",
                        posterUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?q=80&w=300",
                        year = "2023", rating = "8.9", genre = "Historia",
                        description = "La historia del físico estadounidense J. Robert Oppenheimer y su papel en el desarrollo de la bomba atómica."
                    ),
                    CatalogItem(
                        id = "m3", title = "Spider-Man: Across the Spider-Verse",
                        posterUrl = "https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=300",
                        year = "2023", rating = "9.0", genre = "Animación",
                        description = "Miles Morales se embarca en una aventura a través del multiverso junto a Gwen Stacy y un equipo de Spider-People."
                    ),
                    CatalogItem(
                        id = "m4", title = "Anatomía de una Caída",
                        posterUrl = "https://images.unsplash.com/photo-1585647347483-22b66260dfff?q=80&w=300",
                        year = "2023", rating = "8.1", genre = "Drama",
                        description = "Una mujer es sospechosa de la muerte de su esposo en un remoto chalet alpino."
                    ),
                    CatalogItem(
                        id = "m5", title = "Interestelar",
                        posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=300",
                        year = "2014", rating = "8.6", genre = "Sci-Fi",
                        description = "Un grupo de exploradores viaja a través de un agujero de gusano en el espacio en un intento por asegurar la supervivencia de la humanidad."
                    )
                )
            }
            name.contains("Serie") || name.contains("Recomendadas") -> {
                listOf(
                    CatalogItem(
                        id = "s1", title = "The Last of Us",
                        posterUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?q=80&w=300",
                        year = "2023", rating = "8.8", genre = "Drama / Terror",
                        description = "Veinte años después de que una letal infección fúngica destruyera la civilización, Joel es contratado para sacar a Ellie."
                    ),
                    CatalogItem(
                        id = "s2", title = "Shōgun",
                        posterUrl = "https://images.unsplash.com/photo-1533105079780-92b9be482077?q=80&w=300",
                        year = "2024", rating = "9.1", genre = "Historia / Acción",
                        description = "En el Japón de 1600, Lord Yoshii Toranaga lucha por su vida mientras sus enemigos en el Consejo de Regentes se unen."
                    ),
                    CatalogItem(
                        id = "s3", title = "Succession",
                        posterUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=300",
                        year = "2023", rating = "8.9", genre = "Drama",
                        description = "La saga de una familia disfuncional dueña de un imperio de medios globales y la lucha por el poder."
                    ),
                    CatalogItem(
                        id = "s4", title = "Stranger Things",
                        posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=300",
                        year = "2022", rating = "8.7", genre = "Fantasía / Sci-Fi",
                        description = "Un grupo de niños en Hawkins descubre portales a una dimensión paralela llena de monstruos."
                    )
                )
            }
            name.contains("Acción") || name.contains("Suspenso") -> {
                listOf(
                    CatalogItem(
                        id = "a1", title = "John Wick: Capítulo 4",
                        posterUrl = "https://images.unsplash.com/photo-1508962914676-134849a727f0?q=80&w=300",
                        year = "2023", rating = "8.2", genre = "Acción",
                        description = "John Wick descubre un camino para derrotar a la Mesa Alta pero primero debe enfrentarse a un nuevo enemigo de recursos millonarios."
                    ),
                    CatalogItem(
                        id = "a2", title = "Mad Max: Furia en el Camino",
                        posterUrl = "https://images.unsplash.com/photo-1626379616459-b2ce1d9decbc?q=80&w=300",
                        year = "2015", rating = "8.1", genre = "Acción / Post-apocalipsis",
                        description = "En un desierto post-apocalíptico, una mujer se rebela contra un tirano con la ayuda de un vagabundo atormentado."
                    )
                )
            }
            name.contains("Comedia") || name.contains("Familiar") -> {
                listOf(
                    CatalogItem(
                        id = "c1", title = "Barbie",
                        posterUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?q=80&w=300",
                        year = "2023", rating = "7.2", genre = "Comedia",
                        description = "Barbie y Ken experimentan el mundo real después de enfrentar una crisis existencial en Barbieland."
                    ),
                    CatalogItem(
                        id = "c2", title = "La Máscara",
                        posterUrl = "https://images.unsplash.com/photo-1543269865-cbf427effbad?q=80&w=300",
                        year = "1994", rating = "8.0", genre = "Comedia / Fantasía",
                        description = "Un aburrido ejecutivo de banco encuentra una máscara mística de un rey de la travesura que lo convierte en un loco caricaturesco súper poderoso."
                    )
                )
            }
            name.contains("Terror") -> {
                listOf(
                    CatalogItem(
                        id = "h1", title = "El Conjuro",
                        posterUrl = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?q=80&w=300",
                        year = "2013", rating = "7.5", genre = "Terror / Suspenso",
                        description = "Investigadores paranormales acuden en ayuda de una familia acosada por una presencia demoníaca en su granja."
                    ),
                    CatalogItem(
                        id = "h2", title = "Hereditary",
                        posterUrl = "https://images.unsplash.com/photo-1505635552518-3448ff116af3?q=80&w=300",
                        year = "2018", rating = "7.3", genre = "Drama / Terror",
                        description = "Después del fallecimiento de su anciana madre, una matriarca y su familia comienzan a desentrañar oscuros secretos sobre sus antepasados."
                    )
                )
            }
            name.contains("Ciencia") || name.contains("Sci-Fi") -> {
                listOf(
                    CatalogItem(
                        id = "sf1", title = "Blade Runner 2049",
                        posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=300",
                        year = "2017", rating = "8.0", genre = "Sci-Fi / Cyberpunk",
                        description = "Un nuevo blade runner descubre un gran secreto que podría arrojar a la sociedad al caos total en Los Ángeles del futuro."
                    ),
                    CatalogItem(
                        id = "sf2", title = "Matrix",
                        posterUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=300",
                        year = "1999", rating = "8.7", genre = "Sci-Fi / Cyberpunk",
                        description = "Un pirata informático descubre la verdadera naturaleza artificial de su realidad y lucha contra poderosos agentes de control."
                    )
                )
            }
            name.contains("Anime") -> {
                listOf(
                    CatalogItem(
                        id = "an1", title = "El Viaje de Chihiro",
                        posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=300",
                        year = "2001", rating = "8.6", genre = "Anime / Fantasía",
                        description = "Una niña perdida en una extraña ciudad mágica de espíritus debe trabajar para salvar a sus padres encantados."
                    ),
                    CatalogItem(
                        id = "an2", title = "Demon Slayer: Mugen Train",
                        posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=300",
                        year = "2020", rating = "8.3", genre = "Anime / Acción",
                        description = "Tanjiro y sus compañeros investigan misteriosas desapariciones dentro de un tren en movimiento dominado por demonios."
                    )
                )
            }
            name.contains("Deportes") -> {
                listOf(
                    CatalogItem(
                        id = "dp1", title = "Formula 1: Drive to Survive",
                        posterUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?q=80&w=300",
                        year = "2023", rating = "8.6", genre = "Documental / Deporte",
                        description = "Pilotos, directores cinematográficos y dueños de equipos viven la adrenalina de la pista más exigente de F1."
                    ),
                    CatalogItem(
                        id = "dp2", title = "Ted Lasso",
                        posterUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=300",
                        year = "2022", rating = "8.8", genre = "Comedia / Deporte",
                        description = "Un carismático entrenador de fútbol americano cruza el océano para entrenar a un club de fútbol británico."
                    )
                )
            }
            name.contains("Documentales") -> {
                listOf(
                    CatalogItem(
                        id = "dc1", title = "Nuestro Planeta",
                        posterUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=300",
                        year = "2019", rating = "8.9", genre = "Documental",
                        description = "Experimente la belleza natural de nuestro planeta y examine cómo el cambio climático afecta a todos los seres vivos."
                    ),
                    CatalogItem(
                        id = "dc2", title = "Mi Maestro el Pulpo",
                        posterUrl = "https://images.unsplash.com/photo-1437622368342-7a3d73a34c8f?q=80&w=300",
                        year = "2020", rating = "8.4", genre = "Documental / Naturaleza",
                        description = "Un cineasta forja una amistad inusual con un pulpo que vive en un bosque de algas marinas en Sudáfrica."
                    )
                )
            }
            else -> {
                listOf(
                    CatalogItem(
                        id = "g1", title = "Película Estelar Premium",
                        posterUrl = "https://images.unsplash.com/photo-1598257006458-087169a1f08d?q=80&w=300",
                        year = "2024", rating = "8.5", genre = "General",
                        description = "La joya definitiva de la cartelera seleccionada exclusivamente por el panel analítico de Lumina Premium."
                    ),
                    CatalogItem(
                        id = "g2", title = "Saga de Suspenso Cósmico",
                        posterUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=300",
                        year = "2023", rating = "8.0", genre = "Drama / Misterio",
                        description = "Revelaciones impactantes sobre inteligencias misteriosas y secretos que amenazan con desestabilizar la realidad."
                    )
                )
            }
        }
    }
}
