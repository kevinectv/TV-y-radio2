package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MatchStatus
import com.example.data.model.SportMatch
import com.example.ui.components.responsive
import com.example.ui.components.tvFocusEffect

@Composable
fun SportMatchDetailsScreenTv(
    match: SportMatch,
    onDismiss: () -> Unit
) {
    val closeFocusRequester = remember { FocusRequester() }
    val activeCyan = Color(0xFF00E5FF)
    val liveRed = Color(0xFFFF2D55)

    LaunchedEffect(Unit) {
        try {
            closeFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() }
                .padding(32.dp.responsive()),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121C)),
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxHeight(0.90f)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp.responsive())
                ) {
                    // 1. TOP BAR: Competition & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SportsSoccer,
                                contentDescription = null,
                                tint = activeCyan,
                                modifier = Modifier.size(20.dp.responsive())
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = match.competition.uppercase(),
                                fontSize = 14.sp.responsive(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Close button with focus
                        var isCloseFocused by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .focusRequester(closeFocusRequester)
                                .onFocusChanged { isCloseFocused = it.isFocused }
                                .clip(CircleShape)
                                .background(if (isCloseFocused) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                                .tvFocusEffect(
                                    shape = CircleShape,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.Transparent,
                                    borderWidth = 2.dp
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp.responsive()))

                    // 2. HERO MATCHUP SCOREBOARD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1B2032), Color(0xFF121624))
                                )
                            )
                            .padding(20.dp.responsive()),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Home Team
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                TeamLogo(
                                    url = match.homeTeam.logoUrl,
                                    teamName = match.homeTeam.name,
                                    modifier = Modifier.size(64.dp.responsive())
                                )
                                Spacer(modifier = Modifier.height(10.dp.responsive()))
                                Text(
                                    text = match.homeTeam.name,
                                    fontSize = 15.sp.responsive(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Center Score & Status
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 16.dp.responsive())
                            ) {
                                // Status Badge
                                when (match.status) {
                                    MatchStatus.LIVE -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(liveRed.copy(alpha = 0.20f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(liveRed))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "EN VIVO ${match.minute ?: ""}".trim(),
                                                fontSize = 11.sp.responsive(),
                                                fontWeight = FontWeight.Bold,
                                                color = liveRed
                                            )
                                        }
                                    }
                                    MatchStatus.FINISHED -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "FINALIZADO",
                                                fontSize = 11.sp.responsive(),
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    MatchStatus.SCHEDULED -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(activeCyan.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (match.dateDisplay.isNotEmpty()) "${match.dateDisplay} ${match.startTime}".trim() else match.startTime.ifEmpty { "Programado" },
                                                fontSize = 11.sp.responsive(),
                                                fontWeight = FontWeight.Bold,
                                                color = activeCyan
                                            )
                                        }
                                    }
                                    MatchStatus.POSTPONED -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFFA000).copy(alpha = 0.20f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "APLAZADO",
                                                fontSize = 11.sp.responsive(),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFA000)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp.responsive()))

                                if (match.homeScore != null && match.awayScore != null) {
                                    Text(
                                        text = "${match.homeScore}  ─  ${match.awayScore}",
                                        fontSize = 32.sp.responsive(),
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "VS",
                                        fontSize = 24.sp.responsive(),
                                        fontWeight = FontWeight.Black,
                                        color = activeCyan
                                    )
                                }
                            }

                            // Away Team
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                TeamLogo(
                                    url = match.awayTeam.logoUrl,
                                    teamName = match.awayTeam.name,
                                    modifier = Modifier.size(64.dp.responsive())
                                )
                                Spacer(modifier = Modifier.height(10.dp.responsive()))
                                Text(
                                    text = match.awayTeam.name,
                                    fontSize = 15.sp.responsive(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp.responsive()))

                    // 3. INFORMATION AND BROADCASTS ROW
                    if (match.broadcasts.isNotEmpty() || !match.venue.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (match.broadcasts.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LiveTv,
                                        contentDescription = "Transmisión",
                                        tint = activeCyan,
                                        modifier = Modifier.size(16.dp.responsive())
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Transmisión: ${match.broadcasts.joinToString(", ")}",
                                        fontSize = 12.sp.responsive(),
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            if (!match.venue.isNullOrEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = "Estadio",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp.responsive())
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = match.venue,
                                        fontSize = 11.sp.responsive(),
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp.responsive()))
                    }

                    // 4. EVENTS TIMELINE (IF AVAILABLE)
                    if (match.events.isNotEmpty()) {
                        Text(
                            text = "EVENTOS DEL PARTIDO",
                            fontSize = 12.sp.responsive(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(match.events) { event ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = event.time,
                                        fontSize = 11.sp.responsive(),
                                        fontWeight = FontWeight.Bold,
                                        color = activeCyan,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    Text(
                                        text = "⚽ ${event.player} (${event.teamName})",
                                        fontSize = 12.sp.responsive(),
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (event.detail.isNotEmpty()) {
                                        Text(
                                            text = event.detail,
                                            fontSize = 10.sp.responsive(),
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Información oficial proporcionada por la fuente deportiva.",
                                fontSize = 12.sp.responsive(),
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
