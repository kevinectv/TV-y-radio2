package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    val heroChannel = allChannels.firstOrNull() ?: MediaViewModel.DefaultChannel

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. HERO MAIN HIGHLIGHT SPOTLIGHT
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.selectChannel(heroChannel)
                        viewModel.selectTab(AppTab.TV)
                    }
                    .tvFocusEffect(shape = RoundedCornerShape(16.dp))
            ) {
                // Background image
                AsyncImage(
                    model = heroChannel.logoUrl,
                    contentDescription = heroChannel.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark and color wash gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.9f),
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
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
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                )

                // Hero content details
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 380.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DESTACADO LIVE",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .background(
                                    Color.White,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "M3U8 / IPTV PLAYER",
                            color = Color.White.copy(alpha = 0.60f),
                            fontSize = 11.sp
                        )
                    }

                    Column {
                        Text(
                            text = heroChannel.name,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = heroChannel.description,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.selectChannel(heroChannel)
                                viewModel.selectTab(AppTab.TV)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Sintonizar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sintonizar Ahora", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        // Add to Favorites
                        var isHeroFav by remember { mutableStateOf(false) }
                        LaunchedEffect(favoriteChans) {
                            isHeroFav = viewModel.isChannelFavorite(heroChannel.id)
                        }

                        IconButton(
                            onClick = { viewModel.toggleChannelFavorite(heroChannel.id) },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isHeroFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favoritos",
                                tint = if (isHeroFav) Color.Red else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. RECENTS SECTION (HORIZONTAL SCROLLER)
        val hasRecents = recentChans.isNotEmpty() || recentRadios.isNotEmpty()
        if (hasRecents) {
            item {
                HomeSectionRowHeader(title = "REPRODUCIDO RECIENTEMENTE", icon = Icons.Filled.History)
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

        // 3. FAVORITES WATCHLIST (HORIZONTAL SCROLLER)
        val hasFavorites = favoriteChans.isNotEmpty() || favoriteRadios.isNotEmpty()
        if (hasFavorites) {
            item {
                HomeSectionRowHeader(title = "MIS FAVORITOS GUARDADOS", icon = Icons.Filled.Favorite, color = Color.White)
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
        } else {
            // Friendly guide when folder is empty
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritos",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Construye tu Lista de Seguidos",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pulsa el icono del corazón en cualquier canal o radio para tenerlos guardados aquí instantáneamente.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. CHANNELS POPULARES (HORIZONTAL SCROLLER)
        item {
            HomeSectionRowHeader(title = "CANALES POPULARES IPTV", icon = Icons.Filled.Tv)
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

        // 4.5 CUSTOM SYNCED CATALOGS (FILAS DINÁMICAS INSPIRADAS EN STREMIO/PLEX)
        catalogs.filter { it.isVisible }.forEach { catalog ->
            item {
                val icon = when (catalog.sourceType) {
                    "TMDB" -> Icons.Filled.Movie
                    "Trakt" -> Icons.Filled.Tv
                    "MDBList" -> Icons.Filled.FilterAlt
                    else -> Icons.Filled.VideoLibrary
                }
                HomeSectionRowHeader(title = catalog.name.uppercase(), icon = icon, color = Color(0xFF00E5FF))
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

        // 5. RADIO POPULARES (HORIZONTAL SCROLLER)
        item {
            HomeSectionRowHeader(title = "EMISORAS DE RADIO POPULARES", icon = Icons.Filled.Radio)
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

        // 6. RECOMMENDED STREAMING
        item {
            HomeSectionRowHeader(title = "RECOMENDADOS PARA TI", icon = Icons.Filled.AutoAwesome, color = Color.White)
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
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

@Composable
fun CatalogItemDetailsDialog(
    item: CatalogItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1524),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Poster Image Left Side
                Card(
                    modifier = Modifier
                        .width(110.dp)
                        .height(165.dp),
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

                // Details Text Right Side
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )

                    // Tags row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Year badge
                        Text(
                            text = item.year,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        // Rating badge
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
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
                                text = item.rating,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Genre badge
                        Text(
                            text = item.genre,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .background(Color(0xFF00E5FF).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "SINOPSIS / RESUMEN",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = item.description,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    Toast.makeText(context, "Sintonizando la fuente de transmisión recomendada para de '${item.title}'...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reproducir", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Cerrar", color = Color.White, fontSize = 11.sp)
            }
        }
    )
}
