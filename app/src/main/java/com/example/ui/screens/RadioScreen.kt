package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RadioStation
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun RadioScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val station = viewModel.selectedRadioStation

    // Read favorite status asynchronously
    var isFavorite by remember { mutableStateOf(false) }
    LaunchedEffect(station.id, viewModel.favoriteRadioStations.collectAsState().value) {
        isFavorite = viewModel.isRadioFavorite(station.id)
    }

    // Interactive waveform bar bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_waves")
    val waveHeights = List(12) { index ->
        if (viewModel.isRadioPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + (index * 70) % 250,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave_$index"
            )
        } else {
            remember { mutableStateOf(0.15f) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dynamic Backdrop Blurred glow matching the current station color hex!
        val baseColor = remember(station.themeColorHex) {
            try {
                Color(android.graphics.Color.parseColor(station.themeColorHex))
            } catch (e: Exception) {
                Color(0xFF6B4EFE)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .background(Brush.radialGradient(
                    colors = listOf(baseColor.copy(alpha = 0.28f), Color.Transparent),
                    radius = 900f
                ))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REPRODUCTOR RADIO EN VIVO",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .background(Color(0xFFFF3B30), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(5.dp).background(Color.White, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Central Station Card: Artwork and Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            2.dp,
                            baseColor.copy(alpha = 0.6f),
                            RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = station.logoUrl,
                            contentDescription = station.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Glass Overlay at the bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = station.frequency,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = station.genre,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Interactive mini wave when playing
                                if (viewModel.isRadioPlaying) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.Bottom,
                                        modifier = Modifier.height(18.dp)
                                    ) {
                                        repeat(4) { idx ->
                                            val barHeight = rememberInfiniteTransition(label = "mini").animateFloat(
                                                initialValue = 2f,
                                                targetValue = 14f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(250 + (idx * 50), easing = EaseInOutSine),
                                                    repeatMode = RepeatMode.Reverse
                                                ),
                                                label = "minibar_$idx"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(barHeight.value.dp)
                                                    .background(Color(0xFF00FFCC))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Details Text
                Column(
                    modifier = Modifier.widthIn(max = 300.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = station.genre.uppercase(),
                            color = baseColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .background(baseColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Favorite toggle
                        IconButton(
                            onClick = {
                                viewModel.toggleRadioFavorite(station.id)
                            },
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) Color.Red else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = station.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Frecuencia de emisión: ${station.frequency} en alta fidelidad y latencia cero para Smart TV y Móvil.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bouncing equalizer visual bars
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val totalBars = waveHeights.size
                        val spacing = 8.dp.toPx()
                        val barWidth = (canvasWidth - (spacing * (totalBars - 1))) / totalBars

                        for (i in 0 until totalBars) {
                            val h = waveHeights[i].value * canvasHeight
                            val x = i * (barWidth + spacing)
                            val y = canvasHeight - h

                            drawRoundRect(
                                color = baseColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, h),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                }
            }

            // Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Interactive Row buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.selectPrevRadio() },
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .tvFocusEffect(shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // HUGE Play pause toggle with glowing effect
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(baseColor, baseColor.copy(alpha = 0.4f)),
                                    radius = 120f
                                )
                            )
                            .clickable { viewModel.toggleRadioPlay() }
                            .tvFocusEffect(shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isRadioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pausa",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.selectNextRadio() },
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .tvFocusEffect(shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Siguiente",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Volume slider
                Row(
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.radioVolume > 0.5f) Icons.Default.VolumeUp else if (viewModel.radioVolume > 0f) Icons.Default.VolumeDown else Icons.Default.VolumeMute,
                        contentDescription = "Volume",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Slider(
                        value = viewModel.radioVolume,
                        onValueChange = { viewModel.setRadioVolumeLevel(it) },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = baseColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                            thumbColor = Color.White
                        )
                    )

                    Text(
                        text = "${(viewModel.radioVolume * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }

            // Quick Station selector at footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "EMISORAS POPULARES",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(viewModel.repository.radioStationsList) { rad ->
                        val isCurrent = rad.id == station.id
                        Card(
                            modifier = Modifier
                                .width(130.dp)
                                .height(58.dp)
                                .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectRadioStation(rad) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) baseColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) baseColor else Color.White.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AsyncImage(
                                    model = rad.logoUrl,
                                    contentDescription = rad.name,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rad.name,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = rad.frequency,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 8.sp
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
