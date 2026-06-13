package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.AppTab
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect

@Composable
fun SearchScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val allChannels by viewModel.allChannels.collectAsState()
    val allRadioStations by viewModel.allRadioStations.collectAsState()
    val favoriteChannels by viewModel.favoriteChannels.collectAsState()
    val favoriteRadioStations by viewModel.favoriteRadioStations.collectAsState()

    // Filters Configuration
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Canales de TV", "Emisoras de Radio", "Favoritos")

    val categories = remember {
        listOf("Deportes", "Cine", "Noticias", "Entretenimiento", "Música")
    }
    var activeCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Unified Match Logic
    val matchedChannels = remember(query, allChannels, selectedFilter, activeCategoryFilter, favoriteChannels) {
        allChannels.filter { channel ->
            val matchesQuery = query.isEmpty() || channel.name.contains(query, ignoreCase = true) || channel.description.contains(query, ignoreCase = true)
            val matchesFilter = selectedFilter == "Todos" || selectedFilter == "Canales de TV" ||
                    (selectedFilter == "Favoritos" && favoriteChannels.any { it.id == channel.id })
            val matchesCategory = activeCategoryFilter == null || channel.category.contains(activeCategoryFilter!!, ignoreCase = true) || channel.description.contains(activeCategoryFilter!!, ignoreCase = true)

            matchesQuery && matchesFilter && matchesCategory
        }
    }

    val matchedRadioStations = remember(query, allRadioStations, selectedFilter, activeCategoryFilter, favoriteRadioStations) {
        if (selectedFilter == "Canales de TV" || activeCategoryFilter != null) emptyList() else {
            allRadioStations.filter { radio ->
                val matchesQuery = query.isEmpty() || radio.name.contains(query, ignoreCase = true) || radio.genre.contains(query, ignoreCase = true)
                val matchesFilter = selectedFilter == "Todos" || selectedFilter == "Emisoras de Radio" ||
                        (selectedFilter == "Favoritos" && favoriteRadioStations.any { it.id == radio.id })
                matchesQuery && matchesFilter
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    
    // Auto-focus search text field on entry for premium TV / Dpad UX
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        // Upper Header Title
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Title Icon",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "BÚSQUEDA INTEGRADA Y DIRECTA",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }
            
            Text(
                text = "${matchedChannels.size + matchedRadioStations.size} resultados",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search Input Box
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    "Busca películas, series, fútbol, canales de TV o emisoras radiales...",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics { testTag = "search_screen_input" },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar consulta",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedContainerColor = Color.Black.copy(alpha = 0.45f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.22f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // First horizontal row of general filter buttons
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(filters) { filterOpt ->
                val isSel = selectedFilter == filterOpt
                val activeThemeColor = if (filterOpt == "Emisoras de Radio") Color(0xFF00E5FF) else Color(0xFF4A89FF)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) activeThemeColor else Color.White.copy(alpha = 0.04f))
                        .border(1.dp, if (isSel) activeThemeColor else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .clickable { selectedFilter = filterOpt }
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filterOpt,
                        color = if (isSel) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Subcategory badges row (only showing when TV channels or general is chosen)
        if (selectedFilter == "Todos" || selectedFilter == "Canales de TV") {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    val isNone = activeCategoryFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isNone) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .clickable { activeCategoryFilter = null }
                            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "Todo-Género",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(categories) { cat ->
                    val isSel = activeCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF4A89FF).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFF4A89FF) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeCategoryFilter = cat }
                            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            cat,
                            color = if (isSel) Color(0xFF4A89FF) else Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main dynamic list container
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (matchedChannels.isEmpty() && matchedRadioStations.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "Sin resultados icon",
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sin resultados para tu búsqueda",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Valida la ortografía o cambia de categoría en los filtros superiores.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // TV Channels Section
                    if (matchedChannels.isNotEmpty()) {
                        item {
                            Text(
                                text = "CANALES DE TELEVISIÓN (${matchedChannels.size})",
                                color = Color(0xFF4A89FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(matchedChannels) { chan ->
                            val isFav = favoriteChannels.any { it.id == chan.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.selectChannel(chan)
                                    }
                                    .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = chan.logoUrl,
                                    contentDescription = chan.name,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "CH ${chan.number}",
                                            color = Color(0xFF4A89FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = chan.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isFav) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Favorito",
                                                tint = Color.Red,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = chan.description,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleChannelFavorite(chan.id) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorito icon toggle",
                                        tint = if (isFav) Color.Red else Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Ver canal",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Radio Stations Section
                    if (matchedRadioStations.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "EMISORAS DE RADIO (${matchedRadioStations.size})",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(matchedRadioStations) { rad ->
                            val isRadioFav = favoriteRadioStations.any { it.id == rad.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.selectRadioStation(rad)
                                        if (!viewModel.isRadioPlaying) {
                                            viewModel.toggleRadioPlay()
                                        }
                                        viewModel.selectTab(AppTab.RADIO)
                                    }
                                    .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = rad.logoUrl,
                                    contentDescription = rad.name,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = rad.frequency,
                                            color = Color(0xFF00E5FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = rad.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isRadioFav) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Favorito",
                                                tint = Color.Red,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rad.genre.uppercase(),
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleRadioFavorite(rad.id) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRadioFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorito radio icon toggle",
                                        tint = if (isRadioFav) Color.Red else Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Escuchar radio",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
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
