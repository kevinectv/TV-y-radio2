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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
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
import com.example.data.util.ApiConfig
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

// --- Skeleton Loading Effect Extension ---
@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    this
        .onGloballyPositioned { size = it.size }
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E1E1E),
                    Color(0xFF333333),
                    Color(0xFF1E1E1E)
                ),
                start = androidx.compose.ui.geometry.Offset(startOffsetX, 0f),
                end = androidx.compose.ui.geometry.Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            ),
            shape = RoundedCornerShape(4.dp)
        )
}

@Composable
fun HomeSkeleton(isWideLayout: Boolean, bannerHeight: androidx.compose.ui.unit.Dp) {
    Column(modifier = Modifier.fillMaxSize()) {
        HeroSkeleton(isWideLayout, bannerHeight)
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            repeat(3) {
                CatalogRowSkeleton(isWideLayout)
            }
        }
    }
}

@Composable
fun HeroSkeleton(isWideLayout: Boolean, bannerHeight: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isWideLayout) bannerHeight else 440.dp.responsive())
            .padding(
                start = if (isWideLayout) 48.dp else 20.dp.responsive(),
                end = if (isWideLayout) 48.dp else 20.dp.responsive(),
                top = if (isWideLayout) 24.dp else 12.dp.responsive(),
                bottom = if (isWideLayout) 24.dp else 12.dp.responsive()
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp.responsive())) {
            Box(modifier = Modifier.width(if (isWideLayout) 240.dp else 140.dp.responsive()).height(if (isWideLayout) 60.dp else 40.dp.responsive()).shimmerEffect())
            Box(modifier = Modifier.width(if (isWideLayout) 300.dp else 200.dp.responsive()).height(14.dp.responsive()).shimmerEffect())
            Box(modifier = Modifier.fillMaxWidth(if (isWideLayout) 0.5f else 0.8f).height(14.dp.responsive()).shimmerEffect())
            Box(modifier = Modifier.fillMaxWidth(if (isWideLayout) 0.4f else 0.6f).height(14.dp.responsive()).shimmerEffect())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp.responsive())) {
                Box(modifier = Modifier.width(60.dp.responsive()).height(22.dp.responsive()).shimmerEffect())
                Box(modifier = Modifier.width(60.dp.responsive()).height(22.dp.responsive()).shimmerEffect())
                Box(modifier = Modifier.width(60.dp.responsive()).height(22.dp.responsive()).shimmerEffect())
            }
        }
    }
}

@Composable
fun CatalogRowSkeleton(isWideLayout: Boolean) {
    Column {
        Box(
            modifier = Modifier
                .padding(start = 16.dp.responsive(), top = 22.dp.responsive(), bottom = 6.dp.responsive())
                .width(150.dp.responsive())
                .height(16.dp.responsive())
                .shimmerEffect()
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp.responsive()),
            horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
            userScrollEnabled = false
        ) {
            items(6) {
                PosterSkeleton()
            }
        }
    }
}

@Composable
fun PosterSkeleton() {
    Box(
        modifier = Modifier
            .width(130.dp.responsive())
            .height(180.dp.responsive())
            .shimmerEffect()
    )
}

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
        viewModel.fetchTrending()
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
    val trendingMedia = viewModel.trendingMedia
    val selectedDetailsItem by viewModel.selectedDetailsItem.collectAsState()
    
    // Showcase/Banner movies (Curated highlights from trending or catalogs)
    val featuredMovies = remember(catalogs, viewModel.trendingMedia) {
        if (viewModel.trendingMedia.isNotEmpty()) {
            viewModel.trendingMedia.take(12)
        } else {
            catalogs.filter { it.isVisible && it.showInHome }.flatMap { it.items }.filter { it.posterUrl.isNotEmpty() && !it.posterUrl.contains("unsplash.com") && !it.posterUrl.contains("images.unsplash") }.distinctBy { it.id }.shuffled().take(12)
        }
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

    val currentMovie = activeHeroMovie ?: featuredMovies.firstOrNull()

    // Logo state
    var activeHeroLogoUrl by remember { mutableStateOf<String?>(null) }

    // Loaded dynamic properties (from TMDB real-time query) for the current movie
    var activeHeroLoadedDetails by remember(currentMovie) { mutableStateOf<LoadedTmdbDetails?>(null) }
    
    LaunchedEffect(currentMovie) {
        activeHeroLogoUrl = null
        activeHeroLoadedDetails = null

        if (currentMovie == null) return@LaunchedEffect
        
        // Use Lumina Backend for details instead of direct TMDB
        val enriched = viewModel.getDetailsForMedia(currentMovie.id, if (currentMovie.isTvShow) "tv" else "movie")
        
        if (enriched != null) {
            activeHeroLogoUrl = enriched.getFullLogoUrl() ?: enriched.logoUrl
            activeHeroLoadedDetails = LoadedTmdbDetails(
                description = enriched.overview ?: enriched.description,
                rating = if ((enriched.vote_average ?: 0.0) > 0.0) String.format("%.1f", enriched.vote_average) else enriched.rating,
                year = enriched.release_date?.take(4) ?: enriched.year,
                logoUrl = enriched.getFullLogoUrl() ?: enriched.logoUrl,
                backdropUrl = enriched.getFullBackdropUrl() ?: enriched.backdropUrl ?: enriched.posterUrl,
                duration = if ((enriched.runtime ?: 0) > 0) "${enriched.runtime} min" else enriched.duration,
                genre = enriched.genres?.joinToString(", ") { it.name } ?: enriched.genre
            )
        } else {
            // Minimal fallback from current item
            activeHeroLogoUrl = currentMovie.getFullLogoUrl() ?: currentMovie.logoUrl
            activeHeroLoadedDetails = LoadedTmdbDetails(
                description = currentMovie.overview ?: currentMovie.description,
                rating = if ((currentMovie.vote_average ?: 0.0) > 0.0) String.format("%.1f", currentMovie.vote_average) else currentMovie.rating,
                year = currentMovie.release_date?.take(4) ?: currentMovie.year,
                logoUrl = currentMovie.getFullLogoUrl() ?: currentMovie.logoUrl,
                backdropUrl = currentMovie.getFullBackdropUrl() ?: currentMovie.backdropUrl ?: currentMovie.posterUrl,
                duration = if ((currentMovie.runtime ?: 0) > 0) "${currentMovie.runtime} min" else currentMovie.duration,
                genre = currentMovie.genres?.joinToString(", ") { it.name } ?: currentMovie.genre
            )
        }
    }

    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    // Adjust height for layout: TV uses cinematic banner (350.dp), Mobile uses vertical spotlight inside list (0.dp fixed header)
    val bannerHeight = if (isWideLayout) 350.dp else 0.dp

    // Control de carga (Skeleton)
    val isLoadingData = catalogs.isEmpty() || currentMovie == null

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF030406))) {
        Crossfade(
            targetState = isLoadingData,
            animationSpec = tween(700),
            label = "home_skeleton_fade",
            modifier = Modifier.fillMaxSize()
        ) { isLoading ->
            if (isLoading) {
                HomeSkeleton(isWideLayout, bannerHeight)
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // --- 1. NETFLIX-STYLE FULL-SCREEN BACKDROP COVERING THE BACKGROUND (ONLY ON TV / WIDE) ---
                    if (isWideLayout) {
                        Crossfade(
                            targetState = currentMovie,
                            animationSpec = tween(750),
                            label = "home_full_backdrop",
                            modifier = Modifier.fillMaxSize()
                        ) { movie ->
                            movie?.let { currentSafeMovie ->
                                val backdropUrlToUse = activeHeroLoadedDetails?.backdropUrl?.let { 
                                    if (it.startsWith("/")) "https://image.tmdb.org/t/p/original$it" else it 
                                } ?: currentSafeMovie.getFullBackdropUrl()

                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = backdropUrlToUse,
                                        contentDescription = currentSafeMovie.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Cinematic horizontal dark gradient to protect left-aligned text of Hero Banner
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.95f),
                                                        Color.Black.copy(alpha = 0.82f),
                                                        Color.Black.copy(alpha = 0.35f),
                                                        Color.Transparent
                                                    ),
                                                    endX = 1200f
                                                )
                                            )
                                    )

                                    // Cinematic vertical dark gradient to smoothly fade to pure black at bottom
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.30f),
                                                        Color.Black.copy(alpha = 0.55f),
                                                        Color(0xFF030406)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // --- 2. MAIN STRUCTURAL LAYOUT ---
                    Column(modifier = Modifier.fillMaxSize()) {
                        // A) Fixed Hero Banner (ONLY FOR TV / WIDE LAYOUT)
                        if (isWideLayout) {
                            currentMovie?.let { currentSafeMovie ->
                                HomeHeroBanner(
                                    currentMovie = currentSafeMovie,
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
                            }
                        }

                        // B) Scrollable Content Rows
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            // EN TELÉFONO: Carrusel Destacado Vertical estilo móvil adentro de la lista scrollable
                            if (!isWideLayout) {
                                item {
                                    currentMovie?.let { currentSafeMovie ->
                                        HomeHeroBannerMobile(
                                            currentMovie = currentSafeMovie,
                                            activeHeroLoadedDetails = activeHeroLoadedDetails,
                                            featuredMovies = featuredMovies,
                                            favoriteCatalogItems = favoriteCatalogItems,
                                            bannerHeight = 460.dp.responsive(),
                                            viewModel = viewModel,
                                            scrollState = listState,
                                            onTrailerClick = { movie ->
                                                activeTrailerItem = movie
                                            },
                                            onDetailsClick = { movie ->
                                                viewModel.selectedDetailsItem.value = movie
                                            }
                                        )
                                    }
                                }
                            }

                            val homeCatalogs = catalogs.filter { it.isVisible && it.showInHome }

                            if (trendingMedia.isNotEmpty()) {
                                item {
                                    DrawCatalogRow(
                                        catalog = Catalog(
                                            id = "trending_global",
                                            name = "🔥 Tendencias",
                                            sourceType = "Backend",
                                            items = trendingMedia,
                                            isVisible = true,
                                            showInHome = true,
                                            layoutType = "Horizontal Poster Row"
                                        ),
                                        favoriteCatalogItems = favoriteCatalogItems,
                                        seenProgress = seenProgress,
                                        onFocus = { activeHeroMovie = it },
                                        onClick = { clickedItem ->
                                            activeHeroMovie = clickedItem
                                            viewModel.selectedDetailsItem.value = clickedItem
                                        }
                                    )
                                }
                            }

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

                                    // Inject Continue Watching under the first dynamic row
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
            }
        }
    }

    val trailerToShow = activeTrailerItem ?: viewModel.activeTrailerItem
    if (trailerToShow != null) {
        TrailerYoutubePlayerDialog(
            item = trailerToShow,
            viewModel = viewModel,
            onDismiss = {
                activeTrailerItem = null
                viewModel.activeTrailerItem = null
            }
        )
    }
}




@Composable
fun DrawCatalogRow(
    catalog: Catalog,
    favoriteCatalogItems: Set<String>,
    seenProgress: Map<String, Float>,
    onFocus: (CatalogItem) -> Unit,
    onClick: (CatalogItem) -> Unit,
    customTitle: String? = null,
    customLayout: String? = null,
    customIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val titleToDraw = customTitle ?: catalog.name
    val layoutToDraw = customLayout ?: catalog.layoutType
    val iconToDraw = customIcon ?: when (catalog.sourceType) {
        "TMDB" -> Icons.Filled.Movie
        "Trakt" -> Icons.Filled.Tv
        "MDBList" -> Icons.Filled.FilterAlt
        else -> Icons.Filled.VideoLibrary
    }
    HomeSectionRowHeader(
        title = titleToDraw.uppercase(),
        icon = iconToDraw,
        color = Color(0xFF00E5FF)
    )

    val isSupportedRowType = layoutToDraw in listOf(
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
            horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
            contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 6.dp.responsive())
        ) {
            items(catalog.items.take(catalog.numItems)) { item ->
                CatalogItemHomeCard(
                    item = item,
                    layoutType = layoutToDraw,
                    isFavorite = item.id in favoriteCatalogItems,
                    progress = seenProgress[item.id] ?: 0f,
                    onFocus = {
                        onFocus(item)
                    },
                    onClick = {
                        onClick(item)
                    }
                )
            }
        }
    } else if (layoutToDraw == "Vertical") {
        CatalogVerticalGrid(
            items = catalog.items.take(catalog.numItems),
            layoutType = "Vertical",
            favoriteCatalogItems = favoriteCatalogItems,
            seenProgress = seenProgress,
            onItemFocus = { item ->
                onFocus(item)
            },
            onClick = { item ->
                onClick(item)
            }
        )
    } else if (layoutToDraw == "Top Numerado" || layoutToDraw.contains("top", ignoreCase = true) || titleToDraw.contains("top", ignoreCase = true) || titleToDraw.contains("Mejor Valorad", ignoreCase = true) || titleToDraw.contains("Top 250", ignoreCase = true)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp.responsive()),
            contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 6.dp.responsive())
        ) {
            itemsIndexed(catalog.items.take(catalog.numItems)) { index, item ->
                CatalogItemNumberedCard(
                    item = item,
                    rank = index + 1,
                    isFavorite = item.id in favoriteCatalogItems,
                    progress = seenProgress[item.id] ?: 0f,
                    onFocus = {
                        onFocus(item)
                    },
                    onClick = {
                        onClick(item)
                    }
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
            contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 6.dp.responsive())
        ) {
            items(catalog.items.take(catalog.numItems)) { item ->
                CatalogItemHomeCard(
                    item = item,
                    layoutType = "Horizontal Poster Row",
                    isFavorite = item.id in favoriteCatalogItems,
                    progress = seenProgress[item.id] ?: 0f,
                    onFocus = {
                        onFocus(item)
                    },
                    onClick = {
                        onClick(item)
                    }
                )
            }
        }
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
            .padding(
                start = 16.dp.responsive(),
                end = 16.dp.responsive(),
                top = 22.dp.responsive(),
                bottom = 6.dp.responsive()
            ),
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

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        modifier = Modifier
            .width(180.dp.responsive())
            .height(115.dp.responsive())
            .tvFocusEffect(
                shape = RoundedCornerShape(6.dp),
                unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                interactionSource = interactionSource
            )
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onPlayClick
            )
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
                        fontSize = 9.sp.responsive(),
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
                        fontSize = 12.sp.responsive(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = channel.category,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp.responsive()
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

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        modifier = Modifier
            .width(180.dp.responsive())
            .height(115.dp.responsive())
            .tvFocusEffect(
                shape = RoundedCornerShape(6.dp),
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                interactionSource = interactionSource
            )
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onPlayClick
            )
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
                        fontSize = 9.sp.responsive(),
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
                        fontSize = 12.sp.responsive(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = station.genre,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp.responsive()
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
        "Horizontal Poster Row", "Horizontal" -> 130.dp
        "Vertical Poster Row", "Vertical" -> 150.dp
        "Landscape Row" -> 200.dp
        "Banner Row" -> 240.dp
        "Large Featured Row" -> 190.dp
        "Compact Row" -> 100.dp
        else -> 130.dp
    }.responsive()
    
    val imageHeight = when (layoutType) {
        "Horizontal Poster Row", "Horizontal" -> 180.dp
        "Vertical Poster Row", "Vertical" -> 210.dp
        "Landscape Row" -> 110.dp
        "Banner Row" -> 90.dp
        "Large Featured Row" -> 260.dp
        "Compact Row" -> 140.dp
        else -> 180.dp
    }.responsive()

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        modifier = modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(6.dp))
            .tvFocusEffect(
                shape = RoundedCornerShape(6.dp),
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                interactionSource = interactionSource,
                onFocus = onFocus
            )
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                // Movie/Show Poster
                AsyncImage(
                    model = item.getFullPosterUrl(),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Logo/Title Overlay
                Box(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val logoUrl = item.getFullLogoUrl()
                    if (!logoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 60.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = item.title,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

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
        }
    }
}

data class ActorInfo(val name: String, val role: String, val photoUrl: String)

@Composable
fun CatalogItemFullScreenDetails(
    item: CatalogItem,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit,
    onNavigateToSimilar: (CatalogItem) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val offlineDescription = item.description

    var dynamicDescription by remember(item) { mutableStateOf(offlineDescription.ifEmpty { item.description }) }
    var dynamicRating by remember(item) { mutableStateOf(item.rating) }
    var dynamicYear by remember(item) { mutableStateOf(item.year) }
    var dynamicLogoUrl by remember(item) { mutableStateOf<String?>(item.getFullLogoUrl()) }
    var dynamicBackdrop by remember(item) { mutableStateOf(item.getFullBackdropUrl()) }
    var dynamicCast by remember(item) { mutableStateOf<List<ActorInfo>>(emptyList()) }

    val catalogsState = viewModel.catalogsStateFlow.collectAsState()
    val similarItems = remember(item, catalogsState.value) {
        catalogsState.value.flatMap { it.items }
            .filter { it.id != item.id && (it.genre.split("/").any { g -> item.genre.contains(g.trim(), ignoreCase = true) } || it.isTvShow == item.isTvShow) }
            .distinctBy { it.id }
            .take(8)
    }

    LaunchedEffect(item) {
        // Fetch enriched data from Lumina Backend
        try {
            val enriched = viewModel.getDetailsForMedia(item.id, if (item.isTvShow) "tv" else "movie")
            if (enriched != null) {
                dynamicDescription = enriched.description.ifEmpty { item.description }
                dynamicRating = enriched.rating.ifEmpty { item.rating }
                dynamicYear = enriched.year.ifEmpty { item.year }
                dynamicLogoUrl = enriched.logoUrl ?: item.getFullLogoUrl()
                dynamicBackdrop = enriched.backdropUrl ?: item.getFullBackdropUrl()
                
                try {
                    val backendCast = com.example.data.LuminaCatalogEngine.deserializeCast(enriched.castJson).map { engineActor ->
                        ActorInfo(name = engineActor.name, role = engineActor.role, photoUrl = engineActor.photoUrl)
                    }
                    if (backendCast.isNotEmpty()) {
                        dynamicCast = backendCast
                    }
                } catch (e: Exception) { e.printStackTrace() }
            } else {
                // Fallback to local data
                try {
                    val cachedCast = com.example.data.LuminaCatalogEngine.deserializeCast(item.castJson).map { engineActor ->
                        ActorInfo(name = engineActor.name, role = engineActor.role, photoUrl = engineActor.photoUrl)
                    }
                    if (cachedCast.isNotEmpty()) {
                        dynamicCast = cachedCast
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    androidx.activity.compose.BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030406))
    ) {
        // Full screen blurred backdrop
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = dynamicBackdrop.ifEmpty { item.getFullBackdropUrl() },
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.16f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF030406).copy(alpha = 0.6f),
                                Color(0xFF030406)
                            )
                        )
                    )
            )
        }

        val isWide = context.resources.configuration.screenWidthDp >= 600

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .tvFocusEffect(shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "DETALLES",
                        color = Color.White.copy(alpha = 0.60f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Content section: Poster & Core Info
                val contentHeight = if (isWide) 280.dp else 180.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Poster image shadow-framed
                    Card(
                        modifier = Modifier
                            .width(if (isWide) 140.dp.responsive() else 100.dp.responsive())
                            .height(if (isWide) 210.dp.responsive() else 150.dp.responsive()),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        AsyncImage(
                            model = item.getFullPosterUrl(),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Logo or Title
                        if (!dynamicLogoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = dynamicLogoUrl,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .heightIn(max = 60.dp)
                                    .widthIn(max = 200.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.BottomStart
                            )
                        } else {
                            Text(
                                text = item.title.uppercase(),
                                color = Color.White,
                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isWide) 24.sp else 18.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                        }

                        // Year & Genres Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = dynamicYear,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "•",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = item.genre,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Rating badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFFFD700).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = dynamicRating,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            val score = remember(item) {
                                val hash = item.title.hashCode()
                                val absHash = if (hash < 0) -hash else hash
                                val ratingFloat = item.rating.toFloatOrNull() ?: 7.5f
                                (150.0 + (absHash % 750) + (ratingFloat * 12)).toInt()
                            }
                            if (score > 0) {
                                Text(
                                    text = "Popularidad: $score",
                                    color = Color(0xFF00FF87),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .background(Color(0xFF00FF87).copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Essential actions
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Reproducir
                            Button(
                                onClick = {
                                    val movieChannel = Channel(
                                        id = "catalog_${item.id}",
                                        name = item.title,
                                        streamUrl = item.streamUrl ?: "",
                                        logoUrl = item.getFullPosterUrl(),
                                        category = "Cine Premium",
                                        description = item.description,
                                        number = 999
                                    )
                                    viewModel.selectChannel(movieChannel)
                                    viewModel.isFullscreenPlayerActive = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("REPRODUCIR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }

                            // Trailer
                            OutlinedButton(
                                onClick = {
                                    viewModel.activeTrailerItem = item
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TRÁILER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            // Guardar / Mi Lista
                            val isInMyList = item.id in viewModel.favoriteCatalogItems.collectAsState().value
                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleCatalogItemFavorite(item.id)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isInMyList) Color(0xFF00FF87) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isInMyList) Color(0xFF00FF87).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            ) {
                                Icon(if (isInMyList) Icons.Filled.Check else Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isInMyList) "GUARDADO" else "GUARDAR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            
                            // Compartir
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val shareStr = "¡Mira ${item.title} (${item.year}) en Lumina! Calificación: ${item.rating} estrella."
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, shareStr)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Compartir con"))
                                    } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("COMPARTIR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                }
                    }

                // Synopsis Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SINOPSIS COMPLETA",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = dynamicDescription,
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = if (isWide) 13.sp else 12.sp,
                            lineHeight = if (isWide) 18.sp else 16.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                }

                // Spec Grid section: "Mostrar director. Mostrar productora."
                SpecInformationGrid(
                    director = item.director ?: "No especificado",
                    productora = item.producer ?: "Estudio Independiente",
                    pais = "United States",
                    idioma = item.languages ?: "Español Latino / Inglés",
                    subtitulos = item.subtitles ?: "Español Latino / Inglés",
                    clasificacion = "PG-13 / TV-14",
                    temporadas = if (item.isTvShow) "Series" else "Película",
                    status = "Disponible",
                    duracion = item.duration ?: "2h 15m"
                )

                // Casting list: "Mostrar actores."
                if (dynamicCast.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "REPARTO / ACTORES",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                            contentPadding = PaddingValues(vertical = 4.dp.responsive())
                        ) {
                            items(dynamicCast) { actor ->
                                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                Box(
                                    modifier = Modifier
                                        .width(100.dp.responsive())
                                        .wrapContentHeight()
                                        .tvFocusEffect(shape = RoundedCornerShape(6.dp), unfocusedBorderColor = Color.White.copy(alpha = 0.08f), interactionSource = interactionSource)
                                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(interactionSource = interactionSource, indication = null) {  }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp.responsive()),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp.responsive())
                                    ) {
                                        AsyncImage(
                                            model = actor.photoUrl,
                                            contentDescription = actor.name,
                                            modifier = Modifier
                                                .size(54.dp.responsive())
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Text(
                                            text = actor.name,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Text(
                                            text = actor.role,
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Additional Images gallery
                val extraImages = remember(item) {
                    item.extraImagesJson?.split(";;")?.filter { it.isNotEmpty() } ?: emptyList()
                }
                if (extraImages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "IMÁGENES ADICIONALES Y CAPTURAS",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                            contentPadding = PaddingValues(vertical = 4.dp.responsive())
                        ) {
                            items(extraImages) { imageUrl ->
                                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                Box(
                                    modifier = Modifier
                                        .width(180.dp.responsive())
                                        .height(101.dp.responsive())
                                        .tvFocusEffect(shape = RoundedCornerShape(6.dp), unfocusedBorderColor = Color.White.copy(alpha = 0.15f), interactionSource = interactionSource)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(interactionSource = interactionSource, indication = null) { }
                                ) {
                                    AsyncImage(
                                        model = if (imageUrl.startsWith("/")) "https://image.tmdb.org/t/p/w780$imageUrl" else imageUrl,
                                        contentDescription = "Captura",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // Trailers block: "Mostrar trailers."
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TRAILERS OFICIALES Y VIDEOS",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp.responsive())
                            .tvFocusEffect(shape = RoundedCornerShape(6.dp), unfocusedBorderColor = Color.White.copy(alpha = 0.1f), interactionSource = interactionSource)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(interactionSource = interactionSource, indication = null) {
                                viewModel.activeTrailerItem = item
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = dynamicBackdrop.ifEmpty { item.getFullBackdropUrl() },
                                contentDescription = "Trailer Backdrop",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.40f
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Red.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
                                Text(
                                    text = "REPRODUCIR TRÁILER",
                                    color = Color.White,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Similar content scroller: "Mostrar contenido similar."
                if (similarItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "CONTENIDO SIMILAR RECOMENDADO",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                            contentPadding = PaddingValues(vertical = 4.dp.responsive())
                        ) {
                            items(similarItems) { similar ->
                                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                Box(
                                    modifier = Modifier
                                        .width(90.dp.responsive())
                                        .height(135.dp.responsive())
                                        .tvFocusEffect(shape = RoundedCornerShape(6.dp), unfocusedBorderColor = Color.White.copy(alpha = 0.15f), interactionSource = interactionSource)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(interactionSource = interactionSource, indication = null) {
                                            onNavigateToSimilar(similar)
                                        }
                                ) {
                                    AsyncImage(
                                        model = similar.getFullPosterUrl(),
                                        contentDescription = similar.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
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

@Composable
fun CatalogItemDetailsDialog(
    item: CatalogItem,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit,
    onTrailerClick: (CatalogItem) -> Unit = {}
) {
    CatalogItemFullScreenDetails(
        item = item,
        viewModel = viewModel,
        onDismiss = onDismiss,
        onNavigateToSimilar = { similar ->
            onTrailerClick(similar)
        }
    )
    return
}

@Composable
fun CatalogItemDetailsDialog_Original(
    item: CatalogItem,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit,
    onTrailerClick: (CatalogItem) -> Unit = {}
) {
    val context = LocalContext.current
    
    val offlineDescription = item.description
    var dynamicDescription by remember(item) { mutableStateOf(offlineDescription.ifEmpty { item.description }) }
    var dynamicRating by remember(item) { mutableStateOf(item.rating) }
    var dynamicYear by remember(item) { mutableStateOf(item.release_date?.take(4) ?: item.year) }
    var dynamicLogoUrl by remember(item) { mutableStateOf<String?>(null) }
    var dynamicBackdrop by remember(item) { mutableStateOf("") }
    var dynamicCast by remember(item) { mutableStateOf<List<ActorInfo>>(emptyList()) }
    var dynamicGenres by remember(item) { mutableStateOf(item.genres?.joinToString(", ") { it.name } ?: item.genre) }
    var dynamicRuntime by remember(item) { 
        val text = if ((item.runtime ?: 0) > 0) {
            val h = item.runtime!! / 60
            val m = item.runtime!! % 60
            if (h > 0) "${h}h ${m}m" else "${m}m"
        } else item.duration ?: ""
        mutableStateOf(text)
    }
    var dynamicDirector by remember(item) { 
        mutableStateOf(item.credits?.crew?.find { it.job == "Director" }?.name ?: item.director ?: "")
    }

    LaunchedEffect(item) {
        val apiKey = ApiConfig.TMDB_API_KEY
        
        // 1. Try reading straight from backend mapped fields
        val backendCast = item.credits?.cast?.map { castMember ->
            ActorInfo(
                name = castMember.name, 
                role = castMember.character ?: "", 
                photoUrl = if (!castMember.profile_path.isNullOrEmpty()) "https://image.tmdb.org/t/p/w185${castMember.profile_path}" else ""
            )
        } ?: emptyList()

        if (backendCast.isNotEmpty() || !item.overview.isNullOrEmpty() || (item.vote_average ?: 0.0) > 0.0) {
            if (!item.backdrop_path.isNullOrEmpty()) dynamicBackdrop = item.getFullBackdropUrl()
            if (!item.logo_path.isNullOrEmpty()) dynamicLogoUrl = item.getFullLogoUrl()
            if (backendCast.isNotEmpty()) dynamicCast = backendCast
            if (!item.overview.isNullOrEmpty()) dynamicDescription = item.overview ?: item.description
            if ((item.vote_average ?: 0.0) > 0.0) dynamicRating = String.format("%.1f", item.vote_average)
            if (!item.release_date.isNullOrEmpty()) dynamicYear = item.release_date!!.take(4)
            if (!item.genres.isNullOrEmpty()) dynamicGenres = item.genres!!.joinToString(", ") { it.name }
            if ((item.runtime ?: 0) > 0) {
                val h = item.runtime!! / 60
                val m = item.runtime!! % 60
                dynamicRuntime = if (h > 0) "${h}h ${m}m" else "${m}m"
            }
            val directorFound = item.credits?.crew?.find { it.job == "Director" }?.name
            if (directorFound != null) dynamicDirector = directorFound
            
            return@LaunchedEffect
        }

        // 2. Fallback to Catalog Engine lookup if fields are empty
        val engine = viewModel.catalogRepository?.engine
        if (engine != null && apiKey.isNotEmpty()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val enriched = engine.enrichCatalogItem(item, apiKey)
                    
                    if (!enriched.backdropUrl.isNullOrEmpty()) {
                        dynamicBackdrop = if (enriched.backdropUrl!!.startsWith("/")) "https://image.tmdb.org/t/p/original${enriched.backdropUrl}" else enriched.backdropUrl
                    }
                    if (!enriched.getFullLogoUrl().isNullOrEmpty()) {
                        dynamicLogoUrl = enriched.getFullLogoUrl()
                    }
                    val parsedCast = com.example.data.LuminaCatalogEngine.deserializeCast(enriched.castJson).map { engineActor ->
                        ActorInfo(name = engineActor.name, role = engineActor.role, photoUrl = engineActor.photoUrl)
                    }.ifEmpty {
                        enriched.credits?.cast?.map { castMember ->
                            ActorInfo(
                                name = castMember.name,
                                role = castMember.character ?: "",
                                photoUrl = if (!castMember.profile_path.isNullOrEmpty()) "https://image.tmdb.org/t/p/w185${castMember.profile_path}" else ""
                            )
                        } ?: emptyList()
                    }
                    if (parsedCast.isNotEmpty()) {
                        dynamicCast = parsedCast
                    }
                    if (!enriched.description.isNullOrEmpty()) {
                        dynamicDescription = enriched.description
                    }
                    if (!enriched.rating.isNullOrEmpty()) {
                        dynamicRating = enriched.rating
                    }
                    if (!enriched.year.isNullOrEmpty()) {
                        dynamicYear = enriched.year
                    }

                    // Save enriched items into catalogs list asynchronously
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        viewModel.catalogRepository?.let { repo ->
                            val currentList = repo.catalogs.value.map { cat ->
                                val hasItem = cat.items.any { it.id == item.id }
                                if (hasItem) {
                                    val newItems = cat.items.map { if (it.id == item.id) enriched else it }
                                    cat.copy(items = newItems)
                                } else cat
                            }
                            repo.saveCatalogsList(currentList)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    

    // Dynamic and high-fidelity generation of cinematic data specs
    val isSeriesOrAnime = remember(item) {
        val titleLower = item.title.lowercase()
        val genreLower = item.genre.lowercase()
        genreLower.contains("anime") || genreLower.contains("animación") || titleLower.contains("serie") || titleLower.contains("temporada") || item.isTvShow
    }

    val director = dynamicDirector.ifEmpty { "No disponible" }

    val productora = remember(item) {
        item.producer ?: "Paramount Pictures / Universal"
    }

    val pais = remember(item) {
        if (item.genre.contains("Anime", true) || item.genre.contains("Manga", true)) "Japón" else "Estados Unidos"
    }

    val idioma = remember(item) {
        item.languages ?: "Español Latino / Inglés"
    }

    val clasificacion = remember(item) {
        if (item.genre.contains("Terror", true) || item.genre.contains("Horror", true) || item.genre.contains("Drama", true)) "R (Público Adulto)" else "PG-13 (Público General)"
    }

    val temporadasInfo = remember(item) {
        if (isSeriesOrAnime) "Serie / TV" else "Película Completa"
    }

    val emisionStatus = remember(item) {
        if (isSeriesOrAnime) "En Emisión Semanal" else "Emitido"
    }

    val duracionText = dynamicRuntime.ifEmpty { "N/D" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() } // Tap outside content card dismisses
        ) {
            val isWideLayout = maxWidth >= 600.dp
            val cardWidthPercent = if (isWideLayout) 0.85f else 0.95f
            val cardPadding = if (isWideLayout) 24.dp else 12.dp

            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(cardWidthPercent)
                    .fillMaxHeight(0.9f)
                    .clickable(enabled = false) {} // Prevent click-through of content
                    .padding(vertical = cardPadding),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0E17)),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color.White.copy(alpha = 0.05f))))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // 1. Hero Backdrop Cover Image with Close button
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            AsyncImage(
                                model = dynamicBackdrop.ifEmpty { item.getFullBackdropUrl() },
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.45f
                            )
                            
                            // Cinematic dark wash over backdrop
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFF0A0E17).copy(alpha = 0.6f),
                                                Color(0xFF0A0E17)
                                            )
                                        )
                                    )
                            )

                            // Close Button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Left Accent Title Overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                if (!dynamicLogoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = dynamicLogoUrl,
                                        contentDescription = item.title,
                                        modifier = Modifier
                                            .padding(bottom = 6.dp)
                                            .heightIn(max = 65.dp)
                                            .widthIn(max = 160.dp),
                                        contentScale = ContentScale.Fit,
                                        alignment = Alignment.BottomStart,
                                        onError = {
                                            dynamicLogoUrl = null // Fallback to text on error
                                        }
                                    )
                                } else {
                                    Text(
                                        text = item.title.uppercase(),
                                        color = Color.White,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp,
                                            letterSpacing = (-0.5).sp,
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.9f),
                                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                                blurRadius = 6f
                                            )
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dynamicGenres,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .background(Color(0xFF00E5FF).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = dynamicYear,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2. Responsive Content Pane (Split on Wide, Stacked on Mobile)
                    item {
                        if (isWideLayout) {
                            // SPLIT SCREEN: Left column Poster and Info list / Right Column Synopsis and Cast
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                // Column A: Poster Card with Details Grid
                                Column(
                                    modifier = Modifier.width(180.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                    ) {
                                        AsyncImage(
                                            model = item.posterUrl,
                                            contentDescription = item.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    // Compact TMDB badge list
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "TMDB Score",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Rating",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = dynamicRating,
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // Column B: Main Details Panel and Metadata
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Action buttons grid
                                    DetailsActionsGrid(item, viewModel, onDismiss)

                                    // Synopsis Segment
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "SINOPSIS / RESUMEN",
                                            color = Color(0xFF00E5FF),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = dynamicDescription,
                                            color = Color.White.copy(alpha = 0.82f),
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Quick Spec Grid (Wide Layout representation)
                                    SpecInformationGrid(
                                        director = director,
                                        productora = productora,
                                        pais = pais,
                                        idioma = idioma,
                                        clasificacion = clasificacion,
                                        temporadas = temporadasInfo,
                                        status = emisionStatus,
                                        duracion = duracionText
                                    )
                                }
                            }
                        } else {
                            // PORTRAIT / MOBILE LAYOUT: Stacked components sequentially
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // A Row at the top of stacked layout containing the poster card and compact info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(155.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                    ) {
                                        AsyncImage(
                                            model = item.getFullPosterUrl(),
                                            contentDescription = item.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // TMDB Score Badge
                                        Row(
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "TMDB Score",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 9.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Rating",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = dynamicRating,
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Text(
                                            text = "Director: $director",
                                            color = Color.White.copy(alpha = 0.70f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = "Productora:\n$productora",
                                            color = Color.White.copy(alpha = 0.55f),
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                // Inline action button bar
                                DetailsActionsGrid(item, viewModel, onDismiss)

                                // Synopsis Card
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "SINOPSIS / RESUMEN",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.7.sp
                                    )
                                    Text(
                                        text = dynamicDescription,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.2.sp,
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Quick Spec Grid
                                SpecInformationGrid(
                                    director = director,
                                    productora = productora,
                                    pais = pais,
                                    idioma = idioma,
                                    clasificacion = clasificacion,
                                    temporadas = temporadasInfo,
                                    status = emisionStatus,
                                    duracion = duracionText
                                )
                            }
                        }
                    }

                    // 3. Horizontal scrolling Cast of Actors
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "REPARTO Y ELENCO PRINCIPAL",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp)
                            ) {
                                items(dynamicCast) { actor ->
                                    val cardScale = if (isWideLayout) 1f else 0.85f
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width((72.dp * cardScale).coerceAtLeast(64.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size((52.dp * cardScale).coerceAtLeast(46.dp))
                                                .clip(CircleShape)
                                                .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.40f), CircleShape)
                                        ) {
                                            SubcomposeAsyncImage(
                                                model = actor.photoUrl, loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 1.dp, modifier = Modifier.size(16.dp)) } }, error = { val initials = actor.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(); Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors = listOf(Color(0xFF00E5FF), Color(0xFF00FF87)))), contentAlignment = Alignment.Center) { Text(text = initials, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) } },
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
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DetailsActionsGrid(
    item: CatalogItem,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 🔴 Ver Tráiler (Official Dedicated YouTube Player)
        Button(
            onClick = {
                onDismiss()
                viewModel.activeTrailerItem = item
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000), contentColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Movie, contentDescription = "Ver Tráiler", tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("VER TRÁILER", fontWeight = FontWeight.Black, fontSize = 10.5.sp)
        }

        // ▶ Reproducir
        Button(
            onClick = {
                onDismiss()
                val movieChannel = Channel(
                    id = "trailer_${item.id}",
                    name = item.title,
                    streamUrl = item.streamUrl ?: "",
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
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("REPRODUCIR", fontWeight = FontWeight.Black, fontSize = 10.5.sp)
        }

        // ▶ Continuar Viendo
        Button(
            onClick = {
                onDismiss()
                Toast.makeText(context, "Reanudando reproducción desde última pausa...", Toast.LENGTH_SHORT).show()
                val movieChannel = Channel(
                    id = "trailer_${item.id}",
                    name = item.title,
                    streamUrl = item.streamUrl ?: "",
                    logoUrl = item.posterUrl,
                    category = "Cine Premium",
                    description = item.description,
                    number = 999
                )
                viewModel.selectChannel(movieChannel)
                viewModel.isFullscreenPlayerActive = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87), contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("CONTINUAR VIENDO", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // ➕ Mi Lista
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "${item.title} añadida a Mi Lista", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("MI LISTA", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // ❤ Favoritos
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "${item.title} añidada a Favoritos", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("FAVORITOS", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // ⬇ Descargar
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "Descargando para reproducción offline...", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("DESCARGAR", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // 📤 Compartir
        OutlinedButton(
            onClick = {
                try {
                    val shareStr = "¡Mira ${item.title} (${item.year}) en Lumina! Calificación: ${item.rating} estrella."
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, shareStr)
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                } catch(e: Exception) {
                    Toast.makeText(context, "No se pudo compartir", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("COMPARTIR", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

@Composable
fun SpecInformationGrid(
    director: String,
    productora: String,
    pais: String,
    idioma: String,
    subtitulos: String = "Español Latino / Inglés",
    clasificacion: String,
    temporadas: String,
    status: String,
    duracion: String
) {
    val items = listOf(
        Pair("Director", director),
        Pair("Productora", productora),
        Pair("País de Origen", pais),
        Pair("Audio / Idioma", idioma),
        Pair("Subtítulos", subtitulos),
        Pair("Clasificación", clasificacion),
        Pair("Episodios / Duración", "$temporadas ($duracion)"),
        Pair("Estado de Emisión", status)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "DETALLES COMPLETOS DE LA PRODUCCIÓN",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )

        items.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.40f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
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
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(175.dp)
            .tvFocusEffect(
                shape = RoundedCornerShape(6.dp),
                focusedBorderColor = Color.White,
                interactionSource = interactionSource,
                onFocus = onFocus
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
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
            shape = RoundedCornerShape(4.dp),
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
                    model = item.getFullPosterUrl(),
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
@Composable
fun TrailerYoutubePlayerDialog(
    item: CatalogItem,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var fetchedVideoUrl by remember { mutableStateOf(item.trailerUrl ?: item.streamUrl ?: "") }
    var isFetching by remember { mutableStateOf(fetchedVideoUrl.isEmpty()) }
    var isBuffering by remember { mutableStateOf(true) }

    LaunchedEffect(item) {
        if (fetchedVideoUrl.isEmpty()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val prefs = context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE)
                    val apiKey = ApiConfig.TMDB_API_KEY
                    
                    val tmdbId = item.tmdbId ?: item.id.replace(Regex("[^0-9]"), "")
                    val isTv = item.isTvShow
                    val mediaType = if (isTv) "tv" else "movie"
                    
                    val videosUrl = "https://api.themoviedb.org/3/$mediaType/$tmdbId/videos?language=es-MX"
                    val request = okhttp3.Request.Builder()
                    if (apiKey.startsWith("ey")) {
                        request.url(videosUrl).header("Authorization", "Bearer $apiKey")
                    } else {
                        request.url("$videosUrl&api_key=$apiKey")
                    }
                    val client = okhttp3.OkHttpClient()
                    client.newCall(request.build()).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string() ?: ""
                            val results = org.json.JSONObject(body).optJSONArray("results")
                            if (results != null && results.length() > 0) {
                                var ytKey = ""
                                for (i in 0 until results.length()) {
                                    val videoObj = results.getJSONObject(i)
                                    val site = videoObj.optString("site", "")
                                    val type = videoObj.optString("type", "")
                                    val key = videoObj.optString("key", "")
                                    if (site.lowercase() == "youtube" && (type.lowercase() == "trailer" || ytKey.isEmpty())) {
                                        ytKey = key
                                        if (type.lowercase() == "trailer") break
                                    }
                                }
                                if (ytKey.isNotEmpty()) {
                                    fetchedVideoUrl = "https://www.youtube.com/watch?v=$ytKey"
                                    val enriched = item.copy(trailerUrl = fetchedVideoUrl)
                                    viewModel.catalogRepository?.let { repo ->
                                        val currentList = repo.catalogs.value.map { cat ->
                                            if (cat.items.any { it.id == item.id }) {
                                                cat.copy(items = cat.items.map { if (it.id == item.id) enriched else it })
                                            } else cat
                                        }
                                        repo.saveCatalogsList(currentList)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isFetching = false
                }
            }
        } else {
            isFetching = false
        }
    }

    val ytId = remember(fetchedVideoUrl) {
        if (fetchedVideoUrl.isEmpty()) null
        else {
            try {
                var id: String? = null
                val prefixes = listOf(
                    "watch?v=", "youtu.be/", "embed/", "/v/", "/e/",
                    "watch?feature=player_embedded&v="
                )
                for (prefix in prefixes) {
                    val idx = fetchedVideoUrl.indexOf(prefix)
                    if (idx != -1) {
                        val start = idx + prefix.length
                        var end = fetchedVideoUrl.length
                        val breakChars = charArrayOf('#', '&', '?')
                        for (i in start until fetchedVideoUrl.length) {
                            if (fetchedVideoUrl[i] in breakChars) {
                                end = i
                                break
                            }
                        }
                        id = fetchedVideoUrl.substring(start, end)
                        break
                    }
                }
                val finalId = if (id != null && id.length >= 11) id.substring(0, 11) else id
                if (finalId.isNullOrBlank()) null else finalId
            } catch (e: Exception) {
                null
            }
        }
    }

    val useWebView = ytId != null

    LaunchedEffect(isBuffering, isFetching) {
        if (isBuffering && !isFetching) {
            kotlinx.coroutines.delay(4000)
            isBuffering = false
        }
    }


    androidx.activity.compose.BackHandler {
        onDismiss()
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (useWebView) {
                var webViewRef by remember { mutableStateOf<android.webkit.WebView?>(null) }
                DisposableEffect(Unit) {
                    onDispose {
                        webViewRef?.loadUrl("about:blank")
                        webViewRef?.destroy()
                    }
                }
                
                AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewRef = this
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                            isFocusable = true
                            isFocusableInTouchMode = true
                            requestFocus()
                            
                            webChromeClient = object : android.webkit.WebChromeClient() {
                                override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
                                    if (newProgress > 80) isBuffering = false
                                }
                            }
                            webViewClient = object : android.webkit.WebViewClient() {
                                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                    isBuffering = false
                                }
                            }
                            setBackgroundColor(android.graphics.Color.BLACK)
                            
                            val embedHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                    <style>
                                        body { margin: 0; padding: 0; background-color: #000; display: flex; justify-content: center; align-items: center; height: 100vh; overflow: hidden; }
                                        iframe { width: 100vw; height: 100vh; border: none; }
                                    </style>
                                </head>
                                <body>
                                    <iframe id="player" src="https://www.youtube.com/embed/$ytId?autoplay=1&controls=1&fs=0&modestbranding=1&rel=0&playsinline=1&enablejsapi=1" allow="autoplay; fullscreen" allowfullscreen></iframe>
                                </body>
                                </html>
                            """.trimIndent()
                            
                            loadDataWithBaseURL("https://www.youtube.com/", embedHtml, "text/html", "UTF-8", "https://www.youtube.com/")
                        }
                    },
                    update = {},
                    modifier = Modifier.fillMaxSize()
                )
            } else if (fetchedVideoUrl.isNotEmpty() && !isFetching) {
                if (fetchedVideoUrl.contains("youtube.com") || fetchedVideoUrl.contains("youtu.be")) {
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "Tráiler no disponible o ID inválido", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                } else {
                var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            videoViewInstance = this
                        }
                    },
                    update = { videoView ->
                        if (videoView.tag != fetchedVideoUrl) {
                            videoView.tag = fetchedVideoUrl
                            try {
                                isBuffering = true
                                videoView.stopPlayback()
                                videoView.setVideoPath(fetchedVideoUrl)
                                videoView.setOnPreparedListener { mp ->
                                    isBuffering = false
                                    mp.start()
                                }
                                videoView.setOnErrorListener { _, _, _ ->
                                    isBuffering = false
                                    Toast.makeText(context, "Error al reproducir video", Toast.LENGTH_SHORT).show()
                                    true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                isBuffering = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .align(Alignment.Center)
                )
                }
            } else if (!isFetching) {
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Tráiler no disponible", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            }

            // Close button (overlay)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }

            // Buffering Indicator
            if (isBuffering || isFetching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF0000),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isFetching) "Buscando Tráiler..." else "Cargando Tráiler...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun formatSeconds(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
}

private fun getCategoryDisplayInfo(name: String): Pair<String, androidx.compose.ui.graphics.vector.ImageVector> {
    val cleanName = name.trim().lowercase()
    return when {
        cleanName.contains("tendencia") || cleanName.contains("trending") -> Pair("🔥 Tendencias", Icons.Filled.TrendingUp)
        cleanName.contains("popular") -> Pair("🎬 Películas Populares", Icons.Filled.Movie)
        cleanName.contains("cine") || cleanName.contains("película") || cleanName.contains("movie") -> Pair("🎥 Cine Estelar", Icons.Filled.Movie)
        cleanName.contains("serie") || cleanName.contains("show") || cleanName.contains("tv") -> Pair("📺 Series Premium", Icons.Filled.Tv)
        cleanName.contains("anime") -> Pair("🌸 Anime Estelar", Icons.Filled.Movie)
        cleanName.contains("favorito") || cleanName.contains("lista") -> Pair("⭐ Mi Lista", Icons.Filled.Star)
        cleanName.contains("recomenda") -> Pair("✨ Recomendados para ti", Icons.Filled.ThumbUp)
        else -> {
            val capitalized = name.split(" ").map { it.replaceFirstChar { char -> char.uppercase() } }.joinToString(" ")
            Pair("🍿 $capitalized", Icons.Filled.VideoLibrary)
        }
    }
}

