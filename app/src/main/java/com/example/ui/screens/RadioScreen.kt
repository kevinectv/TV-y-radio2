package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.focusable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RadioStation
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect

@Composable
fun RadioScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val station = viewModel.selectedRadioStation
    val radioStations by viewModel.allRadioStations.collectAsState()
    val focusRequester = remember { FocusRequester() }

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

    // Try requesting focus for global key event listening on TV remotes
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Base glowing theme color of the active station
    val baseColor = remember(station.themeColorHex) {
        try {
            Color(android.graphics.Color.parseColor(station.themeColorHex))
        } catch (e: Exception) {
            Color(0xFF6B4EFE)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.VolumeUp -> {
                            viewModel.setRadioVolumeLevel(viewModel.radioVolume + 0.05f)
                            true
                        }
                        Key.VolumeDown -> {
                            viewModel.setRadioVolumeLevel(viewModel.radioVolume - 0.05f)
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        val isWide = maxWidth >= 580.dp

        // Dynamic Background blurred ambient light matches current station theme color
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(baseColor.copy(alpha = 0.15f), Color.Transparent),
                        radius = 900f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Header Section
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        .background(baseColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE SPECTRA",
                        color = Color.Black,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Adaptive Body Section
            if (isWide) {
                // Dual Column Screen Layout (TV and Landscape Tablet mode)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left Cover Art Column
                    Box(
                        contentAlignment = Alignment.BottomCenter,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(2.dp, baseColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = station.logoUrl,
                            contentDescription = station.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Bottom glass panel displaying genre information
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = station.frequency,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Animated mini visualizer
                            if (viewModel.isRadioPlaying) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(14.dp)
                                ) {
                                    repeat(4) { idx ->
                                        val barVal = rememberInfiniteTransition(label = "wave_mini").animateFloat(
                                            initialValue = 2f,
                                            targetValue = 12f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(200 + (idx * 60), easing = EaseInOutSine),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "miniv_$idx"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(barVal.value.dp)
                                                .background(baseColor)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    // Right station metadata & visualization controls
                    Column(
                        modifier = Modifier.widthIn(max = 320.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = station.genre.uppercase(),
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(baseColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )

                            IconButton(
                                onClick = { viewModel.toggleRadioFavorite(station.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorito",
                                    tint = if (isFavorite) Color.Red else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = station.name,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Transmisión oficial de ${station.name} (${station.frequency}). Sonido digital multiplexado para máxima definición estéreo.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live reactive audio equalizer visualizer bars
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val totalBars = waveHeights.size
                            val spacing = 6.dp.toPx()
                            val barWidth = (canvasWidth - (spacing * (totalBars - 1))) / totalBars

                            for (i in 0 until totalBars) {
                                val h = waveHeights[i].value * canvasHeight
                                drawRoundRect(
                                    color = baseColor,
                                    topLeft = Offset(i * (barWidth + spacing), canvasHeight - h),
                                    size = Size(barWidth, h),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }
                        }
                    }
                }
            } else {
                // Vertical responsive structure (Optimal for mobile phone dimensions)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Mobile cover art
                    Box(
                        contentAlignment = Alignment.BottomCenter,
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, baseColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = station.logoUrl,
                            contentDescription = station.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = station.frequency,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (viewModel.isRadioPlaying) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(12.dp)
                                ) {
                                    repeat(4) { idx ->
                                        val barVal = rememberInfiniteTransition(label = "wave_mini_mobile").animateFloat(
                                            initialValue = 2f,
                                            targetValue = 10f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(180 + (idx * 50), easing = EaseInOutSine),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "miniv_mob_$idx"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(1.5.dp)
                                                .height(barVal.value.dp)
                                                .background(baseColor)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title & info section
                    Text(
                        text = station.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = station.genre.uppercase(),
                            color = baseColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .background(baseColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { viewModel.toggleRadioFavorite(station.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) Color.Red else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Line equalizer visualizer bars for portrait mode
                    Canvas(
                        modifier = Modifier
                            .width(200.dp)
                            .height(24.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val totalBars = waveHeights.size
                        val spacing = 4.dp.toPx()
                        val barWidth = (canvasWidth - (spacing * (totalBars - 1))) / totalBars

                        for (i in 0 until totalBars) {
                            val h = waveHeights[i].value * canvasHeight
                            drawRoundRect(
                                color = baseColor,
                                topLeft = Offset(i * (barWidth + spacing), canvasHeight - h),
                                size = Size(barWidth, h),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }
            }

            // Visualizer Media & Volume Control elements
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Interactive playback skip and play controls Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.selectPrevRadio() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.06f), CircleShape)
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

                    // Large Glowing Play/Pause Circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(baseColor, baseColor.copy(alpha = 0.40f)),
                                    radius = 120f
                                )
                            )
                            .clickable { viewModel.toggleRadioPlay() }
                            .tvFocusEffect(shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isRadioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pausa",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.selectNextRadio() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.06f), CircleShape)
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

                Spacer(modifier = Modifier.height(14.dp))

                // High fidelity D-pad responsive volume row (fixes tv controls issue)
                var volumeFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .background(
                            if (volumeFocused) baseColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (volumeFocused) baseColor else Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .onFocusChanged { volumeFocused = it.isFocused }
                        .focusable()
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.DirectionLeft -> {
                                        viewModel.setRadioVolumeLevel(viewModel.radioVolume - 0.05f)
                                        true
                                    }
                                    Key.DirectionRight -> {
                                        viewModel.setRadioVolumeLevel(viewModel.radioVolume + 0.05f)
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.radioVolume > 0.5f) Icons.Default.VolumeUp else if (viewModel.radioVolume > 0f) Icons.Default.VolumeDown else Icons.Default.VolumeMute,
                        contentDescription = "Volume",
                        tint = if (volumeFocused) baseColor else Color.White,
                        modifier = Modifier.size(16.dp)
                    )

                    Slider(
                        value = viewModel.radioVolume,
                        onValueChange = { viewModel.setRadioVolumeLevel(it) },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = baseColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                            thumbColor = if (volumeFocused) baseColor else Color.White
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
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = "MIS EMISORAS DISPONIBLES",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(radioStations) { rad ->
                        val isCurrent = rad.id == station.id
                        val activeRadColor = try {
                            Color(android.graphics.Color.parseColor(rad.themeColorHex))
                        } catch (e: Exception) {
                            baseColor
                        }

                        Card(
                            modifier = Modifier
                                .width(135.dp)
                                .height(56.dp)
                                .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectRadioStation(rad) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) activeRadColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f)
                            ),
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) activeRadColor else Color.White.copy(alpha = 0.08f)
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
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, activeRadColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
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
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
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
