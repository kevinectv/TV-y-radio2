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

    // Android MediaPlayer streaming engine
    val context = LocalContext.current
    var isPrepared by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    val mediaPlayer = remember { android.media.MediaPlayer() }

    // Prepare and load streamUrl reactively
    LaunchedEffect(station.streamUrl) {
        playbackError = null
        isPrepared = false
        isBuffering = true
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(station.streamUrl)
            mediaPlayer.setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer.setOnPreparedListener { mp ->
                isPrepared = true
                isBuffering = false
                val vol = viewModel.radioVolume
                mp.setVolume(vol, vol)
                if (viewModel.isRadioPlaying) {
                    mp.start()
                }
            }
            mediaPlayer.setOnErrorListener { _, what, extra ->
                isPrepared = false
                isBuffering = false
                playbackError = "Error de sintonización ($what, $extra)"
                false
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            isPrepared = false
            isBuffering = false
            playbackError = e.localizedMessage ?: "No se pudo sintonizar la señal."
        }
    }

    // Connect ViewModel's play status reactively
    LaunchedEffect(viewModel.isRadioPlaying, isPrepared) {
        try {
            if (isPrepared) {
                if (viewModel.isRadioPlaying) {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                    }
                } else {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Connect ViewModel's volume reactively
    LaunchedEffect(viewModel.radioVolume) {
        try {
            val vol = viewModel.radioVolume
            mediaPlayer.setVolume(vol, vol)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Free MediaPlayer resources when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
            .background(Color.Transparent)
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
        val isWide = maxWidth >= 650.dp

        // Dynamic Background blurred ambient light matches current station theme color
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(baseColor.copy(alpha = 0.18f), Color.Transparent),
                        radius = minOf(maxWidth, maxHeight).value * 1.5f
                    )
                )
        )

        if (isWide) {
            // HIGH-FIDELITY HORIZONTAL SPLIT LAYOUT FOR TV / WIDESCREEN (Perfect fit, no vertical overflow)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT COLUMN: Visualizer, Logo & Info (Weight 4.2f)
                Column(
                    modifier = Modifier
                        .weight(4.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo & Mini Visualizer Box
                    Box(
                        contentAlignment = Alignment.BottomCenter,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(2.5.dp, baseColor.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = station.logoUrl,
                            contentDescription = station.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Bottom panel with Genre name and frequency
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(Color.Black.copy(alpha = 0.75f))
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
                                        val barVal = rememberInfiniteTransition(label = "wave_mini_tv").animateFloat(
                                            initialValue = 2f,
                                            targetValue = 14f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(200 + (idx * 60), easing = EaseInOutSine),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "miniv_tv_$idx"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(2.5.dp)
                                                .height(barVal.value.dp)
                                                .background(baseColor)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Station Name, Genre & Favorite
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = station.name,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.toggleRadioFavorite(station.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) Color.Red else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = station.genre.uppercase(),
                        color = baseColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Audio connection feedback state
                    if (isBuffering) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = baseColor,
                                strokeWidth = 1.5.dp
                            )
                            Text(
                                text = "Sintonizando señal...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (playbackError != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(Color(0xFFE57373).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Señal sin audio - Conexión inestable",
                                color = Color(0xFFEF5350),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(baseColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (viewModel.isRadioPlaying) Color(0xFF4CAF50) else Color.Gray, CircleShape)
                            )
                            Text(
                                text = if (viewModel.isRadioPlaying) "Reproduciendo en vivo" else "Pausado",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Equalizer Canvas visualizer
                    Canvas(
                        modifier = Modifier
                            .width(220.dp)
                            .height(30.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val totalBars = waveHeights.size
                        val spacing = 5.dp.toPx()
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

                // RIGHT COLUMN: Media Controls, Live Volume, Available Stations List (Weight 5.8f)
                Column(
                    modifier = Modifier
                        .weight(5.8f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LUMINA LIVE TRANSMISIÓN",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier
                                .background(baseColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(Color.Black, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Content Middle part containing Controls & Volume slider
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Play/Pause / Previous / Next Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.selectPrevRadio() },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                    .tvFocusEffect(shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipPrevious,
                                    contentDescription = "Anterior",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(baseColor, baseColor.copy(alpha = 0.35f)),
                                            radius = 140f
                                        )
                                    )
                                    .clickable { viewModel.toggleRadioPlay() }
                                    .tvFocusEffect(shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isRadioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play/Pausa",
                                    tint = Color.Black,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.selectNextRadio() },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                    .tvFocusEffect(shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Siguiente",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // High fidelity D-pad responsive volume row (fixes tv controls issue)
                        var volumeFocused by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .background(
                                    if (volumeFocused) baseColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f),
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

                    // Bottom horizontal select list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "EMISORAS DISPONIBLES",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
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
                                        .width(140.dp)
                                        .height(58.dp)
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
        } else {
            // Vertical responsive structure (Optimal for mobile phone dimensions)
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

                // Vertical responsive body for phone
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Mobile audio sintonization status feedback
                    if (isBuffering) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                color = baseColor,
                                strokeWidth = 1.2.dp
                            )
                            Text(
                                text = "Sintonizando señal...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (playbackError != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(Color(0xFFE57373).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Señal sin audio",
                                color = Color(0xFFEF5350),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(baseColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(if (viewModel.isRadioPlaying) Color(0xFF4CAF50) else Color.Gray, CircleShape)
                            )
                            Text(
                                text = if (viewModel.isRadioPlaying) "En vivo" else "Pausado",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
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
}
