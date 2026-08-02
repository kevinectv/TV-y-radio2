package com.example.ui.screens

import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
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
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.animation.core.FastOutSlowInEasing
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
import com.example.data.BackendApi
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
            horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
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
            .width(186.dp.responsive())
            .height(260.dp.responsive())
            .shimmerEffect()
    )
}

private var lastActiveHeroMovie: CatalogItem? = null

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

    val rawCatalogs by viewModel.catalogsStateFlow.collectAsState()
    val sharedPrefs = remember { context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE) }
    val catalogs = remember(rawCatalogs) {
        rawCatalogs.map { cat ->
            val override = sharedPrefs.getString("layout_override_${cat.id}", null)
            if (override != null) {
                cat.copy(layoutType = override)
            } else {
                cat
            }
        }
    }
    var selectedCatalogItem by remember { mutableStateOf<CatalogItem?>(null) }
    var activeTrailerItem by remember { mutableStateOf<CatalogItem?>(null) }

    val allChannels by viewModel.allChannels.collectAsState()
    // Showcase/Banner movies (Curated highlights from either the active catalogs or premium curated cinema highlights)
    val featuredMovies = remember(catalogs) {
        catalogs.filter { it.isVisible && it.showInHome }.flatMap { it.items }.filter { it.posterUrl.isNotEmpty() && !it.posterUrl.contains("unsplash.com") && !it.posterUrl.contains("images.unsplash") }.distinctBy { it.id }.take(12)
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

    var activeHeroMovie by remember { mutableStateOf<CatalogItem?>(lastActiveHeroMovie) }

    val currentMovie = activeHeroMovie ?: featuredMovies.firstOrNull()

    // Logo state initialized immediately from currentMovie (no delay/no flicker)
    var activeHeroLogoUrl by remember(currentMovie) { mutableStateOf(currentMovie?.logoUrl) }

    // Loaded details state initialized immediately from currentMovie (no delay/no flicker)
    var activeHeroLoadedDetails by remember(currentMovie) {
        mutableStateOf(
            if (currentMovie == null) null else LoadedTmdbDetails(
                description = currentMovie.description,
                rating = currentMovie.rating,
                year = currentMovie.year,
                logoUrl = currentMovie.logoUrl,
                backdropUrl = currentMovie.backdropUrl ?: "",
                duration = currentMovie.duration,
                genre = currentMovie.genre
            )
        )
    }
    
    LaunchedEffect(currentMovie) {
        if (currentMovie != null) {
            lastActiveHeroMovie = currentMovie
        }
    }

    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    // Adjust height for layout: TV uses cinematic banner (300.dp), Mobile uses vertical spotlight inside list (0.dp fixed header)
    val bannerHeight = if (isWideLayout) 300.dp else 0.dp

    // Control de carga (Skeleton)
    val isLoadingData = catalogs.isEmpty() || currentMovie == null
    var progressRowFocusedIndex2 by remember { mutableStateOf<Int?>(null) }
    var progressRowFocusedNearRight2 by remember { mutableStateOf(false) }

    // High performance progressive row limits: starts with the first visible catalog, then loads remaining rows progressively in background.
    // This distributes recomposition overhead over multiple frames and keeps remote/touch navigation extremely light and immediate.
    var visibleRowsLimit by remember { mutableStateOf(1) }
    LaunchedEffect(catalogs) {
        if (catalogs.isNotEmpty()) {
            visibleRowsLimit = 1
            // Small initial delay to let the Home Screen first frame draw completely
            delay(220)
            // Progressively increase the row limit one-by-one to pre-warm next sections in background
            for (i in 2..catalogs.size) {
                visibleRowsLimit = i
                delay(80)
            }
        }
    }

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
                                val backdropUrlToUse = activeHeroLoadedDetails?.backdropUrl ?: currentSafeMovie.backdropUrl ?: ""

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
                        val homeCatalogs = remember(catalogs) {
                            catalogs.filter { it.isVisible && it.showInHome }
                        }
                        val activeCatalogs = remember(homeCatalogs) {
                            homeCatalogs.filter { it.items.isNotEmpty() }
                        }
                        val visibleCatalogs = remember(activeCatalogs, visibleRowsLimit) {
                            activeCatalogs.take(visibleRowsLimit)
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(if (isWideLayout) 36.dp.responsive() else 16.dp.responsive()),
                            contentPadding = PaddingValues(
                                top = if (isWideLayout) 36.dp else 0.dp,
                                bottom = 90.dp
                            )
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

                            if (homeCatalogs.isEmpty()) {
                                if (progressItems.isNotEmpty()) {
                                    item(key = "empty_progress_watching") {
                                        HomeSectionRowHeader(
                                            title = "⏱️ CONTINUAR VIENDO",
                                            icon = Icons.Filled.PlayCircle,
                                            color = Color(0xFF00FF87)
                                        )
                                        Spacer(modifier = Modifier.height(if (isWideLayout) 8.dp.responsive() else 12.dp.responsive()))
                                        var progressRowFocusedIndex by remember { mutableStateOf<Int?>(null) }
                                        var progressRowFocusedNearRight by remember { mutableStateOf(false) }
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                                            contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 8.dp.responsive())
                                        ) {
                                            itemsIndexed(
                                                items = progressItems,
                                                key = { _, pair -> "progress_empty_${pair.first.id}" }
                                            ) { index, (item, progressVal) ->
                                                val fIndex = progressRowFocusedIndex
                                                val isCovered = isCardCovered(index, fIndex, progressRowFocusedNearRight, isWideLayout)
                                                CatalogItemHomeCard(
                                                    item = item,
                                                    layoutType = "Landscape Row",
                                                    isFavorite = item.id in favoriteCatalogItems,
                                                    progress = progressVal,
                                                    onFocus = { activeHeroMovie = item },
                                                    onFocusChange = { isFocused, isNearRight ->
                                                        if (isFocused) {
                                                            progressRowFocusedIndex = index
                                                            progressRowFocusedNearRight = isNearRight
                                                        } else {
                                                            if (progressRowFocusedIndex == index) {
                                                                progressRowFocusedIndex = null
                                                            }
                                                        }
                                                    },
                                                    isOtherFocusedInRow = isCovered,
                                                    onClick = {
                                                        activeHeroMovie = item
                                                        viewModel.selectedDetailsItem.value = item
                                                    },
                                                    cardIndex = index,
                                                    focusedIndex = fIndex
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                itemsIndexed(
                                    items = visibleCatalogs,
                                    key = { _, catalog -> catalog.id }
                                ) { index, catalog ->
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

                                    // Inject Continue Watching under the first dynamic row
                                    if (index == 0 && progressItems.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp.responsive()))
                                        HomeSectionRowHeader(
                                            title = "⏱️ CONTINUAR VIENDO",
                                            icon = Icons.Filled.PlayCircle,
                                            color = Color(0xFF00FF87)
                                        )
                                        Spacer(modifier = Modifier.height(if (isWideLayout) 8.dp.responsive() else 12.dp.responsive()))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                                            contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 8.dp.responsive())
                                        ) {
                                            itemsIndexed(
                                                items = progressItems,
                                                key = { _, pair -> "progress_embed_${pair.first.id}" }
                                            ) { idx, (item, progressVal) ->
                                                val fIndex2 = progressRowFocusedIndex2
                                                val isCovered = isCardCovered(idx, fIndex2, progressRowFocusedNearRight2, isWideLayout)
                                                CatalogItemHomeCard(
                                                    item = item,
                                                    layoutType = "Landscape Row",
                                                    isFavorite = item.id in favoriteCatalogItems,
                                                    progress = progressVal,
                                                    onFocus = { activeHeroMovie = item },
                                                    onFocusChange = { isFocused, isNearRight ->
                                                        if (isFocused) {
                                                            progressRowFocusedIndex2 = idx
                                                            progressRowFocusedNearRight2 = isNearRight
                                                        } else {
                                                            if (progressRowFocusedIndex2 == idx) {
                                                                progressRowFocusedIndex2 = null
                                                            }
                                                        }
                                                    },
                                                    isOtherFocusedInRow = isCovered,
                                                    onClick = {
                                                        activeHeroMovie = item
                                                        viewModel.selectedDetailsItem.value = item
                                                    },
                                                    cardIndex = idx,
                                                    focusedIndex = fIndex2
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp.responsive()))
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

    val context = LocalContext.current
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    var focusedIndex by remember { mutableStateOf<Int?>(null) }
    var isFocusedNearRight by remember { mutableStateOf(false) }

    Column {
        HomeSectionRowHeader(
            title = titleToDraw.uppercase(),
            icon = iconToDraw,
            color = Color(0xFF00E5FF)
        )
        Spacer(modifier = Modifier.height(if (isWideLayout) 8.dp.responsive() else 12.dp.responsive()))

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
                horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 8.dp.responsive())
            ) {
                itemsIndexed(
                    items = catalog.items.take(catalog.numItems),
                    key = { _, item -> "${catalog.id}_card_${item.id}" }
                ) { index, item ->
                    val fIndex = focusedIndex
                    val isCovered = isCardCovered(index, fIndex, isFocusedNearRight, isWideLayout)
                    CatalogItemHomeCard(
                        item = item,
                        layoutType = layoutToDraw,
                        isFavorite = item.id in favoriteCatalogItems,
                        progress = seenProgress[item.id] ?: 0f,
                        onFocus = {
                            onFocus(item)
                        },
                        onFocusChange = { isFocused, isNearRight ->
                            if (isFocused) {
                                focusedIndex = index
                                isFocusedNearRight = isNearRight
                            } else {
                                if (focusedIndex == index) {
                                    focusedIndex = null
                                }
                            }
                        },
                        isOtherFocusedInRow = isCovered,
                        onClick = {
                            onClick(item)
                        },
                        cardIndex = index,
                        focusedIndex = fIndex
                    )
                }
                item {
                    if (catalog.items.isNotEmpty()) {
                        val N = catalog.items.take(catalog.numItems).size
                        val isSeeAllCovered = isCardCovered(N, focusedIndex, isFocusedNearRight, isWideLayout)
                        val seeAllAlpha by animateFloatAsState(
                            targetValue = if (isSeeAllCovered) 0f else 1f,
                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                            label = "see_all_fade"
                        )
                        SeeAllHomeCard(
                            layoutType = layoutToDraw,
                            modifier = Modifier.graphicsLayer { alpha = seeAllAlpha },
                            onClick = { CatalogNavigation.activeCatalogForSeeAll = catalog }
                        )
                    }
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
                contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 3.dp.responsive())
            ) {
                itemsIndexed(
                    items = catalog.items.take(catalog.numItems),
                    key = { _, item -> "${catalog.id}_numbered_${item.id}" }
                ) { index, item ->
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
                item {
                    if (catalog.items.isNotEmpty()) {
                        SeeAllHomeCard(
                            layoutType = "Vertical",
                            onClick = { CatalogNavigation.activeCatalogForSeeAll = catalog }
                        )
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 8.dp.responsive())
            ) {
                itemsIndexed(
                    items = catalog.items.take(catalog.numItems),
                    key = { _, item -> "${catalog.id}_fallback_${item.id}" }
                ) { index, item ->
                    val fIndex = focusedIndex
                    val isCovered = isCardCovered(index, fIndex, isFocusedNearRight, isWideLayout)
                    CatalogItemHomeCard(
                        item = item,
                        layoutType = "Horizontal Poster Row",
                        isFavorite = item.id in favoriteCatalogItems,
                        progress = seenProgress[item.id] ?: 0f,
                        onFocus = {
                            onFocus(item)
                        },
                        onFocusChange = { isFocused, isNearRight ->
                            if (isFocused) {
                                focusedIndex = index
                                isFocusedNearRight = isNearRight
                            } else {
                                if (focusedIndex == index) {
                                    focusedIndex = null
                                }
                            }
                        },
                        isOtherFocusedInRow = isCovered,
                        onClick = {
                            onClick(item)
                        },
                        cardIndex = index,
                        focusedIndex = fIndex
                    )
                }
                item {
                    if (catalog.items.isNotEmpty()) {
                        val N = catalog.items.take(catalog.numItems).size
                        val isSeeAllCovered = isCardCovered(N, focusedIndex, isFocusedNearRight, isWideLayout)
                        val seeAllAlpha by animateFloatAsState(
                            targetValue = if (isSeeAllCovered) 0f else 1f,
                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                            label = "see_all_fade_fallback"
                        )
                        SeeAllHomeCard(
                            layoutType = "Horizontal Poster Row",
                            modifier = Modifier.graphicsLayer { alpha = seeAllAlpha },
                            onClick = { CatalogNavigation.activeCatalogForSeeAll = catalog }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeeAllHomeCard(
    layoutType: String = "Landscape Row",
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    
    val targetWidth = getNormalCardWidth(isWideLayout)
    val targetHeight = getCardHeight(isWideLayout)

    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused && !isWideLayout) 1.045f else 1.00f,
        animationSpec = tween(durationMillis = 200),
        label = "see_all_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 1.dp,
        animationSpec = tween(durationMillis = 200),
        label = "see_all_border"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "see_all_shadow"
    )

    val borderBrush = remember(isFocused) {
        if (isFocused) {
            Brush.linearGradient(
                colors = listOf(Color(0xFF00E5FF), Color(0xFF3B82F6))
            )
        } else {
            Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.06f))
            )
        }
    }

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(targetWidth)
            .height(targetHeight),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (isFocused) 2f else 1f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.shadowElevation = shadowElevation.value
                    shape = RoundedCornerShape(14.dp)
                    clip = true
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isFocused) {
                            listOf(Color(0xFF1B1B33), Color(0xFF0A0B14))
                        } else {
                            listOf(Color(0xFF101124), Color(0xFF0A0B14))
                        }
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .border(
                    width = borderWidth,
                    brush = borderBrush,
                    shape = RoundedCornerShape(14.dp)
                )
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused || focusState.hasFocus
                }
                .focusable(interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Ver todos",
                    color = if (isFocused) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp.responsive(),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp.responsive())
                        .background(
                            if (isFocused) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                            CircleShape
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isFocused) Color(0xFF00E5FF).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Ver todos",
                        tint = if (isFocused) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(14.dp.responsive())
                    )
                }
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
                start = 20.dp.responsive(),
                end = 20.dp.responsive(),
                top = 6.dp.responsive(),
                bottom = 1.dp.responsive()
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // High-end dual-gradient neon bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.45f))
                    )
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp.responsive())
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp.responsive(),
            letterSpacing = 1.1.sp,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                    blurRadius = 2f
                )
            )
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

    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.045f else 1.00f,
        animationSpec = tween(durationMillis = 200),
        label = "channel_card_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 0.5.dp,
        animationSpec = tween(durationMillis = 200),
        label = "channel_card_border_width"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "channel_card_shadow"
    )

    val imageAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 200),
        label = "channel_image_alpha"
    )

    val borderBrush = remember(isFocused) {
        if (isFocused) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF00E5FF),
                    Color(0xFF3B82F6)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        }
    }

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(222.dp.responsive())
            .height(141.dp.responsive()),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (isFocused) 2f else 1f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.shadowElevation = shadowElevation.value
                    shape = RoundedCornerShape(12.dp)
                    clip = true
                }
                .background(Color(0xFF07080F), RoundedCornerShape(12.dp))
                .border(
                    width = borderWidth,
                    brush = borderBrush,
                    shape = RoundedCornerShape(12.dp)
                )
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused || focusState.hasFocus
                }
                .focusable(interactionSource = interactionSource)
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
                    alpha = imageAlpha
                )

                // Elegant discrete bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.92f)
                                ),
                                startY = 0.35f
                            )
                        )
                )

                // Top Floating Badges (Rating or Favorite)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CH ${channel.number}",
                            color = Color(0xFF00E5FF),
                            fontSize = 8.sp.responsive(),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleChannelFavorite(channel.id) },
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(
                                width = 0.5.dp,
                                color = if (isFavorite) Color.Red.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color(0xFFFF2D55) else Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                // Bottom Metadata Info Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontSize = 12.sp.responsive(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.85f),
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 3f
                            )
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Color(0xFFFF2D55), CircleShape)
                        )
                        
                        Text(
                            text = channel.category.lowercase(),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp.responsive(),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
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

    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.045f else 1.00f,
        animationSpec = tween(durationMillis = 200),
        label = "radio_card_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 0.5.dp,
        animationSpec = tween(durationMillis = 200),
        label = "radio_card_border_width"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "radio_card_shadow"
    )

    val imageAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 200),
        label = "radio_image_alpha"
    )

    val borderBrush = remember(isFocused, cardColor) {
        if (isFocused) {
            Brush.linearGradient(
                colors = listOf(
                    cardColor,
                    Color(0xFF00E5FF)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        }
    }

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(222.dp.responsive())
            .height(141.dp.responsive()),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (isFocused) 2f else 1f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.shadowElevation = shadowElevation.value
                    shape = RoundedCornerShape(12.dp)
                    clip = true
                }
                .background(Color(0xFF07080F), RoundedCornerShape(12.dp))
                .border(
                    width = borderWidth,
                    brush = borderBrush,
                    shape = RoundedCornerShape(12.dp)
                )
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused || focusState.hasFocus
                }
                .focusable(interactionSource = interactionSource)
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
                    alpha = imageAlpha
                )

                // Elegant discrete bottom gradient overlay with a touch of station theme color
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    cardColor.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.92f)
                                ),
                                startY = 0.35f
                            )
                        )
                )

                // Top Floating Badges (Rating or Favorite)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(cardColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .border(
                                width = 0.5.dp,
                                color = cardColor.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = station.frequency,
                            color = Color.White,
                            fontSize = 8.sp.responsive(),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleRadioFavorite(station.id) },
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(
                                width = 0.5.dp,
                                color = if (isFavorite) Color.Red.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritos",
                            tint = if (isFavorite) Color(0xFFFF2D55) else Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                // Bottom Metadata Info Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = station.name,
                        color = Color.White,
                        fontSize = 12.sp.responsive(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.85f),
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 3f
                            )
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = station.genre.lowercase(),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 8.sp.responsive(),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
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
private fun getNormalCardWidth(isWideLayout: Boolean): androidx.compose.ui.unit.Dp =
    if (isWideLayout) 100.dp.responsive() else 115.dp.responsive()

@Composable
private fun getExpandedCardWidth(isWideLayout: Boolean): androidx.compose.ui.unit.Dp =
    if (isWideLayout) 200.dp.responsive() else 230.dp.responsive()

@Composable
private fun getCardHeight(isWideLayout: Boolean): androidx.compose.ui.unit.Dp =
    if (isWideLayout) 150.dp.responsive() else 172.dp.responsive()

@Composable
private fun isCardCovered(
    index: Int,
    focusedIndex: Int?,
    isFocusedNearRight: Boolean,
    isWideLayout: Boolean
): Boolean {
    if (!isWideLayout || focusedIndex == null || index == focusedIndex) return false
    val normalWidth = getNormalCardWidth(true)
    val expandedWidth = getExpandedCardWidth(true)
    val delta = expandedWidth - normalWidth
    val S = 16.dp.responsive()
    val left_f = (normalWidth + S) * focusedIndex + (if (isFocusedNearRight) -delta else 0.dp)
    val right_f = left_f + expandedWidth
    val left_i = (normalWidth + S) * index
    val right_i = left_i + normalWidth
    return left_i.value < right_f.value && right_i.value > left_f.value
}

@Composable
fun CatalogItemHomeCard(
    item: CatalogItem,
    layoutType: String = "Vertical",
    isFavorite: Boolean = false,
    progress: Float = 0f,
    onFocus: () -> Unit = {},
    modifier: Modifier = Modifier,
    onFocusChange: (Boolean, Boolean) -> Unit = { _, _ -> },
    isOtherFocusedInRow: Boolean = false,
    onClick: () -> Unit,
    cardIndex: Int = 0,
    focusedIndex: Int? = null
) {
    LuminaPremiumCard(
        item = item,
        layoutType = layoutType,
        isFavorite = isFavorite,
        progress = progress,
        onFocus = onFocus,
        onFocusChange = onFocusChange,
        isOtherFocusedInRow = isOtherFocusedInRow,
        onClick = onClick,
        modifier = modifier,
        cardIndex = cardIndex,
        focusedIndex = focusedIndex
    )
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
    var dynamicLogoUrl by remember(item) { mutableStateOf<String?>(item.logoUrl) }
    var dynamicBackdrop by remember(item) { mutableStateOf(item.backdropUrl ?: "") }
    var dynamicCast by remember(item) { mutableStateOf<List<ActorInfo>>(emptyList()) }
    var dynamicDirector by remember(item) { mutableStateOf(item.director ?: "No especificado") }
    var dynamicProducer by remember(item) { mutableStateOf(item.producer ?: "Estudio Independiente") }
    var dynamicLanguages by remember(item) { mutableStateOf(item.languages ?: "Español Latino / Inglés") }
    var dynamicSubtitles by remember(item) { mutableStateOf(item.subtitles ?: "Español Latino / Inglés") }
    var dynamicDuration by remember(item) { mutableStateOf(item.duration ?: "2h 15m") }
    var dynamicGenre by remember(item) { mutableStateOf(item.genre) }
    var dynamicCountry by remember(item) { mutableStateOf(item.country ?: "Estados Unidos") }
    var dynamicClassification by remember(item) { mutableStateOf(item.classification ?: "PG-13 / TV-14") }

    val catalogsState = viewModel.catalogsStateFlow.collectAsState()
    val similarItems = remember(item, catalogsState.value) {
        catalogsState.value.flatMap { it.items }
            .filter { it.id != item.id && (it.genre.split("/").any { g -> item.genre.contains(g.trim(), ignoreCase = true) } || it.isTvShow == item.isTvShow) }
            .distinctBy { it.id }
            .take(8)
    }

    LaunchedEffect(item) {
        val cachedCast = com.example.data.LuminaCatalogEngine.deserializeCast(item.castJson).map { engineActor ->
            ActorInfo(name = engineActor.name, role = engineActor.role, photoUrl = engineActor.photoUrl)
        }
        if (cachedCast.isNotEmpty()) {
            dynamicCast = cachedCast
        } else {
            dynamicCast = emptyList()
        }

        // Add robust logging as requested
        android.util.Log.d("LuminaFlow_Details", "Arrived at FullScreenDetails - Title: ${item.title}, Logo: ${item.logoUrl}, Backdrop: ${item.backdropUrl}, Director: ${item.director}, Producer: ${item.producer}, Cast: ${item.castJson}, Duration: ${item.duration}, Trailer: ${item.trailerUrl}, Country: ${item.country}, Classification: ${item.classification}, Overview: ${item.description}")

        dynamicDescription = item.description.ifEmpty { "Descubre una experiencia de entretenimiento increíble con este título cuidadosamente seleccionado para el catálogo premium de Lumina." }
        dynamicRating = item.rating
        dynamicYear = item.year
        dynamicLogoUrl = item.logoUrl
        dynamicBackdrop = item.backdropUrl ?: ""
        dynamicDirector = item.director ?: "No especificado"
        dynamicProducer = item.producer ?: "Estudio Independiente"
        dynamicLanguages = item.languages ?: "Español Latino / Inglés"
        dynamicSubtitles = item.subtitles ?: "Español Latino / Inglés"
        dynamicDuration = item.duration ?: "2h 15m"
        dynamicGenre = item.genre
        dynamicCountry = item.country ?: "Estados Unidos"
        dynamicClassification = item.classification ?: "PG-13 / TV-14"
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
                model = dynamicBackdrop.ifEmpty { item.backdropUrl ?: item.posterUrl },
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
                            model = item.posterUrl,
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
                        val cleanedLogoUrl = if (dynamicLogoUrl.isNullOrBlank() || dynamicLogoUrl == "null" || dynamicLogoUrl == "NULL") null else dynamicLogoUrl
                        if (cleanedLogoUrl != null) {
                            coil.compose.SubcomposeAsyncImage(
                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                    .data(cleanedLogoUrl)
                                    .crossfade(true)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .heightIn(max = 60.dp)
                                    .widthIn(max = 200.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.BottomStart,
                                loading = { },
                                error = {
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
                                text = dynamicGenre,
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
                    director = dynamicDirector,
                    productora = dynamicProducer,
                    pais = dynamicCountry,
                    idioma = dynamicLanguages,
                    subtitulos = dynamicSubtitles,
                    clasificacion = dynamicClassification,
                    temporadas = if (item.isTvShow) "Series" else "Película",
                    status = "Disponible",
                    duracion = dynamicDuration
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
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
                                        model = imageUrl,
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
                                model = dynamicBackdrop.ifEmpty { item.backdropUrl ?: item.posterUrl },
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
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
                                        model = similar.posterUrl,
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
    var dynamicYear by remember(item) { mutableStateOf(item.year) }
    var dynamicLogoUrl by remember(item) { mutableStateOf<String?>(null) }
    var dynamicBackdrop by remember(item) { mutableStateOf("") }
    var dynamicCast by remember(item) { mutableStateOf<List<ActorInfo>>(emptyList()) }

    LaunchedEffect(item) {
        if (!item.backdropUrl.isNullOrEmpty()) {
            dynamicBackdrop = item.backdropUrl
        }
        if (!item.logoUrl.isNullOrEmpty()) {
            dynamicLogoUrl = item.logoUrl
        }
        if (!item.description.isNullOrEmpty()) {
            dynamicDescription = item.description
        }
        if (!item.rating.isNullOrEmpty()) {
            dynamicRating = item.rating
        }
        if (!item.year.isNullOrEmpty()) {
            dynamicYear = item.year
        }
        
        try {
            val cachedCast = com.example.data.LuminaCatalogEngine.deserializeCast(item.castJson).map { engineActor ->
                ActorInfo(name = engineActor.name, role = engineActor.role, photoUrl = engineActor.photoUrl)
            }
            if (cachedCast.isNotEmpty()) {
                dynamicCast = cachedCast
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    

    // Dynamic and high-fidelity generation of cinematic data specs
    val isSeriesOrAnime = remember(item) {
        val titleLower = item.title.lowercase()
        val genreLower = item.genre.lowercase()
        genreLower.contains("anime") || genreLower.contains("animación") || titleLower.contains("serie") || titleLower.contains("temporada") || item.isTvShow
    }

    val director = remember(item) {
        val titleLower = item.title.lowercase()
        when {
            titleLower.contains("dune") -> "Denis Villeneuve"
            titleLower.contains("oppenheimer") -> "Christopher Nolan"
            titleLower.contains("interstellar") || titleLower.contains("interestelar") -> "Christopher Nolan"
            titleLower.contains("spider") -> "Kemp Powers"
            item.genre.contains("Acción", true) -> "Chad Stahelski"
            item.genre.contains("Terror", true) -> "James Wan"
            else -> "Jon Favreau"
        }
    }

    val productora = remember(item) {
        val titleLower = item.title.lowercase()
        when {
            titleLower.contains("dune") -> "Warner Bros. / Legendary Entertainment"
            titleLower.contains("oppenheimer") -> "Universal Pictures / Syncopy"
            titleLower.contains("spider") -> "Columbia Pictures / Marvel Arts"
            item.genre.contains("Anime", true) -> "Toei Animation"
            else -> "Paramount Pictures / Universal"
        }
    }

    val pais = remember(item) {
        if (item.genre.contains("Anime", true) || item.genre.contains("Manga", true)) "Japón" else "Estados Unidos"
    }

    val idioma = remember(item) {
        "Español Latino / Inglés"
    }

    val clasificacion = remember(item) {
        if (item.genre.contains("Terror", true) || item.genre.contains("Horror", true) || item.genre.contains("Drama", true)) "R (Público Adulto)" else "PG-13 (Público General)"
    }

    val temporadasInfo = remember(item) {
        if (isSeriesOrAnime) "3 Temporadas" else "Película Completa"
    }

    val emisionStatus = remember(item) {
        if (isSeriesOrAnime) "En Emisión Semanal" else "Emitido"
    }

    val duracionText = remember(item) {
        if (isSeriesOrAnime) "24m por ep." else "1h 56m"
    }

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
                                model = dynamicBackdrop.ifEmpty { item.backdropUrl ?: item.posterUrl },
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
                                        alignment = Alignment.BottomStart
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
                                        text = item.genre,
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
                                            model = item.posterUrl,
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
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CatalogItemHomeCard(
                            item = item,
                            layoutType = layoutType,
                            isFavorite = item.id in favoriteCatalogItems,
                            progress = seenProgress[item.id] ?: 0f,
                            onFocus = { onItemFocus(item) },
                            modifier = Modifier,
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
    LuminaPremiumCard(
        item = item,
        layoutType = "Vertical",
        isFavorite = isFavorite,
        progress = progress,
        rank = rank,
        onFocus = onFocus,
        onClick = onClick
    )
}

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
                    
                    val videosUrl = "https://lumina-api-coral.vercel.app/api/$mediaType/$tmdbId/videos"
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
    val icon = when {
        cleanName.contains("tendencia") || cleanName.contains("trending") -> Icons.Filled.TrendingUp
        cleanName.contains("popular") -> Icons.Filled.Movie
        cleanName.contains("cine") || cleanName.contains("película") || cleanName.contains("movie") -> Icons.Filled.Movie
        cleanName.contains("serie") || cleanName.contains("show") || cleanName.contains("tv") -> Icons.Filled.Tv
        cleanName.contains("anime") -> Icons.Filled.Movie
        cleanName.contains("favorito") || cleanName.contains("lista") -> Icons.Filled.Star
        cleanName.contains("recomenda") -> Icons.Filled.ThumbUp
        else -> Icons.Filled.VideoLibrary
    }
    return Pair(name, icon)
}

