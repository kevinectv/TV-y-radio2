package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.example.ui.AppTab
import com.example.ui.MediaViewModel

@Composable
fun WatchlistScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteChans by viewModel.favoriteChannels.collectAsState()
    val favoriteRadios by viewModel.favoriteRadioStations.collectAsState()
    val recentChans by viewModel.recentChannels.collectAsState()
    val recentRadios by viewModel.recentRadioStations.collectAsState()

    val totalFavorites = favoriteChans.size + favoriteRadios.size
    val totalHistory = recentChans.size + recentRadios.size

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Title Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MI BIBLIOTECA MULTIMEDIA",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Gestiona tus favoritos guardados y tu historial de reproducciones recientes.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }

                if (totalHistory > 0) {
                    Button(
                        onClick = { viewModel.clearRecentsHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar Historial",
                            tint = Color.Red,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Limpiar Historial", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- EMPTY STATE PROMPT ---
        if (totalFavorites == 0 && totalHistory == 0) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.widthIn(max = 350.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Empty Watchlist",
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tu Watchlist está vacía",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Navega por la sección Home o los canales de TV y pulsa en el corazón para almacenar tus favoritos aquí al instante.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- FAVORITE FILES ---
        if (favoriteChans.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeSectionRowHeader(title = "CANALES DE TELEVISIÓN FAVORITOS", icon = Icons.Default.Favorite, color = Color.Red)
            }

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
        }

        if (favoriteRadios.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeSectionRowHeader(title = "EMISORAS DE RADIO FAVORITAS", icon = Icons.Default.Favorite, color = Color.Red)
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

        // --- RECENT PLAYS ---
        if (recentChans.isNotEmpty() || recentRadios.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeSectionRowHeader(title = "REPRODUCCIONES RECIENTES", icon = Icons.Default.FavoriteBorder, color = Color.Gray)
            }

            if (recentChans.isNotEmpty()) {
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
            }

            if (recentRadios.isNotEmpty()) {
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
}
