package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogItem
import com.example.ui.MediaViewModel
import com.example.ui.components.responsive

@Composable
fun HomeTvScreen(
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

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

    val featuredMovies = remember(catalogs) {
        catalogs.filter { it.isVisible && it.showInHome }
            .flatMap { it.items }
            .filter { it.posterUrl.isNotEmpty() && !it.posterUrl.contains("unsplash.com") && !it.posterUrl.contains("images.unsplash") }
            .distinctBy { it.id }
            .take(12)
    }

    val favoriteCatalogItems by viewModel.favoriteCatalogItems.collectAsState()
    val seenProgress by viewModel.seenProgress.collectAsState()

    val homeCatalogs = remember(catalogs) { catalogs.filter { it.isVisible && it.showInHome } }
    val firstNonEmptyCatalogIndex = remember(homeCatalogs) { homeCatalogs.indexOfFirst { it.items.isNotEmpty() } }
    
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

    val heroFocusRequester = remember { FocusRequester() }
    val firstRowFocusRequester = remember { FocusRequester() }

    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    val bannerHeight = 300.dp

    val isLoadingData = catalogs.isEmpty() || featuredMovies.isEmpty()

    var activeTrailerItem by remember { mutableStateOf<CatalogItem?>(null) }
    val trailerToShow = activeTrailerItem ?: viewModel.activeTrailerItem

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
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(if (isWideLayout) 36.dp.responsive() else 16.dp.responsive()),
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        bottom = 90.dp
                    )
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .focusRequester(heroFocusRequester)
                                .focusProperties {
                                    if (progressItems.isNotEmpty() || firstNonEmptyCatalogIndex != -1) {
                                        down = firstRowFocusRequester
                                    }
                                }
                                .focusGroup()
                        ) {
                            HomeHeroBannerTv(
                                featuredMovies = featuredMovies,
                                favoriteCatalogItems = favoriteCatalogItems,
                                bannerHeight = bannerHeight,
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

                    if (progressItems.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .focusRequester(firstRowFocusRequester)
                                    .focusProperties {
                                        up = heroFocusRequester
                                    }
                                    .focusGroup()
                            ) {
                                Column {
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
                                        itemsIndexed(progressItems) { index, (item, progressVal) ->
                                            val fIndex = progressRowFocusedIndex
                                            val isCovered = isCardCovered(index, fIndex, progressRowFocusedNearRight, isWideLayout)
                                            CatalogItemHomeCard(
                                                item = item,
                                                layoutType = "Landscape Row",
                                                isFavorite = item.id in favoriteCatalogItems,
                                                progress = progressVal,
                                                onFocus = {},
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
                                                    viewModel.selectedDetailsItem.value = item
                                                },
                                                cardIndex = index,
                                                focusedIndex = fIndex
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    homeCatalogs.forEachIndexed { index, catalog ->
                        if (catalog.items.isNotEmpty()) {
                            item(key = "catalog_${catalog.name}") {
                                val isFirstRow = progressItems.isEmpty() && index == firstNonEmptyCatalogIndex
                                Box(
                                    modifier = Modifier
                                        .then(if (isFirstRow) Modifier.focusRequester(firstRowFocusRequester) else Modifier)
                                        .focusProperties {
                                            if (isFirstRow) {
                                                up = heroFocusRequester
                                            }
                                        }
                                        .focusGroup()
                                ) {
                                    val (displayName, displayIcon) = getCategoryDisplayInfo(catalog.name)
                                    DrawCatalogRow(
                                        catalog = catalog,
                                        favoriteCatalogItems = favoriteCatalogItems,
                                        seenProgress = seenProgress,
                                        customTitle = displayName,
                                        customIcon = displayIcon,
                                        onFocus = {},
                                        onClick = { clickedItem ->
                                            viewModel.selectedDetailsItem.value = clickedItem
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
