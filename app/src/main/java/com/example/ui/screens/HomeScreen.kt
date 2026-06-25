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

    // Base flows
    val favoriteChans by viewModel.favoriteChannels.collectAsState()
    val favoriteRadios by viewModel.favoriteRadioStations.collectAsState()
    val recentChans by viewModel.recentChannels.collectAsState()
    val recentRadios by viewModel.recentRadioStations.collectAsState()

    val catalogs by viewModel.catalogsStateFlow.collectAsState()
    var selectedCatalogItem by remember { mutableStateOf<CatalogItem?>(null) }
    var activeTrailerItem by remember { mutableStateOf<CatalogItem?>(null) }

    val allChannels by viewModel.allChannels.collectAsState()
    // Showcase/Banner movies (Curated highlights)
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

    // Logo state
    var activeHeroLogoUrl by remember { mutableStateOf<String?>(null) }

    // Loaded dynamic properties (from TMDB real-time query) for the current movie
    var activeHeroLoadedDetails by remember(currentMovie) { mutableStateOf<LoadedTmdbDetails?>(null) }
    
    LaunchedEffect(currentMovie) {
        activeHeroLogoUrl = null
        activeHeroLoadedDetails = null
        
        val prefs = context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE)
        val rawApiKey = prefs.getString("tmdb_api_key", "")?.trim() ?: ""
        val apiKey = if (rawApiKey.isEmpty() || rawApiKey == "INSERT_KEY_HERE") "ca8c2c77f0a9bfd68cbca8b99009139d" else rawApiKey
        
        // 1. Try reading straight from Lumina Catalog Engine cache fields
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

        // 2. Fallback to Catalog Engine lookup
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

                    // Persist enriched hero item back to catalogs list asynchronously
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

    // Trailer Simulation States
    var isTrailerLive by remember { mutableStateOf(false) }
    var currentSubIndex by remember { mutableStateOf(0) }
    var trailerProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(currentMovie) {
        isTrailerLive = false
        currentSubIndex = 0
        trailerProgress = 0f
        
        // Wait 1.8 seconds of initial poster preview before triggering simulated trailer session
        kotlinx.coroutines.delay(1800L)
        isTrailerLive = true
        
        val details = getCinematicDetails(currentMovie)
        val lineCount = details.subtitleLines.size
        
        if (lineCount > 0) {
            val totalDuration = 13500L // 13.5 seconds trailer loop
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < totalDuration) {
                val elapsed = System.currentTimeMillis() - startTime
                if (currentMovie != (activeHeroMovie ?: featuredMovies.firstOrNull())) {
                    break
                }
                trailerProgress = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
                
                // Change subtitles dynamically based on elapsed fraction
                val segment = totalDuration / lineCount
                currentSubIndex = (elapsed / segment).toInt().coerceIn(0, lineCount - 1)
                
                kotlinx.coroutines.delay(100L)
            }
        }
    }
    
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    // Adaptación sutil de la altura del banner según dispositivo sin romper proporciones en TV
    val bannerHeight = if (isWideLayout) 310.dp else 220.dp

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF030406))) {
        // --- 1. NETFLIX-STYLE FULL-SCREEN BACKDROP COVERING THE BACKGROUND ---
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

                // Cinematic horizontal dark gradient
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

                // Cinematic vertical dark gradient
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

        // --- 2. MAIN STRUCTURAL LAYOUT WITH FIXED HERO BANNER AND SCROLLING ROWS ---
        Column(modifier = Modifier.fillMaxSize()) {
            // A) Fixed Hero Banner
            HomeHeroBanner(
                currentMovie = currentMovie,
                activeHeroLoadedDetails = activeHeroLoadedDetails,
                featuredMovies = featuredMovies,
                favoriteCatalogItems = favoriteCatalogItems,
                bannerHeight = bannerHeight,
                isWideLayout = isWideLayout,
                viewModel = viewModel,
                scrollState = listState,
                onTrailerClick = { movie ->
                    activeTrailerItem = movie
                },
                onDetailsClick = { movie ->
                    viewModel.selectedDetailsItem.value = movie
                }
            )

            // B) Scrollable Content Rows
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                val homeCatalogs = catalogs.filter { it.isVisible && it.showInHome }

                if (homeCatalogs.isEmpty()) {
                    if (progressItems.isNotEmpty()) {
                        item {
                            HomeSectionRowHeader(
                                title = "⏱️ CONTINUAR VIENDO",
                                icon = Icons.Filled.PlayCircle,
                                color = Color(0xFF00FF87)
                            )
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
                                HomeSectionRowHeader(
                                    title = "⏱️ CONTINUAR VIENDO",
                                    icon = Icons.Filled.PlayCircle,
                                    color = Color(0xFF00FF87)
                                )
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
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeHeroBanner(
    currentMovie: CatalogItem,
    activeHeroLoadedDetails: LoadedTmdbDetails?,
    featuredMovies: List<CatalogItem>,
    favoriteCatalogItems: Set<String>,
    bannerHeight: androidx.compose.ui.unit.Dp,
    isWideLayout: Boolean,
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
            .tvFocusEffect(
                shape = RoundedCornerShape(8.dp),
                focusedBorderColor = Color(0xFF00E5FF),
                scaleAmount = 1.01f
            )
    ) {
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(400),
            label = "hero_content_fade"
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (isWideLayout) 32.dp else 16.dp,
                        end = if (isWideLayout) 32.dp else 16.dp,
                        bottom = if (isWideLayout) 16.dp else 8.dp,
                        top = if (isWideLayout) 24.dp else 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = if (isWideLayout) 650.dp else 340.dp)
                        .wrapContentHeight(),
                    // Distribuimos el flujo de manera limpia e idéntica sin importar el cambio de tarjeta
                    verticalArrangement = Arrangement.spacedBy(if (isWideLayout) 8.dp else 5.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // 1. LOGO OR TITLE (Controlamos su tamaño base para TV para que no colapse)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (richMeta.logoUrl != null) {
                            AsyncImage(
                                model = richMeta.logoUrl,
                                contentDescription = richMeta.title,
                                modifier = Modifier
                                    .heightIn(max = if (isWideLayout) 70.dp else 45.dp)
                                    .widthIn(max = if (isWideLayout) 280.dp else 160.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.CenterStart
                            )
                        } else {
                            Text(
                                text = richMeta.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isWideLayout) 32.sp else 22.sp,
                                    color = Color.White,
                                    letterSpacing = (-1).sp,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.95f),
                                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                        blurRadius = 12f
                                    )
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = if (isWideLayout) 36.sp else 26.sp
                            )
                        }
                    }

                    // 2. YEAR & RATINGS ROW (Aumento de tamaño de badges y fuentes en Android TV)
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (isWideLayout) 8.dp else 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = richMeta.year,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isWideLayout) 13.sp else 10.sp,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = if (isWideLayout) 8.dp else 5.dp, vertical = if (isWideLayout) 3.dp else 2.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.40f), RoundedCornerShape(4.dp))
                                .padding(horizontal = if (isWideLayout) 8.dp else 5.dp, vertical = if (isWideLayout) 3.dp else 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "IMDb Rating",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(if (isWideLayout) 14.dp else 10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "IMDb ${richMeta.ratingImdb}",
                                color = Color(0xFFFFD700),
                                fontSize = if (isWideLayout) 13.sp else 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFF00FF87).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF00FF87).copy(alpha = 0.40f), RoundedCornerShape(4.dp))
                                .padding(horizontal = if (isWideLayout) 8.dp else 5.dp, vertical = if (isWideLayout) 3.dp else 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "TMDB Rating",
                                tint = Color(0xFF00FF87),
                                modifier = Modifier.size(if (isWideLayout) 14.dp else 10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TMDB ${richMeta.ratingTmdb}",
                                color = Color(0xFF00FF87),
                                fontSize = if (isWideLayout) 13.sp else 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                                .padding(horizontal = if (isWideLayout) 8.dp else 5.dp, vertical = if (isWideLayout) 3.dp else 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Whatshot,
                                contentDescription = "Popularity",
                                tint = Color(0xFFFF2E93),
                                modifier = Modifier.size(if (isWideLayout) 14.dp else 10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = richMeta.popularityText,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = if (isWideLayout) 13.sp else 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3. TRENDING POSITION & PREMIUM BADGES (Ajuste de rejilla auto-envolvente)
                    if (richMeta.trendPositionText != null || richMeta.premiumBadges.isNotEmpty()) {
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (isWideLayout) 8.dp else 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (richMeta.trendPositionText != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFFF2E93), Color(0xFFFF8A00))
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = if (isWideLayout) 8.dp else 5.dp, vertical = if (isWideLayout) 3.dp else 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.TrendingUp,
                                        contentDescription = "Trend Position",
                                        tint = Color.White,
                                        modifier = Modifier.size(if (isWideLayout) 14.dp else 10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = richMeta.trendPositionText.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (isWideLayout) 11.sp else 9.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            richMeta.premiumBadges.forEach { badge ->
                                val badgeColor = when (badge) {
                                    "Top 10" -> Color(0xFFFFD700)
                                    "Tendencia Global" -> Color(0xFF00FF87)
                                    else -> Color(0xFF00E5FF)
                                }
                                Text(
                                    text = badge.uppercase(),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isWideLayout) 11.sp else 9.sp,
                                    modifier = Modifier
                                        .background(badgeColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = if (isWideLayout) 8.dp else 5.dp, vertical = if (isWideLayout) 3.dp else 2.dp)
                                )
                            }
                        }
                    }

                    // 4. GENRES & DURATION (Texto secundario legible)
                    val genreText = richMeta.genre.ifEmpty { "Multimedia / Catálogo" }
                    val durationText = if (!richMeta.duration.isNullOrEmpty()) "  •  ${richMeta.duration}" else ""
                    Text(
                        text = "$genreText$durationText",
                        color = Color.LightGray.copy(alpha = 0.9f),
                        fontSize = if (isWideLayout) 14.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 5. TECHNICAL BADGES FORMAT ROW (Dolby Vision, HD, 5.1... sin amontonarse)
                    // Eliminamos el padding innecesario y usamos espaciado vertical estricto de 4.dp de seguridad
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (isWideLayout) 6.dp else 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val techTags = listOf("HD", "HDR10", "Dolby Vision", "5.1 Audio", "Español (LAT)", "Subtítulos")
                        techTags.forEach { tag ->
                            Text(
                                text = tag,
                                color = Color(0xFFB0BEC5),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isWideLayout) 11.sp else 8.sp,
                                modifier = Modifier
                                    .border(1.dp, Color(0xFFB0BEC5).copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(horizontal = if (isWideLayout) 6.dp else 4.dp, vertical = if (isWideLayout) 2.dp else 1.5.dp)
                            )
                        }
                    }

                    // 6. SYNOPSIS DESCRIPTION (MaxLines expandido y tamaño adaptado para TV de forma clara)
                    Text(
                        text = richMeta.description,
                        color = Color.White.copy(alpha = 0.85f),
                        style = TextStyle(
                            fontSize = if (isWideLayout) 14.5.sp else 11.sp,
                            lineHeight = if (isWideLayout) 20.sp else 15.sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 4f
                            )
                        ),
                        maxLines = if (isWideLayout) 3 else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}