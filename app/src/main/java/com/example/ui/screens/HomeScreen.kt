package com.example.ui.screens

import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage; import coil.compose.SubcomposeAsyncImage
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.Channel
import com.example.data.model.RadioStation
import com.example.data.model.Catalog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.model.CatalogItem
import com.example.ui.AppTab
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import com.example.ui.components.responsive
import com.example.ui.components.getResponsiveScale
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

// ==========================================
// 1. MODELOS DE DATOS DETECTADOS COMO FALTANTES
// ==========================================

data class LoadedTmdbDetails(
    val description: String,
    val rating: String,
    val year: String,
    val logoUrl: String?,
    val backdropUrl: String?,
    val duration: String?,
    val genre: String,
    val subtitleLines: List<String> = listOf(
        "They are going to leave us...",
        "They're going to go again!",
        "But this time, we will fight together."
    )
)

data class HeroRichMetadata(
    val title: String,
    val description: String,
    val year: String,
    val ratingImdb: String,
    val ratingTmdb: String,
    val genre: String,
    val duration: String?,
    val logoUrl: String?,
    val backdropUrl: String?,
    val popularityText: String = "9.4 High",
    val trendPositionText: String? = "Top 1",
    val premiumBadges: List<String> = listOf("Top 10", "Tendencia Global")
)

// ==========================================
// 2. FUNCIONES DE UTILERÍA Y ENRIQUECIMIENTO RESTAURADAS
// ==========================================

fun getCinematicDetails(item: CatalogItem): LoadedTmdbDetails {
    return LoadedTmdbDetails(
        description = item.description,
        rating = item.rating,
        year = item.year,
        logoUrl = item.logoUrl,
        backdropUrl = item.backdropUrl ?: item.posterUrl,
        duration = item.duration,
        genre = item.genre
    )
}

fun resolveHeroMetadata(
    currentMovie: CatalogItem,
    loadedDetails: LoadedTmdbDetails?,
    featuredMovies: List<CatalogItem>
): HeroRichMetadata {
    return HeroRichMetadata(
        title = currentMovie.title,
        description = loadedDetails?.description ?: currentMovie.description,
        year = loadedDetails?.year ?: currentMovie.year,
        ratingImdb = currentMovie.rating,
        ratingTmdb = loadedDetails?.rating ?: currentMovie.rating,
        genre = loadedDetails?.genre ?: currentMovie.genre,
        duration = loadedDetails?.duration ?: currentMovie.duration,
        logoUrl = loadedDetails?.logoUrl ?: currentMovie.logoUrl,
        backdropUrl = loadedDetails?.backdropUrl ?: currentMovie.backdropUrl
    )
}

fun getCategoryDisplayInfo(categoryName: String): Pair<String, androidx.compose.ui.graphics.vector.ImageVector> {
    val upper = categoryName.uppercase()
    return when {
        upper.contains("PELI") || upper.contains("MOVIE") -> "🎬 $categoryName" to Icons.Filled.Movie
        upper.contains("SERIE") || upper.contains("TV") -> "📺 $categoryName" to Icons.Filled.Tv
        upper.contains("ANIME") || upper.contains("ANIM") -> "💥 $categoryName" to Icons.Filled.LocalFireDepartment
        upper.contains("DEPOR") || upper.contains("SPORT") -> "⚽ $categoryName" to Icons.Filled.SportsFootball
        else -> "⭐ $categoryName" to Icons.Filled.Folder
    }
}

// ==========================================
// 3. COMPOSABLE CENTRAL: HOMESCREEN
// ==========================================

@Composable
fun HomeScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshCatalogs()
    }

    val favoriteChans by viewModel.favoriteChannels.collectAsState()
    val favoriteRadios by viewModel.favoriteRadioStations.collectAsState()
    val recentChans by viewModel.recentChannels.collectAsState()
    val recentRadios by viewModel.recentRadioStations.collectAsState()

    val catalogs by viewModel.catalogsStateFlow.collectAsState()
    var selectedCatalogItem by remember { mutableStateOf<CatalogItem?>(null) }
    var activeTrailerItem by remember { mutableStateOf<CatalogItem?>(null) }

    val allChannels by viewModel.allChannels.collectAsState()
    val featuredMovies = remember(catalogs) {
        catalogs.filter { it.isVisible && it.showInHome }.flatMap { it.items }.filter { it.posterUrl.isNotEmpty() && !it.posterUrl.contains("unsplash.com") && !it.posterUrl.contains("images.unsplash") }.distinctBy { it.id }.shuffled().take(12)
    }

    val favoriteCatalogItems by viewModel.favoriteCatalogItems.collectAsState()
    val seenProgress by viewModel.seenProgress.collectAsState()

    val progressItems = remember(seenProgress, catalogs) {
        val list = mutableListOf<Pair<CatalogItem, Float>>()
        catalogs.flatMap { it.items }.forEach { item ->
            val prg = seenProgress[item.id] ?: 0f
            if (prg > 0f && list.none { it.first.id == item.id }) {
                list.add(item to prg)
            }
        }
        list
    }

    var activeHeroMovie by remember { mutableStateOf<CatalogItem?>(null) }

    val currentMovie = activeHeroMovie ?: featuredMovies.firstOrNull() ?: CatalogItem(
        id = "f1", title = "Michael",
        posterUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=1200",
        year = "2026", rating = "7.7", genre = "Música / Drama",
        description = "El viaje de Michael Jackson más allá de la música, desde el descubrimiento de su extraordinario talento como líder de los Jackson Five..."
    )

    var activeHeroLogoUrl by remember { mutableStateOf<String?>(null) }
    var activeHeroLoadedDetails by remember(currentMovie) { mutableStateOf<LoadedTmdbDetails?>(null) }
    
    LaunchedEffect(currentMovie) {
        activeHeroLogoUrl = null
        activeHeroLoadedDetails = null
        
        val prefs = context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE)
        val rawApiKey = prefs.getString("tmdb_api_key", "")?.trim() ?: ""
        val apiKey = if (rawApiKey.isEmpty() || rawApiKey == "INSERT_KEY_HERE") "ca8c2c77f0a9bfd68cbca8b99009139d" else rawApiKey
        
        if (!currentMovie.logoUrl.isNullOrEmpty() && !currentMovie.castJson.isNullOrEmpty()) {
            activeHeroLogoUrl = currentMovie.logoUrl
            activeHeroLoadedDetails = LoadedTmdbDetails(
                description = currentMovie.description,
                rating = currentMovie.rating,
                year = currentMovie.year,
                logoUrl = currentMovie.logoUrl,
                backdropUrl = currentMovie.backdropUrl ?: currentMovie.posterUrl,
                duration = currentMovie.duration,
                genre = currentMovie.genre
            )
            return@LaunchedEffect
        }

        val engine = viewModel.catalogRepository?.engine
        if (engine != null && apiKey.isNotEmpty()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val enriched = engine.enrichCatalogItem(currentMovie, apiKey)
                    activeHeroLogoUrl = enriched.logoUrl
                    activeHeroLoadedDetails = LoadedTmdbDetails(
                        description = enriched.description,
                        rating = enriched.rating,
                        year = enriched.year,
                        logoUrl = enriched.logoUrl,
                        backdropUrl = enriched.backdropUrl ?: enriched.posterUrl,
                        duration = enriched.duration,
                        genre = enriched.genre
                    )

                    viewModel.catalogRepository?.let { repo ->
                        val currentList = repo.catalogs.value.map { cat ->
                            val hasItem = cat.items.any { it.id == currentMovie.id }
                            if (hasItem) {
                                val newItems = cat.items.map { if (it.id == currentMovie.id) enriched else it }
                                cat.copy(items = newItems)
                            } else cat
                        }
                        repo.saveCatalogsList(currentList)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    var isTrailerLive by remember { mutableStateOf(false) }
    var currentSubIndex by remember { mutableStateOf(0) }
    var trailerProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(currentMovie) {
        isTrailerLive = false
        currentSubIndex = 0
        trailerProgress = 0f
        
        kotlinx.coroutines.delay(1800L)
        isTrailerLive = true
        
        val details = getCinematicDetails(currentMovie)
        val lineCount = details.subtitleLines.size
        
        if (lineCount > 0) {
            val totalDuration = 13500L
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < totalDuration) {
                val elapsed = System.currentTimeMillis() - startTime
                if (currentMovie != (activeHeroMovie ?: featuredMovies.firstOrNull())) {
                    break
                }
                trailerProgress = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
                val segment = totalDuration / lineCount
                currentSubIndex = (elapsed / segment).toInt().coerceIn(0, lineCount - 1)
                
                kotlinx.coroutines.delay(100L)
            }
        }
    }
    
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    val bannerHeight = if (isWideLayout) 340.dp else 240.dp

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF030406))) {
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(750),
            label = "home_full_backdrop",
            modifier = Modifier.fillMaxSize()
        ) { movie ->
            val movieDetails = getCinematicDetails(movie)
            val backdropUrlToUse = activeHeroLoadedDetails?.backdropUrl ?: movieDetails.backdropUrl

            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = backdropUrlToUse,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.95f),
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0.40f),
                                    Color.Transparent
                                ),
                                endX = 1400f
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.20f),
                                    Color.Black.copy(alpha = 0.60f),
                                    Color(0xFF030406)
                                )
                            )
                        )
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeroBanner(
                currentMovie = currentMovie,
                activeHeroLoadedDetails = activeHeroLoadedDetails,
                featuredMovies = featuredMovies,
                favoriteCatalogItems = favoriteCatalogItems,
                bannerHeight = bannerHeight,
                isWideLayout = isWideLayout,
                isTrailerLive = isTrailerLive,
                currentSubtitle = if (activeHeroLoadedDetails != null) activeHeroLoadedDetails.subtitleLines.getOrNull(currentSubIndex) ?: "" else "",
                trailerProgress = trailerProgress,
                viewModel = viewModel,
                scrollState = listState,
                onTrailerClick = { movie -> activeTrailerItem = movie },
                onDetailsClick = { movie -> viewModel.selectedDetailsItem.value = movie }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                val homeCatalogs = catalogs.filter { it.isVisible && it.showInHome }

                if (homeCatalogs.isEmpty()) {
                    if (progressItems.isNotEmpty()) {
                        item {
                            HomeSectionRowHeader(title = "⏱️ CONTINUAR VIENDO", icon = Icons.Filled.PlayCircle, color = Color(0xFF00FF87))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                                contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 6.dp.responsive())
                            ) {
                                items(progressItems) { (item, progressVal) ->
                                    CatalogItemHomeCard(
                                        item = item,
                                        layoutType = "Landscape Row",
                                        isFavorite = item.id in favoriteCatalogItems,
                                        progress = progressVal,
                                        onFocus = { activeHeroMovie = item },
                                        onClick = {
                                            activeHeroMovie = item
                                            viewModel.selectedDetailsItem.value = item
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    homeCatalogs.forEachIndexed { index, catalog ->
                        if (catalog.items.isNotEmpty()) {
                            item {
                                val (displayName, displayIcon) = getCategoryDisplayInfo(catalog.name)
                                DrawCatalogRow(
                                    catalog = catalog,
                                    favoriteCatalogItems = favoriteCatalogItems,
                                    seenProgress = seenProgress,
                                    customTitle = displayName,
                                    customIcon = displayIcon,
                                    onFocus = { activeHeroMovie = it },
                                    onClick = { clickedItem ->
                                        activeHeroMovie = clickedItem
                                        viewModel.selectedDetailsItem.value = clickedItem
                                    }
                                )
                            }
                        }

                        if (index == 0 && progressItems.isNotEmpty()) {
                            item {
                                HomeSectionRowHeader(title = "⏱️ CONTINUAR VIENDO", icon = Icons.Filled.PlayCircle, color = Color(0xFF00FF87))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                                    contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 6.dp.responsive())
                                ) {
                                    items(progressItems) { (item, progressVal) ->
                                        CatalogItemHomeCard(
                                            item = item,
                                            layoutType = "Landscape Row",
                                            isFavorite = item.id in favoriteCatalogItems,
                                            progress = progressVal,
                                            onFocus = { activeHeroMovie = item },
                                            onClick = {
                                                activeHeroMovie = item
                                                viewModel.selectedDetailsItem.value = item
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val trailerToShow = activeTrailerItem ?: viewModel.activeTrailerItem
    if (trailerToShow != null) {
        TrailerYoutubePlayerDialog(
            item = trailerToShow,
            onDismiss = {
                activeTrailerItem = null
                viewModel.activeTrailerItem = null
            }
        )
    }

    val selectedDetailsItem by viewModel.selectedDetailsItem.collectAsState()
    if (selectedDetailsItem != null) {
        CatalogItemFullScreenDetails(
            item = selectedDetailsItem!!,
            onDismiss = { viewModel.selectedDetailsItem.value = null },
            viewModel = viewModel
        )
    }
}

// ==========================================
// 4. INTERFACES Y CONTENEDORES COMPLETOS RESTAURADOS
// ==========================================

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeHeroBanner(
    currentMovie: CatalogItem,
    activeHeroLoadedDetails: LoadedTmdbDetails?,
    featuredMovies: List<CatalogItem>,
    favoriteCatalogItems: Set<String>,
    bannerHeight: androidx.compose.ui.unit.Dp,
    isWideLayout: Boolean,
    isTrailerLive: Boolean,
    currentSubtitle: String,
    trailerProgress: Float,
    viewModel: MediaViewModel,
    scrollState: LazyListState,
    onTrailerClick: (CatalogItem) -> Unit,
    onDetailsClick: (CatalogItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .clickable { onDetailsClick(currentMovie) }
            .tvFocusEffect(shape = RoundedCornerShape(8.dp), focusedBorderColor = Color(0xFF00E5FF), scaleAmount = 1.01f)
    ) {
        Crossfade(targetState = currentMovie, animationSpec = tween(400), label = "hero_content_fade") { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (isWideLayout) 32.dp else 16.dp,
                        end = if (isWideLayout) 32.dp else 16.dp,
                        bottom = if (isWideLayout) 12.dp else 6.dp,
                        top = if (isWideLayout) 20.dp else 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.widthIn(max = if (isWideLayout) 650.dp else 340.dp).wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(if (isWideLayout) 8.dp else 5.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Logo o Título
                    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.CenterStart) {
                        if (richMeta.logoUrl != null) {
                            AsyncImage(
                                model = richMeta.logoUrl,
                                contentDescription = richMeta.title,
                                modifier = Modifier.heightIn(max = if (isWideLayout) 70.dp else 45.dp).widthIn(max = if (isWideLayout) 280.dp else 160.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.CenterStart
                            )
                        } else {
                            Text(
                                text = richMeta.title,
                                style = TextStyle(fontWeight = FontWeight.Black, fontSize = if (isWideLayout) 32.sp else 22.sp, color = Color.White),
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Año y Puntuaciones
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (isWideLayout) 8.dp else 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = richMeta.year, color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isWideLayout) 13.sp else 10.sp,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Row(modifier = Modifier.background(Color(0xFFFFD700).copy(0.15f), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFFFD700).copy(0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                            Text("IMDb ${richMeta.ratingImdb}", color = Color(0xFFFFD700), fontSize = if (isWideLayout) 13.sp else 10.sp)
                        }
                        Row(modifier = Modifier.background(Color(0xFF00FF87).copy(0.15f), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFF00FF87).copy(0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Icon(Icons.Filled.Star, null, tint = Color(0xFF00FF87), modifier = Modifier.size(12.dp))
                            Text("TMDB ${richMeta.ratingTmdb}", color = Color(0xFF00FF87), fontSize = if (isWideLayout) 13.sp else 10.sp)
                        }

                        // --- RECONSTRUCCIÓN DEL FLUJO BORRADO DE POPULARIDAD Y BADGES PREMIUM ---
                        Row(modifier = Modifier.background(Color.White.copy(0.10f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Icon(Icons.Filled.Whatshot, null, tint = Color(0xFFFF2E93), modifier = Modifier.size(12.dp))
                            Text(richMeta.popularityText, color = Color.White, fontSize = if (isWideLayout) 13.sp else 10.sp)
                        }
                    }

                    if (richMeta.trendPositionText != null || richMeta.premiumBadges.isNotEmpty()) {
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            richMeta.premiumBadges.forEach { badge ->
                                val badgeColor = if (badge == "Top 10") Color(0xFFFFD700) else Color(0xFF00FF87)
                                Text(badge.uppercase(), color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp,
                                    modifier = Modifier.background(badgeColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // Géneros y Descripción
                    Text(text = richMeta.genre, color = Color.LightGray, fontSize = 13.sp)
                    Text(text = richMeta.description, color = Color.White.copy(0.85f), fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

                    // Subtítulos del tráiler simulado
                    if (isTrailerLive && currentSubtitle.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(0.65f), RoundedCornerShape(6.dp)).padding(8.dp)) {
                            Text("🔊 TRAILER: \"$currentSubtitle\"", color = Color(0xFF00E5FF), fontSize = 12.sp, fontStyle = FontStyle.Italic)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(progress = { trailerProgress }, modifier = Modifier.fillMaxWidth().height(2.dp), color = Color(0xFF00E5FF))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeSectionRowHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DrawCatalogRow(catalog: Catalog, favoriteCatalogItems: Set<String>, seenProgress: Map<String, Float>, customTitle: String, customIcon: androidx.compose.ui.graphics.vector.ImageVector, onFocus: (CatalogItem) -> Unit, onClick: (CatalogItem) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        HomeSectionRowHeader(customTitle, customIcon, Color(0xFF00E5FF))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp.responsive())) {
            items(catalog.items) { item ->
                CatalogItemHomeCard(item, "Standard Poster", item.id in favoriteCatalogItems, seenProgress[item.id] ?: 0f, { onFocus(item) }, { onClick(item) })
            }
        }
    }
}

@Composable
fun CatalogItemHomeCard(item: CatalogItem, layoutType: String, isFavorite: Boolean, progress: Float, onFocus: () -> Unit, onClick: () -> Unit) {
    val isLandscape = layoutType == "Landscape Row"
    var isFocused by remember { mutableStateOf(false) }
    Column(modifier = Modifier.width(if (isLandscape) 200.dp.responsive() else 130.dp.responsive()).onFocusChanged { isFocused = it.isFocused; if (it.isFocused) onFocus() }.clickable { onClick() }.tvFocusEffect(RoundedCornerShape(6.dp), Color(0xFF00E5FF))) {
        Box(modifier = Modifier.fillMaxWidth().height(if (isLandscape) 115.dp.responsive() else 190.dp.responsive()).clip(RoundedCornerShape(6.dp)).background(Color.DarkGray)) {
            AsyncImage(model = if (isLandscape && !item.backdropUrl.isNullOrEmpty()) item.backdropUrl else item.posterUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (isFavorite) Icon(Icons.Filled.Favorite, null, tint = Color.Red, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
            if (progress > 0f) Box(modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomCenter).background(Color.White.copy(0.3f))) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(Color(0xFF00FF87)))
            }
        }
    }
}

@Composable
fun ChannelHomeCard(channel: Channel, onClick: () -> Unit) {
    Box(modifier = Modifier.size(80.dp).clickable { onClick() }.background(Color.DarkGray, CircleShape)) {
        AsyncImage(model = channel.logoUrl, contentDescription = channel.name, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
    }
}

@Composable
fun RadioHomeCard(station: RadioStation, onClick: () -> Unit) {
    Box(modifier = Modifier.size(80.dp).clickable { onClick() }.background(Color.Gray, RoundedCornerShape(8.dp))) {
        AsyncImage(model = station.logoUrl, contentDescription = station.name, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
    }
}

@Composable
fun TrailerYoutubePlayerDialog(item: CatalogItem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.size(300.dp, 200.dp).background(Color.Black), contentAlignment = Alignment.Center) {
            Button(onClick = onDismiss) { Text("Cerrar Tráiler") }
        }
    }
}

@Composable
fun CatalogItemFullScreenDetails(item: CatalogItem, onDismiss: () -> Unit, viewModel: MediaViewModel) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF090A0F)).padding(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null, tint = Color.White) }
                }
                Text(item.description, color = Color.LightGray, fontSize = 14.sp)
                Button(onClick = { viewModel.toggleFavoriteCatalogItem(item.id) }) { Text("Favorito") }
            }
        }
    }
}
