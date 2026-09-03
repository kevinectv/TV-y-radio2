package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MatchStatus
import com.example.data.model.SportMatch
import com.example.ui.components.LocalTvFocusReporter
import com.example.ui.components.LocalTvRowCoordinates
import com.example.ui.components.TvFocusBounds
import com.example.ui.components.responsive
import com.example.ui.components.tvFocusEffect

@Composable
fun SportCardTv(
    match: SportMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFocusChanged: ((Boolean) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val cardBg = if (isFocused) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF222838),
                Color(0xFF181C28)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF141722),
                Color(0xFF0F121A)
            )
        )
    }

    val activeCyan = Color(0xFF00E5FF)
    val liveRed = Color(0xFFFF2D55)

    Box(
        modifier = modifier
            .width(280.dp.responsive())
            .height(132.dp.responsive())
            .onFocusChanged {
                isFocused = it.isFocused
                onFocusChanged?.invoke(it.isFocused)
            }
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .tvFocusEffect(
                shape = RoundedCornerShape(14.dp),
                focusedBorderColor = if (match.status == MatchStatus.LIVE) liveRed else Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                borderWidth = 2.dp,
                scaleAmount = 1.04f
            )
            .clickable { onClick() }
            .padding(12.dp.responsive())
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER: Competition & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Competition Name
                Text(
                    text = match.competition.uppercase(),
                    fontSize = 9.sp.responsive(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Status Badge
                when (match.status) {
                    MatchStatus.LIVE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(liveRed.copy(alpha = 0.20f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(liveRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VIVO ${match.minute ?: ""}".trim(),
                                fontSize = 9.sp.responsive(),
                                fontWeight = FontWeight.Bold,
                                color = liveRed
                            )
                        }
                    }
                    MatchStatus.FINISHED -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FINAL",
                                fontSize = 9.sp.responsive(),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                    MatchStatus.SCHEDULED -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(activeCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (match.dateDisplay.isNotEmpty()) "${match.dateDisplay} ${match.startTime}".trim() else match.startTime.ifEmpty { "Próx." },
                                fontSize = 9.sp.responsive(),
                                fontWeight = FontWeight.Medium,
                                color = activeCyan
                            )
                        }
                    }
                    MatchStatus.POSTPONED -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFA000).copy(alpha = 0.20f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "APLAZADO",
                                fontSize = 9.sp.responsive(),
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFA000)
                            )
                        }
                    }
                }
            }

            // 2. MIDDLE ROW: Local Team vs Away Team & Score
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamLogo(
                        url = match.homeTeam.logoUrl,
                        teamName = match.homeTeam.name,
                        modifier = Modifier.size(26.dp.responsive())
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam.shortName.ifEmpty { match.homeTeam.name },
                        fontSize = 11.sp.responsive(),
                        fontWeight = if (match.homeScore != null && match.awayScore != null && match.homeScore > match.awayScore) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Center Score or VS
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (match.status == MatchStatus.LIVE) liveRed.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.homeScore != null && match.awayScore != null) {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            fontSize = 13.sp.responsive(),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (match.status == MatchStatus.LIVE) Color.White else Color.White.copy(alpha = 0.9f)
                        )
                    } else {
                        Text(
                            text = "VS",
                            fontSize = 10.sp.responsive(),
                            fontWeight = FontWeight.Bold,
                            color = activeCyan
                        )
                    }
                }

                // Away Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.awayTeam.shortName.ifEmpty { match.awayTeam.name },
                        fontSize = 11.sp.responsive(),
                        fontWeight = if (match.homeScore != null && match.awayScore != null && match.awayScore > match.homeScore) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamLogo(
                        url = match.awayTeam.logoUrl,
                        teamName = match.awayTeam.name,
                        modifier = Modifier.size(26.dp.responsive())
                    )
                }
            }

            // 3. BOTTOM ROW: Transmission Broadcaster or Venue (if available)
            if (match.broadcasts.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LiveTv,
                        contentDescription = "Transmisión",
                        tint = activeCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp.responsive())
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.broadcasts.take(2).joinToString(", "),
                        fontSize = 9.sp.responsive(),
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else if (!match.venue.isNullOrEmpty()) {
                Text(
                    text = "📍 ${match.venue}",
                    fontSize = 9.sp.responsive(),
                    color = Color.White.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}

@Composable
fun TeamLogo(
    url: String?,
    teamName: String,
    modifier: Modifier = Modifier
) {
    if (!url.isNullOrEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(false)
                .build(),
            contentDescription = teamName,
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    } else {
        // Initials fallback badge
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = teamName.take(2).uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
