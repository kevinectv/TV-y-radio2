package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Catalog
import com.example.ui.MediaViewModel
import com.example.ui.components.responsive
import com.example.ui.components.tvFocusEffect
import com.example.data.util.ApiConfig

data class DiscoverableCatalog(
    val id: String,
    val name: String,
    val sourceType: String, // "MDBList", "Trakt", "TMDB", "Lumina"
    val url: String,
    val posterUrl: String,
    val numItems: Int,
    val description: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumCatalogSearchScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val installedCatalogs by viewModel.catalogsStateFlow.collectAsState()

    val hasTmdbKey = remember {
        ApiConfig.TMDB_API_KEY.isNotEmpty()
    }

    
    var searchQuery by remember { mutableStateOf("") }
    
    // Dataset of premium curated catalogs ready for instant discovery
    val premiumCatalogs = remember {
        listOf(
            // Marvel Catalogs
            DiscoverableCatalog(
                id = "premium_mcu",
                name = "Marvel Cinematic Universe (Saga Completa)",
                sourceType = "TMDB Collections",
                url = "https://api.themoviedb.org/3/list/8254719?api_key=INSERT_KEY_HERE&language=es-MX",
                posterUrl = "https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=600",
                numItems = 45,
                description = "Todas las películas y series canon de Marvel Studios ordenadas cronológicamente por fases.",
                category = "Marvel"
            ),
            DiscoverableCatalog(
                id = "premium_marvel_timeline",
                name = "Marvel Timeline & Story Order",
                sourceType = "Trakt Lists",
                url = "https://api.trakt.tv/users/movist-app/lists/marvel-cinematic-universe-chronological",
                posterUrl = "https://images.unsplash.com/photo-1608889175123-8ec330b86f84?q=80&w=600",
                numItems = 38,
                description = "Orden definitivo de la narrativa de Marvel incluyendo agentes de S.H.I.E.L.D, Daredevil y Disney+.",
                category = "Marvel"
            ),
            DiscoverableCatalog(
                id = "premium_marvel_anim",
                name = "Marvel Animation Essentials",
                sourceType = "MDBList",
                url = "https://api.themoviedb.org/3/discover/movie?api_key=INSERT_KEY_HERE&with_companies=420&with_genres=16",
                posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=600",
                numItems = 20,
                description = "Series icónicas de los 90s, X-Men '97, Spider-Man: TAS, What If...? y películas animadas clásicas.",
                category = "Marvel"
            ),
            DiscoverableCatalog(
                id = "premium_marvel_top",
                name = "Top Marvel Movies & Specials",
                sourceType = "Lumina",
                url = "https://api.themoviedb.org/3/discover/movie?api_key=INSERT_KEY_HERE&with_companies=420&sort_by=vote_average.desc&vote_count.gte=1000",
                posterUrl = "https://images.unsplash.com/photo-1569003339405-ea396a5a8a90?q=80&w=600",
                numItems = 15,
                description = "Las obras del universo Marvel mejor puntuadas por la crítica global de IMDb y Rotten Tomatoes.",
                category = "Marvel"
            ),
            
            // Anime Catalogs
            DiscoverableCatalog(
                id = "premium_anime_trending",
                name = "Anime Trending Worldwide",
                sourceType = "Trakt",
                url = "https://api.themoviedb.org/3/discover/tv?api_key=INSERT_KEY_HERE&with_genres=16&with_original_language=ja&sort_by=popularity.desc&language=es-MX",
                posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=600",
                numItems = 30,
                description = "Las series y películas de anime de las que todo el mundo está hablando esta temporada.",
                category = "Anime"
            ),
            DiscoverableCatalog(
                id = "premium_anime_popular",
                name = "Top Rated Anime Masterpieces",
                sourceType = "TMDB Lists",
                url = "https://api.themoviedb.org/3/discover/tv?api_key=INSERT_KEY_HERE&with_genres=16&with_original_language=ja&sort_by=vote_average.desc&vote_count.gte=100&language=es-MX",
                posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=600",
                numItems = 40,
                description = "Colección legendaria de los mejores animes calificados históricamente (MyAnimeList & TMDB).",
                category = "Anime"
            ),
            DiscoverableCatalog(
                id = "premium_anime_new",
                name = "Nuevos Estrenos Anime",
                sourceType = "MDBList",
                url = "https://api.themoviedb.org/3/discover/tv?api_key=INSERT_KEY_HERE&with_genres=16&with_original_language=ja&sort_by=first_air_date.desc",
                posterUrl = "https://images.unsplash.com/photo-1528360983277-13d401ccd795?q=80&w=600",
                numItems = 25,
                description = "Las series más recientes que acaban de ser transmitidas y estrenadas en Japón.",
                category = "Anime"
            ),
            DiscoverableCatalog(
                id = "premium_anime_movies",
                name = "Grandes Películas de Anime",
                sourceType = "Lumina",
                url = "https://api.themoviedb.org/3/discover/movie?api_key=INSERT_KEY_HERE&with_genres=16&with_original_language=ja&sort_by=popularity.desc",
                posterUrl = "https://images.unsplash.com/photo-1541562232579-512a21360020?q=80&w=600",
                numItems = 20,
                description = "Una selección premium de películas icónicas de Studio Ghibli, Makoto Shinkai y Mamoru Hosoda.",
                category = "Anime"
            ),

            // Star Wars & Sci-Fi
            DiscoverableCatalog(
                id = "premium_starwars",
                name = "Star Wars: Saga Skywalker & Series",
                sourceType = "TMDB Lists",
                url = "https://api.themoviedb.org/3/list/8254720?api_key=INSERT_KEY_HERE&language=es-MX",
                posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=600",
                numItems = 28,
                description = "Que la fuerza te acompañe. Películas de la saga principal, spin-offs y series oficiales de Disney+.",
                category = "Ciencia Ficción"
            ),
            DiscoverableCatalog(
                id = "premium_cyberpunk",
                name = "Sci-Fi & Cyberpunk Classics",
                sourceType = "Lumina",
                url = "https://api.themoviedb.org/3/discover/movie?api_key=INSERT_KEY_HERE&with_genres=878&sort_by=vote_average.desc&vote_count.gte=1500&language=es-MX",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=600",
                numItems = 25,
                description = "Películas distópicas, futuristas, robótica avanzada y mundos neones de culto.",
                category = "Ciencia Ficción"
            ),

            // Classics & Awards
            DiscoverableCatalog(
                id = "premium_disney",
                name = "Clásicos Animados Disney & Pixar",
                sourceType = "TMDB Collections",
                url = "https://api.themoviedb.org/3/discover/movie?api_key=INSERT_KEY_HERE&with_companies=2|3|34&with_genres=16&sort_by=popularity.desc&language=es-MX",
                posterUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?q=80&w=600",
                numItems = 35,
                description = "La magia de tu infancia. Desde Blancanieves hasta los últimos éxitos tridimensionales de Pixar.",
                category = "Familiar"
            ),
            DiscoverableCatalog(
                id = "premium_oscar",
                name = "Películas Ganadoras del Óscar",
                sourceType = "MDBList",
                url = "https://api.themoviedb.org/3/discover/movie?api_key=INSERT_KEY_HERE&sort_by=vote_average.desc&vote_count.gte=10000",
                posterUrl = "https://images.unsplash.com/photo-1598257006458-087169a1f08d?q=80&w=600",
                numItems = 45,
                description = "Todas las cintas memorables coronadas con el premio de la Academia de Cine a Mejor Película.",
                category = "Clásicos"
            ),
            DiscoverableCatalog(
                id = "premium_imdb_top250",
                name = "IMDb Top 250 de la Historia",
                sourceType = "MDBList",
                url = "https://api.themoviedb.org/3/discover/movie?api_key=INSERT_KEY_HERE&sort_by=vote_average.desc&vote_count.gte=15000",
                posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=600",
                numItems = 50,
                description = "El estándar de oro del cine mundial. Las 250 mejores películas de todos los tiempos.",
                category = "Clásicos"
            ),
            DiscoverableCatalog(
                id = "premium_horror_trends",
                name = "Terror & Thriller Spooktacular",
                sourceType = "Trakt",
                url = "https://api.themoviedb.org/3/discover/movie?api_key=INSERT_KEY_HERE&with_genres=27&sort_by=popularity.desc&language=es-MX",
                posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600",
                numItems = 25,
                description = "Cine de horror, suspenso psicológico y monstruos espeluznantes seleccionados para mentes valientes.",
                category = "Terror"
            )
        )
    }
    
    // Filtered catalogs based on search query
    val filteredCatalogs = remember(searchQuery, premiumCatalogs) {
        if (searchQuery.isBlank()) {
            premiumCatalogs
        } else {
            premiumCatalogs.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.sourceType.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0F14))
            .padding(16.dp.responsive()),
        verticalArrangement = Arrangement.spacedBy(16.dp.responsive())
    ) {
        // TOP HEADER ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp.responsive()))
            
            Column {
                Text(
                    text = "BUSCADOR DE CATÁLOGOS PREMIUM",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp.responsive(),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Explora y agrega carteleras completas de tus sagas y plataformas favoritas con un solo clic.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp.responsive()
                )
            }
        }
        
        // SEARCH INPUT BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Escribe 'Marvel', 'Anime', 'Star Wars', 'Terror', 'Disney'...", fontSize = 12.sp.responsive(), color = Color.White.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color(0xFF00E5FF)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.03f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.01f),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .tvFocusEffect(shape = RoundedCornerShape(10.dp))
        )
        
        // RESULTS SECTION
        if (filteredCatalogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "Sin resultados",
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(54.dp.responsive())
                    )
                    Text(
                        text = "No se encontraron catálogos exactos para \"$searchQuery\"",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp.responsive()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Button(
                        onClick = {
                            val newCatalogName = searchQuery.trim().replaceFirstChar { it.uppercase() }
                            val isInstalled = installedCatalogs.any { it.name.lowercase() == newCatalogName.lowercase() }
                            
                            if (isInstalled) {
                                Toast.makeText(context, "El catálogo \"$newCatalogName\" ya está instalado.", Toast.LENGTH_SHORT).show()
                            } else {
                                val catalog = Catalog(
                                    id = "custom_search_${System.currentTimeMillis()}",
                                    name = newCatalogName,
                                    sourceType = "Lumina",
                                    url = "https://api.themoviedb.org/3/search/movie?api_key=INSERT_KEY_HERE&query=${searchQuery}&language=es-MX",
                                    isVisible = true,
                                    showInHome = true,
                                    numItems = 20,
                                    layoutType = "Horizontal Poster Row"
                                )
                                viewModel.addCatalog(catalog)
                                Toast.makeText(context, "¡Catálogo inteligente \"$newCatalogName\" creado e instalado!", Toast.LENGTH_LONG).show()
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp.responsive()))
                        Spacer(modifier = Modifier.width(6.dp.responsive()))
                        Text("Crear catálogo inteligente para \"$searchQuery\"", fontSize = 11.sp.responsive(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                verticalArrangement = Arrangement.spacedBy(14.dp.responsive())
            ) {
                items(filteredCatalogs) { premium ->
                    val isAlreadyInstalled = installedCatalogs.any { it.name.lowercase().trim() == premium.name.lowercase().trim() || it.url == premium.url }
                    
                    PremiumCatalogGridCard(
                        premium = premium,
                        isInstalled = isAlreadyInstalled,
                        onAdd = {
                            if (premium.url.contains("INSERT_KEY_HERE") && !hasTmdbKey) {
                                Toast.makeText(context, "⚠️ El servicio TMDB no está configurado internamente.", Toast.LENGTH_LONG).show()
                                return@PremiumCatalogGridCard
                            }
                            if (premium.url.contains("mdblist.com") && !premium.url.contains("/garycrawfordgc/")) {
                                if (ApiConfig.MDBLIST_API_KEY.isEmpty()) {
                                    Toast.makeText(context, "⚠️ El servicio MDBList no está configurado internamente.", Toast.LENGTH_LONG).show()
                                    return@PremiumCatalogGridCard
                                }
                            }
                            val catalog = Catalog(
                                id = "premium_${premium.id}",
                                name = premium.name,
                                sourceType = when (premium.sourceType) {
                                    "Trakt", "Trakt Lists" -> "Trakt"
                                    "MDBList" -> "MDBList"
                                    "Lumina" -> "Local"
                                    else -> "TMDB"
                                },
                                url = premium.url,
                                isVisible = true,
                                showInHome = true,
                                numItems = premium.numItems,
                                layoutType = when {
                                    premium.category == "Anime" -> "Vertical Poster Row"
                                    premium.category == "Terror" -> "Compact Row"
                                    premium.name.contains("Trending", ignoreCase = true) -> "Horizontal Poster Row"
                                    else -> "Landscape Row"
                                }
                            )
                            viewModel.addCatalog(catalog)
                            Toast.makeText(context, "¡Instalado correctamente en Inicio: ${premium.name}!", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumCatalogGridCard(
    premium: DiscoverableCatalog,
    isInstalled: Boolean,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp.responsive())
            .tvFocusEffect(shape = RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.03f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (isInstalled) Color(0xFF00FF87).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.07f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp.responsive()),
            horizontalArrangement = Arrangement.spacedBy(12.dp.responsive())
        ) {
            // Poster preview
            AsyncImage(
                model = premium.posterUrl,
                contentDescription = premium.name,
                modifier = Modifier
                    .width(76.dp.responsive())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge Source Type
                        val badgeColor = when (premium.sourceType) {
                            "MDBList" -> Color(0xFFFF2E93)
                            "Trakt", "Trakt Lists" -> Color(0xFFED1C24)
                            "TMDB", "TMDB Lists", "TMDB Collections" -> Color(0xFF00E5FF)
                            else -> Color(0xFF9D4EDD)
                        }
                        
                        Text(
                            text = premium.sourceType.uppercase(),
                            color = badgeColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp.responsive(),
                            modifier = Modifier
                                .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                .border(0.5.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp.responsive(), vertical = 1.5.dp.responsive())
                        )
                        
                        // Elements count
                        Text(
                            text = "${premium.numItems} ITEMS",
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp.responsive()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp.responsive()))
                    
                    Text(
                        text = premium.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp.responsive(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(3.dp.responsive()))
                    
                    Text(
                        text = premium.description,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 9.5.sp.responsive(),
                        maxLines = 2,
                        lineHeight = 12.5.sp.responsive(),
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Add Button
                Button(
                    onClick = { if (!isInstalled) onAdd() },
                    enabled = !isInstalled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInstalled) Color(0xFF00FF87).copy(alpha = 0.12f) else Color(0xFF00E5FF),
                        contentColor = if (isInstalled) Color(0xFF00FF87) else Color.Black,
                        disabledContainerColor = Color(0xFF00FF87).copy(alpha = 0.08f),
                        disabledContentColor = Color(0xFF00FF87).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp.responsive())
                        .tvFocusEffect(shape = RoundedCornerShape(6.dp))
                ) {
                    if (isInstalled) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Instalado",
                            modifier = Modifier.size(12.dp.responsive())
                        )
                        Spacer(modifier = Modifier.width(4.dp.responsive()))
                        Text("Agregado", fontSize = 10.sp.responsive(), fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar",
                            modifier = Modifier.size(12.dp.responsive())
                        )
                        Spacer(modifier = Modifier.width(4.dp.responsive()))
                        Text("Agregar a Inicio", fontSize = 10.sp.responsive(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
