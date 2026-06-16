package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
        val lists = catalogs.flatMap { it.items }.filter { it.posterUrl.isNotEmpty() }.distinctBy { it.id }
        if (lists.isNotEmpty()) {
            lists.take(5)
        } else {
            listOf(
                CatalogItem(
                    id = "m1", title = "Dune: Parte Dos",
                    posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=600",
                    year = "2024", rating = "8.7", genre = "Sci-Fi",
                    description = "Paul Atreides se une a Chani y los Fremen mientras busca venganza contra quienes destruyeron a su familia."
                ),
                CatalogItem(
                    id = "m2", title = "Oppenheimer",
                    posterUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?q=80&w=600",
                    year = "2023", rating = "8.9", genre = "Historia",
                    description = "La historia del físico estadounidense J. Robert Oppenheimer y su papel en el desarrollo de la bomba atómica del Proyecto Manhattan."
                ),
                CatalogItem(
                    id = "m3", title = "Spider-Man: Across the Spider-Verse",
                    posterUrl = "https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=600",
                    year = "2023", rating = "9.0", genre = "Animación",
                    description = "Miles Morales se embarca en una aventura a través del multiverso junto a Gwen Stacy para enfrentar una nueva amenaza espectacular."
                ),
                CatalogItem(
                    id = "m4", title = "Anatomía de una Caída",
                    posterUrl = "https://images.unsplash.com/photo-1585647347483-22b66260dfff?q=80&w=600",
                    year = "2023", rating = "8.1", genre = "Drama",
                    description = "Una mujer es sospechosa de la muerte de su esposo en un remoto chalet de montaña alpino."
                ),
                CatalogItem(
                    id = "m5", title = "Interestelar",
                    posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=600",
                    year = "2014", rating = "8.6", genre = "Sci-Fi",
                    description = "Un grupo de astronautas se embarca en una expedición heroica a través de un agujero de gusano para salvar el destino de la Tierra."
                )
            )
        }
    }

    // Auto-rolling slide interval
    var carouselIndex by remember(featuredMovies) { mutableStateOf(0) }
    LaunchedEffect(featuredMovies) {
        if (featuredMovies.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(4500L) // Switch slide every 4.5 seconds
                carouselIndex = (carouselIndex + 1) % featuredMovies.size
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- SECCIÓN 1: PANTALLAS GRANDES DE PELÍCULAS EN CARRUSEL (AUTO-RODANTE) ---
        item {
            if (featuredMovies.isNotEmpty()) {
                val currentMovie = featuredMovies[carouselIndex]
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedCatalogItem = currentMovie
                            showDetailsDialog = true
                        }
                        .tvFocusEffect(shape = RoundedCornerShape(16.dp))
                ) {
                    // Smooth Crossfade Animated transition of posters
                    Crossfade(
                        targetState = currentMovie,
                        animationSpec = tween(durationMillis = 800)
                    ) { targetMovie ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = targetMovie.posterUrl,
                                contentDescription = targetMovie.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Glassmorphism elegant bottom dark gradient vignette
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.25f),
                                                Color.Black.copy(alpha = 0.9f)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // Content details row and columns
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Header metadata
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PANEL CINEMATOGRÁFICO",
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp,
                                    modifier = Modifier
                                        .background(Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentMovie.genre.uppercase(),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Rating Badge
                            Row(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentMovie.rating,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Bottom Title, Descriptions, Action button & Indicator Dots
                        Column {
                            Text(
                                text = currentMovie.title,
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = currentMovie.description,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(end = 24.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Full details popup trigger action button
                                Button(
                                    onClick = {
                                        selectedCatalogItem = currentMovie
                                        showDetailsDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ver Información", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }

                                // Carousel active slide dots
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    featuredMovies.indices.forEach { idx ->
                                        val active = (idx == carouselIndex)
                                        Box(
                                            modifier = Modifier
                                                .size(if (active) 7.dp else 5.dp)
                                                .clip(CircleShape)
                                                .background(if (active) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.35f))
                                                .clickable { carouselIndex = idx }
                                        )
                                    }
                                }
                            }
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

                // Layout Choice selector checking
                if (catalog.layoutType == "Vertical") {
                    CatalogVerticalGrid(
                        items = catalog.items.take(catalog.numItems),
                        onClick = { item ->
                            selectedCatalogItem = item
                            showDetailsDialog = true
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
                                onClick = {
                                    selectedCatalogItem = item
                                    showDetailsDialog = true
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
                                onClick = {
                                    selectedCatalogItem = item
                                    showDetailsDialog = true
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

    if (showDetailsDialog && selectedCatalogItem != null) {
        CatalogItemDetailsDialog(
            item = selectedCatalogItem!!,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val appliedModifier = if (modifier == Modifier) Modifier.width(110.dp) else modifier
    Card(
        modifier = appliedModifier
            .clickable { onClick() }
            .tvFocusEffect(shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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

                // Dynamic Year overlay gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
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
                                Toast.makeText(context, "Sintonizando transmisión de '${item.title}'...", Toast.LENGTH_LONG).show()
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(175.dp)
            .clickable { onClick() }
            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
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

                // Dynamic Year overlay gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
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
            }
        }
    }
}
