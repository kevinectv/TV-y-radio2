package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchStatus
import com.example.data.model.SportMatch
import com.example.ui.MediaViewModel
import com.example.ui.components.responsive
import com.example.ui.components.tvFocusEffect
import kotlinx.coroutines.launch

@Composable
fun SportsScreenTv(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allMatches by viewModel.sportsMatches.collectAsState()
    val liveMatches by viewModel.liveSportsMatches.collectAsState()
    val leagueSections by viewModel.sportsLeagueSections.collectAsState()
    val isLoading by viewModel.isSportsLoading.collectAsState()

    var selectedMatchForDetails by remember { mutableStateOf<SportMatch?>(null) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    val activeCyan = Color(0xFF00E5FF)
    val liveRed = Color(0xFFFF2D55)

    // Polling lifecycle for TV
    DisposableEffect(Unit) {
        viewModel.startSportsPolling()
        onDispose {
            viewModel.stopSportsPolling()
        }
    }

    val filters = remember(leagueSections, liveMatches) {
        val list = mutableListOf("Todos")
        if (liveMatches.isNotEmpty()) list.add("🔴 En Vivo")
        leagueSections.forEach { section ->
            if (section.matches.isNotEmpty() && !list.contains(section.leagueName)) {
                list.add(section.leagueName)
            }
        }
        list
    }

    val filteredMatches = remember(allMatches, liveMatches, selectedFilter) {
        when {
            selectedFilter == "🔴 En Vivo" -> liveMatches
            selectedFilter == "Todos" -> allMatches
            else -> allMatches.filter { it.competition.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080A10))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 28.dp.responsive(), bottom = 48.dp.responsive()),
            verticalArrangement = Arrangement.spacedBy(24.dp.responsive())
        ) {
            // 1. HEADER ROW: Title & Refresh Button
            item(key = "sports_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp.responsive()),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp.responsive())
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(activeCyan.copy(alpha = 0.3f), activeCyan.copy(alpha = 0.05f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SportsSoccer,
                                contentDescription = null,
                                tint = activeCyan,
                                modifier = Modifier.size(24.dp.responsive())
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp.responsive()))

                        Column {
                            Text(
                                text = "DEPORTES EN VIVO Y MARCADORES",
                                fontSize = 20.sp.responsive(),
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (liveMatches.isNotEmpty()) "${liveMatches.size} partidos en juego actualmente" else "Próximos partidos y resultados oficiales",
                                fontSize = 12.sp.responsive(),
                                color = if (liveMatches.isNotEmpty()) liveRed else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Refresh button for TV
                    var isRefreshFocused by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.refreshSportsData()
                            }
                        },
                        modifier = Modifier
                            .onFocusChanged { isRefreshFocused = it.isFocused }
                            .clip(CircleShape)
                            .background(if (isRefreshFocused) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f))
                            .tvFocusEffect(
                                shape = CircleShape,
                                focusedBorderColor = activeCyan,
                                unfocusedBorderColor = Color.Transparent,
                                borderWidth = 2.dp
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar",
                            tint = if (isLoading) activeCyan else Color.White
                        )
                    }
                }
            }

            // 2. FILTER PILLS ROW
            item(key = "sports_filters") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp.responsive()),
                    contentPadding = PaddingValues(horizontal = 32.dp.responsive())
                ) {
                    items(filters) { filterText ->
                        val isSelected = selectedFilter == filterText
                        var isFilterFocused by remember { mutableStateOf(false) }

                        val bgColor = when {
                            isFilterFocused -> activeCyan.copy(alpha = 0.35f)
                            isSelected -> activeCyan.copy(alpha = 0.20f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }

                        val textColor = when {
                            isFilterFocused || isSelected -> Color.White
                            else -> Color.White.copy(alpha = 0.65f)
                        }

                        Box(
                            modifier = Modifier
                                .onFocusChanged { isFilterFocused = it.isFocused }
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgColor)
                                .tvFocusEffect(
                                    shape = RoundedCornerShape(20.dp),
                                    focusedBorderColor = activeCyan,
                                    unfocusedBorderColor = Color.Transparent,
                                    borderWidth = 2.dp,
                                    scaleAmount = 1.05f
                                )
                                .clickable { selectedFilter = filterText }
                                .padding(horizontal = 16.dp.responsive(), vertical = 8.dp.responsive())
                        ) {
                            Text(
                                text = filterText,
                                fontSize = 12.sp.responsive(),
                                fontWeight = if (isSelected || isFilterFocused) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // 3. IF "TODOS" IS SELECTED: SHOW STRUCTURED ROWS (LIVE, FEATURED, BY LEAGUE)
            if (selectedFilter == "Todos") {
                // Section: LIVE MATCHES (if any)
                if (liveMatches.isNotEmpty()) {
                    item(key = "sports_row_live") {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 32.dp.responsive(), vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(liveRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EN VIVO AHORA",
                                    fontSize = 14.sp.responsive(),
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp.responsive()))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                                contentPadding = PaddingValues(horizontal = 32.dp.responsive())
                            ) {
                                items(liveMatches, key = { "live_${it.id}" }) { match ->
                                    SportCardTv(
                                        match = match,
                                        onClick = { selectedMatchForDetails = match }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: LEAGUE ROWS
                leagueSections.forEach { section ->
                    if (section.matches.isNotEmpty()) {
                        item(key = "sports_section_${section.leagueId}") {
                            Column {
                                Text(
                                    text = section.leagueName.uppercase(),
                                    fontSize = 13.sp.responsive(),
                                    fontWeight = FontWeight.Bold,
                                    color = activeCyan,
                                    modifier = Modifier.padding(horizontal = 32.dp.responsive(), vertical = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp.responsive()))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                                    contentPadding = PaddingValues(horizontal = 32.dp.responsive())
                                ) {
                                    items(section.matches, key = { "${section.leagueId}_${it.id}" }) { match ->
                                        SportCardTv(
                                            match = match,
                                            onClick = { selectedMatchForDetails = match }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // FILTERED GRID / ROW
                item(key = "sports_filtered_list") {
                    Column {
                        Text(
                            text = selectedFilter.uppercase(),
                            fontSize = 14.sp.responsive(),
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 32.dp.responsive(), vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp.responsive()))
                        if (filteredMatches.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                                contentPadding = PaddingValues(horizontal = 32.dp.responsive())
                            ) {
                                items(filteredMatches, key = { "filtered_${it.id}" }) { match ->
                                    SportCardTv(
                                        match = match,
                                        onClick = { selectedMatchForDetails = match }
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp.responsive(), vertical = 32.dp.responsive()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay partidos disponibles para esta categoría en este momento.",
                                    fontSize = 13.sp.responsive(),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Details Screen
    selectedMatchForDetails?.let { match ->
        SportMatchDetailsScreenTv(
            match = match,
            onDismiss = { selectedMatchForDetails = null }
        )
    }
}
