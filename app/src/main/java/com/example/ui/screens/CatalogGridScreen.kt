package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Catalog
import com.example.ui.MediaViewModel
import com.example.ui.components.ElegantBackground
import com.example.ui.components.responsive
import com.example.ui.components.tvFocusEffect

object CatalogNavigation {
    var activeCatalogForSeeAll by mutableStateOf<Catalog?>(null)
}

@Composable
fun CatalogGridScreen(
    catalog: Catalog,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isWideLayout = configuration.screenWidthDp >= 580
    val columnsCount = if (isWideLayout) 6 else 3

    val favoriteCatalogItems by viewModel.favoriteCatalogItems.collectAsState()
    val seenProgress by viewModel.seenProgress.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080F))
    ) {
        // Atmospheric gradient/line background matching Lumina's visual look
        ElegantBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWideLayout) 32.dp else 16.dp)
                .padding(top = if (isWideLayout) 24.dp else 16.dp, bottom = 16.dp)
        ) {
            // TOP HEADER ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp.responsive()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp.responsive())
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp.responsive())
                    )
                }

                Spacer(modifier = Modifier.width(16.dp.responsive()))

                Column {
                    Text(
                        text = catalog.name.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp.responsive(),
                        letterSpacing = 1.sp
                    )
                    
                    val itemsText = if (catalog.items.size == 1) "1 elemento" else "${catalog.items.size} elementos"
                    Text(
                        text = itemsText,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp.responsive()
                    )
                }
            }

            // GRID CONTENT
            if (catalog.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VideoLibrary,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(64.dp.responsive())
                        )
                        Spacer(modifier = Modifier.height(12.dp.responsive()))
                        Text(
                            text = "Este catálogo está vacío",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp.responsive(),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                    verticalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp.responsive())
                ) {
                    items(catalog.items) { item ->
                        CatalogItemHomeCard(
                            item = item,
                            layoutType = "Vertical",
                            isFavorite = item.id in favoriteCatalogItems,
                            progress = seenProgress[item.id] ?: 0f,
                            onFocus = {
                                // Optional hook for TV focus
                            },
                            onClick = {
                                // Open detailed view of movie/show
                                viewModel.selectedDetailsItem.value = item
                            }
                        )
                    }
                }
            }
        }
    }
}
