package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CatalogItem
import com.example.ui.MediaViewModel
import com.example.ui.components.responsive

@Composable
fun MoviesScreen(
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel = viewModel()
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val rawCatalogs by viewModel.catalogsStateFlow.collectAsState()
    val sharedPrefs = remember { context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE) }
    
    val movieCatalogs = remember(rawCatalogs) {
        rawCatalogs.map { cat -> 
            val override = sharedPrefs.getString("layout_override_${cat.id}", null)
            val adaptedCat = if (override != null) cat.copy(layoutType = override) else cat
            adaptedCat.copy(items = adaptedCat.items.filter { !it.isTvShow })
        }.filter { it.items.isNotEmpty() }
    }
    
    val favoriteCatalogItems by viewModel.favoriteCatalogItems.collectAsState()
    val seenProgress by viewModel.seenProgress.collectAsState()
    
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    val isLoadingData = movieCatalogs.isEmpty()
    
    var activeTrailerItem by remember { mutableStateOf<CatalogItem?>(null) }
    val trailerToShow = activeTrailerItem ?: viewModel.activeTrailerItem

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF030406))) {
        Crossfade(
            targetState = isLoadingData,
            animationSpec = tween(700),
            label = "movies_skeleton_fade",
            modifier = Modifier.fillMaxSize()
        ) { isLoading ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF00E5FF))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(if (isWideLayout) 36.dp.responsive() else 16.dp.responsive()),
                    contentPadding = PaddingValues(
                        top = if (isWideLayout) 28.dp else 16.dp,
                        bottom = 90.dp
                    )
                ) {
                    movieCatalogs.forEachIndexed { index, catalog ->
                        item(key = "movie_cat_${catalog.id}_$index") {
                            Box(modifier = Modifier.focusGroup()) {
                                val (displayName, displayIcon) = getCategoryDisplayInfo(catalog.name)
                                DrawCatalogRow(
                                    catalog = catalog,
                                    favoriteCatalogItems = favoriteCatalogItems,
                                    seenProgress = seenProgress,
                                    customTitle = displayName ?: catalog.name,
                                    customIcon = displayIcon ?: Icons.Filled.Movie,
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
