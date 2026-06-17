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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.Channel
import com.example.data.model.RadioStation
import com.example.data.model.Catalog
import com.example.data.model.CatalogItem
import com.example.ui.AppTab
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
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
    var showDetailsDialog by remember { mutableStateOf(false) }

    val allChannels by viewModel.allChannels.collectAsState()
    // Showcase/Banner Channel (First channel by default)
    // Showcase/Banner movies (Curated highlights from either the active catalogs or premium curated cinema highlights)
    val featuredMovies = remember(catalogs) {
        catalogs.filter { it.isVisible && it.showInHome }.flatMap { it.items }.filter { it.posterUrl.isNotEmpty() && !it.posterUrl.contains("unsplash.com") && !it.posterUrl.contains("images.unsplash") }.distinctBy { it.id }.shuffled().take(12)
    }

    val favoriteCatalogItems by viewModel.favoriteCatalogItems.collectAsState()
    val seenProgress by viewModel.seenProgress.collectAsState()

    var activeHeroMovie by remember { mutableStateOf<CatalogItem?>(null) }

    val currentMovie = activeHeroMovie ?: featuredMovies.firstOrNull() ?: CatalogItem(
        id = "f1", title = "Michael",
        posterUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=1200",
        year = "2026", rating = "7.7", genre = "Música / Drama",
        description = "El viaje de Michael Jackson más allá de la música, desde el descubrimiento de su extraordinario talento como líder de los Jackson Five..."
    )

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
                    // Break loop early if movie has changed
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

    val scale by animateFloatAsState(
        targetValue = if (isTrailerLive) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 13500, easing = androidx.compose.animation.core.LinearEasing),
        label = "ken_burns_scale"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // --- 1. CAPA DE FONDO CINEMÁTICO ADAPTATIVO CON CROSSFADE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(410.dp)
                .background(Color(0xFF030406))
        ) {
            Crossfade(
                targetState = currentMovie,
                animationSpec = tween(650),
                label = "movie_backdrop_fade"
            ) { movie ->
                val movieDetails = getCinematicDetails(movie)
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isTrailerLive && movieDetails.trailerUrl.isNotEmpty() && movie == currentMovie) {
                        var isReady by remember(movie) { mutableStateOf(false) }
                        
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            update = { videoView ->
                                val url = movieDetails.trailerUrl
                                if (videoView.tag != url) {
                                    videoView.tag = url
                                    try {
                                        videoView.stopPlayback()
                                        videoView.setVideoPath(url)
                                        videoView.setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            mp.setVolume(0f, 0f) // Silenciado para ambiente premium
                                            videoView.start()
                                            isReady = true
                                        }
                                        videoView.setOnErrorListener { _, _, _ ->
                                            true
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            onRelease = { videoView ->
                                try {
                                    videoView.stopPlayback()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        if (!isReady) {
                            AsyncImage(
                                model = movieDetails.backdropUrl,
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        AsyncImage(
                            model = movieDetails.backdropUrl,
                            contentDescription = movie.title,
                            modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    ),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Degradado Cine-Lux para garantizar legibilidad extrema de textos en el lado izquierdo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.96f),
                                        Color.Black.copy(alpha = 0.72f),
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Transparent
                                    ),
                                    endX = 1000f
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.35f),
                                        Color(0xFF030406) // Fusión perfecta con el color de fondo obsidian de abajo
                                    ),
                                    startY = 220f
                                )
                            )
                    )
                }
            }

            // Barra fina de progreso del tráiler en el fondo
            if (isTrailerLive) {
                LinearProgressIndicator(
                    progress = { trailerProgress },
                    color = Color(0xFF00E5FF),
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomCenter)
                )
            }
        }

        // --- 2. LISTADO DESLIZANTE DE CATEGORÍAS EN PRIMERA PLANA (OVERLAPPING SCROLL) ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // -- SECCIÓN 1: CABECERA TRANSLÚCIDA CON DETALLES DEL CONTENIDO (INFO OVERLAY PANEL) --
            item {
                if (featuredMovies.isNotEmpty()) {
                    val details = getCinematicDetails(currentMovie)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(390.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp, end = 24.dp, bottom = 12.dp, top = 16.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.Start
                            ) {
                                // A) Título elegante con sombra
                                Text(
                                    text = details.logoText,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 38.sp,
                                        color = Color.White,
                                        letterSpacing = (-0.5).sp,
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.85f),
                                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                            blurRadius = 8f
                                        )
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                // B) Línea de metadatos (Año • Género • Duración)
                                Text(
                                    text = details.dateAndMetadata,
                                    color = Color.White.copy(alpha = 0.90f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // C) Etiquetas de plataforma, IMDb y presupuesto
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Text(
                                        text = details.providerBadge.uppercase(),
                                        color = Color.Black,
                                        fontWeight = FontWeight.W900,
                                        fontSize = 9.sp,
                                        modifier = Modifier
                                            .background(Color.White, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "IMDb ${currentMovie.rating}",
                                            color = Color(0xFFFFD700),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Text(
                                        text = details.budget,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                // D) Sinopsis del contenido con buen interlineado
                                Text(
                                    text = currentMovie.description,
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontSize = 11.5.sp,
                                    maxLines = 2,
                                    lineHeight = 15.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .padding(end = 36.dp, bottom = 14.dp)
                                        .widthIn(max = 580.dp)
                                )

                                // E) Botones de acción y puntos indicadores del carrusel
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                val movieChannel = Channel(
                                                    id = "trailer_${currentMovie.id}",
                                                    name = currentMovie.title,
                                                    streamUrl = currentMovie.streamUrl ?: details.trailerUrl,
                                                    logoUrl = currentMovie.posterUrl,
                                                    category = "Cine Premium",
                                                    description = currentMovie.description,
                                                    number = 999
                                                )
                                                viewModel.selectChannel(movieChannel)
                                                viewModel.isFullscreenPlayerActive = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF00E5FF),
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (currentMovie.streamUrl != null) "REPRODUCIR CINE" else "REPRODUCIR TRÁILER",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.W900
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                Toast.makeText(context, "Agregada a favoritos: ${currentMovie.title}", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("AÑADIR A LISTA", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                selectedCatalogItem = currentMovie
                                                showDetailsDialog = true
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("DETALLES", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Indicadores de posición del carrusel para navegación manual táctil/remoto
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        featuredMovies.forEachIndexed { idx, movie ->
                                            val active = (movie.id == currentMovie.id)
                                            Box(
                                                modifier = Modifier
                                                    .size(if (active) 7.dp else 5.dp)
                                                    .clip(CircleShape)
                                                    .background(if (active) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.35f))
                                                    /* no clickable on TV/mobile to prevent focus trap */
                                            )
                                        }
                                    }
                                }

                                // Subtítulos dinámicos removidos para un diseño limpio y premium
                            }
                        }
                    }
                }
            }

        // --- SECCIÓN 2: CINEMA FILAS DE PELÍCULAS DINÁMICAS (MAIN ATTRACTION) ---
        catalogs.filter { it.isVisible && it.showInHome }.forEach { catalog ->
            item {
                val icon = when (catalog.sourceType) {
                    "TMDB" -> Icons.Filled.Movie
                    "Trakt" -> Icons.Filled.Tv
                    "MDBList" -> Icons.Filled.FilterAlt
                    else -> Icons.Filled.VideoLibrary
                }
                HomeSectionRowHeader(
                    title = catalog.name.uppercase(), 
                    icon = icon, 
                    color = Color(0xFF00E5FF)
                )

                val isSupportedRowType = catalog.layoutType in listOf(
                    "Horizontal Poster Row",
                    "Vertical Poster Row",
                    "Landscape Row",
                    "Banner Row",
                    "Large Featured Row",
                    "Compact Row",
                    "Horizontal"
                )

                if (isSupportedRowType) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        items(catalog.items.take(catalog.numItems)) { item ->
                            CatalogItemHomeCard(
                                item = item,
                                layoutType = catalog.layoutType,
                                isFavorite = item.id in favoriteCatalogItems,
                                progress = seenProgress[item.id] ?: 0f,
                                onFocus = {
                                    // Focus does not trigger hero update
                                },
                                onClick = {
                                    activeHeroMovie = item
                                }
                            )
                        }
                    }
                } else if (catalog.layoutType == "Vertical") {
                    CatalogVerticalGrid(
                        items = catalog.items.take(catalog.numItems),
                        layoutType = "Vertical",
                        favoriteCatalogItems = favoriteCatalogItems,
                        seenProgress = seenProgress,
                        onItemFocus = { item ->
                            // Separated focus
                        },
                        onClick = { item ->
                            activeHeroMovie = item
                        }
                    )
                } else if (catalog.layoutType == "Top Numerado" || catalog.layoutType.contains("top", ignoreCase = true) || catalog.name.contains("top", ignoreCase = true) || catalog.name.contains("Mejor Valorad", ignoreCase = true) || catalog.name.contains("Top 250", ignoreCase = true)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        itemsIndexed(catalog.items.take(catalog.numItems)) { index, item ->
                            CatalogItemNumberedCard(
                                item = item,
                                rank = index + 1,
                                isFavorite = item.id in favoriteCatalogItems,
                                progress = seenProgress[item.id] ?: 0f,
                                onFocus = {
                                    // Separated focus
                                },
                                onClick = {
                                    activeHeroMovie = item
                                }
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        items(catalog.items.take(catalog.numItems)) { item ->
                            CatalogItemHomeCard(
                                item = item,
                                layoutType = "Horizontal Poster Row",
                                isFavorite = item.id in favoriteCatalogItems,
                                progress = seenProgress[item.id] ?: 0f,
                                onFocus = {
                                    // Separated focus
                                },
                                onClick = {
                                    activeHeroMovie = item
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- SUB-HEADER: RECREACIÓN TELEVISIVA Y DE EMISORAS DE RADIO (DESPLAZADA AL FONDO) ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 3. RECENTS SECTION (HORIZONTAL SCROLLER)
        val hasRecents = recentChans.isNotEmpty() || recentRadios.isNotEmpty()
        if (hasRecents) {
            item {
                HomeSectionRowHeader(title = "REPRODUCIDO RECIENTEMENTE", icon = Icons.Filled.History, color = Color.White.copy(alpha = 0.7f))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    items(recentChans) { chan ->
                        ChannelHomeCard(
                            channel = chan,
                            viewModel = viewModel,
                            onPlayClick = {
                                viewModel.selectChannel(chan)
                                viewModel.selectTab(AppTab.TV)
                            }
                        )
                    }

                    items(recentRadios) { rad ->
                        RadioHomeCard(
                            station = rad,
                            viewModel = viewModel,
                            onPlayClick = {
                                viewModel.selectRadioStation(rad)
                                viewModel.selectTab(AppTab.RADIO)
                            }
                        )
                    }
                }
            }
        }

        // 4. FAVORITES WATCHLIST (HORIZONTAL SCROLLER)
        val hasFavorites = favoriteChans.isNotEmpty() || favoriteRadios.isNotEmpty()
        if (hasFavorites) {
            item {
                HomeSectionRowHeader(title = "MIS FAVORITOS GUARDADOS", icon = Icons.Filled.Favorite, color = Color.White.copy(alpha = 0.7f))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    items(favoriteChans) { chan ->
                        ChannelHomeCard(
                            channel = chan,
                            viewModel = viewModel,
                            onPlayClick = {
                                viewModel.selectChannel(chan)
                                viewModel.selectTab(AppTab.TV)
                            }
                        )
                    }

                    items(favoriteRadios) { rad ->
                        RadioHomeCard(
                            station = rad,
                            viewModel = viewModel,
                            onPlayClick = {
                                viewModel.selectRadioStation(rad)
                                viewModel.selectTab(AppTab.RADIO)
                            }
                        )
                    }
                }
            }
        }

        // 5. CANALES POPULARES IPTV (DESPLAZADOS AL RECLUSO)
        item {
            HomeSectionRowHeader(title = "CANALES POPULARES IPTV", icon = Icons.Filled.Tv, color = Color.White.copy(alpha = 0.7f))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                items(allChannels) { chan ->
                    ChannelHomeCard(
                        channel = chan,
                        viewModel = viewModel,
                        onPlayClick = {
                            viewModel.selectChannel(chan)
                            viewModel.selectTab(AppTab.TV)
                        }
                    )
                }
            }
        }

        // 6. RADIO POPULARES (DESPLAZADOS ABAJO)
        item {
            HomeSectionRowHeader(title = "EMISORAS DE RADIO POPULARES", icon = Icons.Filled.Radio, color = Color.White.copy(alpha = 0.7f))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                items(viewModel.repository.radioStationsList) { rad ->
                    RadioHomeCard(
                        station = rad,
                        viewModel = viewModel,
                        onPlayClick = {
                            viewModel.selectRadioStation(rad)
                            viewModel.selectTab(AppTab.RADIO)
                        }
                    )
                }
            }
        }

        // 7. RECOMMENDED STREAMING (ABSOLUTE BOTTOM)
        item {
            HomeSectionRowHeader(title = "RECOMENDADOS PARA TI", icon = Icons.Filled.AutoAwesome, color = Color.White.copy(alpha = 0.7f))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Blend channels and radios
                items(allChannels.takeLast(3)) { chan ->
                    ChannelHomeCard(
                        channel = chan,
                        viewModel = viewModel,
                        onPlayClick = {
                            viewModel.selectChannel(chan)
                            viewModel.selectTab(AppTab.TV)
                        }
                    )
                }

                items(viewModel.repository.radioStationsList.takeLast(2)) { rad ->
                    RadioHomeCard(
                        station = rad,
                        viewModel = viewModel,
                        onPlayClick = {
                            viewModel.selectRadioStation(rad)
                            viewModel.selectTab(AppTab.RADIO)
                        }
                    )
                }
            }
        }
    }
    }

    if (showDetailsDialog && selectedCatalogItem != null) {
        CatalogItemDetailsDialog(
            item = selectedCatalogItem!!,
            viewModel = viewModel,
            onDismiss = {
                showDetailsDialog = false
                selectedCatalogItem = null
            }
        )
    }
}

// Subordinate Layout helpers
@Composable
fun HomeSectionRowHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color = Color(0xFF00E5FF)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Vertical Neon Anchor Bar for high-end cinematic vibe
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun ChannelHomeCard(
    channel: Channel,
    viewModel: MediaViewModel,
    onPlayClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    LaunchedEffect(channel.id, viewModel.favoriteChannels.collectAsState().value) {
        isFavorite = viewModel.isChannelFavorite(channel.id)
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(115.dp)
            .tvFocusEffect(shape = RoundedCornerShape(12.dp))
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )

            // Fade Gradient overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Top controls (Channel tag and Favorite bullet)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CH ${channel.number}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )

                    IconButton(
                        onClick = { viewModel.toggleChannelFavorite(channel.id) },
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Channel metadata
                Column {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = channel.category,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RadioHomeCard(
    station: RadioStation,
    viewModel: MediaViewModel,
    onPlayClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    LaunchedEffect(station.id, viewModel.favoriteRadioStations.collectAsState().value) {
        isFavorite = viewModel.isRadioFavorite(station.id)
    }

    val cardColor = remember(station.themeColorHex) {
        try {
            Color(android.graphics.Color.parseColor(station.themeColorHex))
        } catch (e: Exception) {
            Color(0xFF6B4EFE)
        }
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(115.dp)
            .tvFocusEffect(shape = RoundedCornerShape(12.dp))
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, CardColorGradientOverlay(cardColor))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = station.logoUrl,
                contentDescription = station.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.45f
            )

            // Glow gradient overlay matching station
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                cardColor.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // Station particulars
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = station.frequency,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(cardColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    IconButton(
                        onClick = { viewModel.toggleRadioFavorite(station.id) },
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritos",
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = station.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = station.genre,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

private fun CardColorGradientOverlay(color: Color): Brush {
    return Brush.radialGradient(
        colors = listOf(color, Color.White.copy(alpha = 0.08f)),
        radius = 180f
    )
}

@Composable
fun CatalogItemHomeCard(
    item: CatalogItem,
    layoutType: String = "Vertical",
    isFavorite: Boolean = false,
    progress: Float = 0f,
    onFocus: () -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardWidth = when (layoutType) {
        "Horizontal Poster Row", "Horizontal" -> 110.dp
        "Vertical Poster Row", "Vertical" -> 130.dp
        "Landscape Row" -> 180.dp
        "Banner Row" -> 260.dp
        "Large Featured Row" -> 190.dp
        "Compact Row" -> 85.dp
        else -> 110.dp
    }
    
    val imageHeight = when (layoutType) {
        "Horizontal Poster Row", "Horizontal" -> 154.dp
        "Vertical Poster Row", "Vertical" -> 190.dp
        "Landscape Row" -> 105.dp
        "Banner Row" -> 95.dp
        "Large Featured Row" -> 260.dp
        "Compact Row" -> 120.dp
        else -> 154.dp
    }

    val appliedModifier = if (modifier == Modifier) {
        Modifier.width(cardWidth)
    } else modifier
    Card(
        modifier = appliedModifier
            .onFocusChanged { state ->
                if (state.isFocused || state.hasFocus) {
                    onFocus()
                }
            }
            .clickable { onClick() }
            .tvFocusEffect(
                shape = RoundedCornerShape(8.dp),
                focusedBorderColor = Color(0xFF00E5FF),
                scaleAmount = 1.10f
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                // Movie/Show Poster
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gold Rating Overlay Tag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.rating,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favorite Overlay Tag
                if (isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorito",
                            tint = Color.Red,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                // Dynamic Year overlay gradient background and Progress Indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(4.dp)
                    ) {
                        Text(
                            text = item.year,
                            color = Color.White.copy(alpha = 0.80f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Seen progress bar
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(Color(0xFF00E5FF))
                            )
                        }
                    }
                }
            }

            // Title padding segment
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.genre,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 8.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class ActorInfo(val name: String, val role: String, val photoUrl: String)

fun getMockCast(itemTitle: String, genre: String): List<ActorInfo> {
    val cleanTitle = itemTitle.lowercase()
    return when {
        cleanTitle.contains("dune") -> listOf(
            ActorInfo("T. Chalamet", "Paul Atreides", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200"),
            ActorInfo("Zendaya", "Chani", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200"),
            ActorInfo("Austin Butler", "Feyd-Rautha", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200"),
            ActorInfo("Florence Pugh", "Irulan", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200"),
            ActorInfo("R. Ferguson", "Lady Jessica", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200")
        )
        cleanTitle.contains("oppenheimer") -> listOf(
            ActorInfo("Cillian Murphy", "Oppenheimer", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=200"),
            ActorInfo("Emily Blunt", "Kitty Opp.", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200"),
            ActorInfo("R. Downey Jr.", "Lewis Strauss", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?q=80&w=200"),
            ActorInfo("Matt Damon", "Leslie Groves", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?q=80&w=200")
        )
        cleanTitle.contains("spider") -> listOf(
            ActorInfo("S. Moore", "Miles Morales", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200"),
            ActorInfo("H. Steinfeld", "Gwen Stacy", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200"),
            ActorInfo("Oscar Isaac", "Miguel O'Hara", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200")
        )
        cleanTitle.contains("interstellar") || cleanTitle.contains("interestelar") -> listOf(
            ActorInfo("M. McConaughey", "Cooper", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=200"),
            ActorInfo("Anne Hathaway", "Brand", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200"),
            ActorInfo("J. Chastain", "Murph", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200")
        )
        else -> {
            if (genre.contains("Terror", true) || genre.contains("Suspenso", true)) {
                listOf(
                    ActorInfo("Jenna Ortega", "Tara", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200"),
                    ActorInfo("B. Skarsgård", "Pennywise", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200"),
                    ActorInfo("Anya Taylor-Joy", "Thomasin", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200")
                )
            } else if (genre.contains("Acción", true) || genre.contains("Ficción", true) || genre.contains("Sci-Fi", true)) {
                listOf(
                    ActorInfo("Pedro Pascal", "Mandalorian", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=200"),
                    ActorInfo("Ana de Armas", "Paloma", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200"),
                    ActorInfo("Ryan Gosling", "K", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200")
                )
            } else {
                listOf(
                    ActorInfo("Margot Robbie", "Barbie", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200"),
                    ActorInfo("Glen Powell", "Hangman", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200"),
                    ActorInfo("S. Sweeney", "Bea", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200")
                )
            }
        }
    }
}

@Composable
fun CatalogItemDetailsDialog(
    item: CatalogItem,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cast = remember(item) { getMockCast(item.title, item.genre) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color.White.copy(alpha = 0.08f)))),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 1. Immersive Hero Backdrop
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        AsyncImage(
                            model = item.posterUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.35f
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF0F1524)
                                        )
                                    )
                                )
                        )
                        
                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Gold star rating badge
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.60f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.rating,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                // 2. Main Metadata Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .width(82.dp)
                                .height(120.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            AsyncImage(
                                model = item.posterUrl,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.title.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                lineHeight = 20.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.year,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                Text(
                                    text = item.genre,
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .background(Color(0xFF00E5FF).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "CALIDAD IPTV",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "4K ULTRA HD",
                                    color = Color(0xFF00FF87),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.sp,
                                    modifier = Modifier
                                        .border(0.5.dp, Color(0xFF00FF87).copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                                Text(
                                    text = "HDR10",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Description Segment
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SINOPSIS / RESUMEN",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = item.description,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // 4. Cast list with Actors Faces and names
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "REPARTO Y ELENCO PRINCIPAL",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(cast) { actor ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(72.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), CircleShape)
                                    ) {
                                        AsyncImage(
                                            model = actor.photoUrl,
                                            contentDescription = actor.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = actor.name,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Text(
                                        text = actor.role,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 8.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Actions row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onDismiss()
                                val details = getCinematicDetails(item)
                                val movieChannel = Channel(
                                    id = "trailer_${item.id}",
                                    name = item.title,
                                    streamUrl = item.streamUrl ?: details.trailerUrl,
                                    logoUrl = item.posterUrl,
                                    category = "Cine Premium",
                                    description = item.description,
                                    number = 999
                                )
                                viewModel.selectChannel(movieChannel)
                                viewModel.isFullscreenPlayerActive = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REPRODUCIR", fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Añadida a tu Lista de Canales Favoritos", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GUARDAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogVerticalGrid(
    items: List<CatalogItem>,
    layoutType: String = "Vertical",
    favoriteCatalogItems: Set<String> = emptySet(),
    seenProgress: Map<String, Float> = emptyMap(),
    onItemFocus: (CatalogItem) -> Unit = {},
    onClick: (CatalogItem) -> Unit
) {
    val chunked = remember(items) { items.chunked(3) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        CatalogItemHomeCard(
                            item = item,
                            layoutType = layoutType,
                            isFavorite = item.id in favoriteCatalogItems,
                            progress = seenProgress[item.id] ?: 0f,
                            onFocus = { onItemFocus(item) },
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onClick(item) }
                        )
                    }
                }
                // Align column layouts cleanly if row size is under 3
                val remainder = 3 - rowItems.size
                if (remainder > 0) {
                    repeat(remainder) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogItemNumberedCard(
    item: CatalogItem,
    rank: Int,
    isFavorite: Boolean = false,
    progress: Float = 0f,
    onFocus: () -> Unit = {},
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .onFocusChanged { state ->
                if (state.isFocused || state.hasFocus) {
                    onFocus()
                }
            }
            .width(150.dp)
            .height(175.dp)
            .clickable { onClick() }
            .tvFocusEffect(
                shape = RoundedCornerShape(8.dp),
                focusedBorderColor = Color(0xFF00E5FF),
                scaleAmount = 1.12f
            )
    ) {
        // 1. Giant Number Rank (Drawn FIRST on the bottom layer to stay behind the poster card)
        val rankStr = "$rank"
        Text(
            text = rankStr,
            style = TextStyle(
                fontSize = 120.sp,
                fontWeight = FontWeight.W900,
                color = Color.White.copy(alpha = 0.82f), // Semi-transparent silvery white/gray look
                letterSpacing = (-9).sp,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.95f),
                    offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                    blurRadius = 8f
                )
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 2.dp, y = 14.dp) // Aligns beautifully with the bottom edge
        )

        // 2. Poster Card (Drawn SECOND on the top layer to overlap the giant rank number on the right)
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
                .width(110.dp)
                .height(155.dp)
                .align(Alignment.BottomEnd)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Movie/Show Poster
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gold Rating Overlay Tag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.rating,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favorite Overlay Tag
                if (isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorito",
                            tint = Color.Red,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }

                // Dynamic Year overlay gradient background & progress
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                )
                            )
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.year,
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Seen progress bar
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(Color(0xFF00E5FF))
                            )
                        }
                    }
                }
            }
        }
    }
}

// Cinematic Details helper structures for rich immersive home page banners matching user screenshot
data class CinematicInfo(
    val logoText: String,
    val dateAndMetadata: String,
    val providerBadge: String,
    val budget: String,
    val subtitleLines: List<String>,
    val trailerUrl: String,
    val backdropUrl: String
)

fun getCinematicDetails(movie: CatalogItem): CinematicInfo {
    return when (movie.id) {
        "m_michael" -> CinematicInfo(
            logoText = "Michael",
            dateAndMetadata = "22 Apr 2026 • Music / Drama • 2h 6m",
            providerBadge = "tv+",
            budget = "Presupuesto: $250M",
            subtitleLines = listOf(
                "Michael: No le temas a la oscuridad...",
                "Su voz sacudió al mundo, su corazón rompió moldes.",
                "Desde el descubrimiento de su extraordinario talento temprano...",
                "[Suena Billie Jean en versión sinfónica de fondo]",
                "Hasta convertirse en una visionaria estrella del pop global.",
                "Michael: El viaje definitivo del Rey del Pop de regreso."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=1200"
        )
        "m_theboys" -> CinematicInfo(
            logoText = "THE BOYS",
            dateAndMetadata = "13 Jun 2024 • Acción / Ciencia Ficción • 4 Temp",
            providerBadge = "prime video",
            budget = "Presupuesto: $180M",
            subtitleLines = listOf(
                "Homelander: ¡Mírenme! ¡Yo soy vuestro único salvador!",
                "Un grupo fuera de la ley dispuesto a desenmascarar el corporativismo.",
                "Butcher: Es hora de nivelar esta balanza ruidosa...",
                "[Efecto de rayos oculares destrozando un hangar de caza]",
                "The Boys: La última y más explosiva temporada de todas."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200"
        )
        "m1" -> CinematicInfo(
            logoText = "DUNE II",
            dateAndMetadata = "29 Feb 2024 • Ciencia Ficción / Aventura • 2h 46m",
            providerBadge = "MAX",
            budget = "Presupuesto: $190M",
            subtitleLines = listOf(
                "Paul Atreides: Seguiré el camino que conduce a mi pueblo.",
                "Chani: Aquel que controla la especia gobierna el espacio.",
                "[Gemidos de gigantescos gusanos de arena de Arrakis]",
                "Únete a los Fremen en la última cruzada santa por Arrakis."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1200"
        )
        "m2" -> CinematicInfo(
            logoText = "Oppenheimer",
            dateAndMetadata = "20 Jul 2023 • Historia / Drama • 3h 0m",
            providerBadge = "UNIVERSAL",
            budget = "Presupuesto: $100M",
            subtitleLines = listOf(
                "Oppenheimer: No sabemos si causaremos la ignición atmosférica.",
                "Un invento secreto que cambiará irrevocablemente la historia del hombre.",
                "[Tictac acelerado de reloj despertador con suspenso bélico]",
                "Oppenheimer: Ahora me he convertido en la muerte, destructora de mundos."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?q=80&w=1200"
        )
        "m3" -> CinematicInfo(
            logoText = "SPIDER-MAN",
            dateAndMetadata = "02 Jun 2023 • Animación / Acción • 2h 20m",
            providerBadge = "CINE PREMIUM",
            budget = "Presupuesto: $150M",
            subtitleLines = listOf(
                "Miles Morales: Todos me dicen cómo debe ser mi historia.",
                "Un viaje a través del indómito Multiverso arácnido.",
                "Gwen Stacy: ¿Quieres salir de aquí?",
                "Spider-Man: Across the Spider-Verse ya disponible."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1618005198143-e52834644026?q=80&w=1200"
        )
        else -> CinematicInfo(
            logoText = movie.title,
            dateAndMetadata = "${movie.year} • ${movie.genre} • 2h 15m",
            providerBadge = "CINE PREMIUM",
            budget = "Presupuesto: $120M",
            subtitleLines = listOf(
                "Entra en un festín cinematográfico inolvidable en Lumina...",
                "Ya disponible con soporte multi-idioma oficial en HD.",
                "Presiona reproducir para iniciar la inmersión completa."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            backdropUrl = movie.posterUrl
        )
    }
}
