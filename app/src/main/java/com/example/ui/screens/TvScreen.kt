package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.data.model.EPGProgram
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberScrollState()

    // Base layout sizing definitions for the EPG
    val hourWidth = 220.dp
    val timelineStartDecimal = 8.0f // Timeline starts at 08:00 AM
    val timelineEndDecimal = 24.0f // Timeline ends at 12:00 AM (16 hours)

    // Current synchronized time simulation (e.g. 11:15 AM = 11.25f decimal)
    val simulatedTimeDecimal = 11.25f

    // Selected program to show in the EPG Panel
    val selectedProgram = viewModel.selectedEpgProgram

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- 1. IPTV LIVE PLAYER SIMULATOR SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
        ) {
            // Simulated live feed placeholder or static wallpaper
            AsyncImage(
                model = selectedProgram.thumbnailUrl,
                contentDescription = "Live Channel Feed",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (viewModel.isTvPlaying) 0.55f else 0.25f
            )

            // Live stream lighting scan animation to mimic moving pictures
            if (viewModel.isTvPlaying) {
                val waveAnim = rememberInfiniteTransition(label = "player_scan")
                val offsetX by waveAnim.animateFloat(
                    initialValue = -1000f,
                    targetValue = 2000f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "sweep"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    start = Offset(offsetX, 0f),
                                    end = Offset(offsetX + 300f, size.height)
                                )
                            )
                        }
                )
            }

            // Dark Player Gradient Vignette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Stream Info Panel Overlay (Top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // LOGO
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = viewModel.selectedChannel.logoUrl,
                            contentDescription = viewModel.selectedChannel.name,
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CH ${viewModel.selectedChannel.number}",
                                color = Color(0xFFFF9500),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        Color(0xFFFF9500).copy(alpha = 0.15f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.selectedChannel.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = viewModel.selectedChannel.description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 400.dp)
                        )
                    }
                }

                // Streaming indicators (Format, Bitrate, Quality)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "1080P",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green,
                        modifier = Modifier
                            .background(Color.Green.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = "H.264",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan,
                        modifier = Modifier
                            .background(Color.Cyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = "60FPS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Player Overlay Controls (Bottom bar of player)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Control Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.selectPrevChannel() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Canal Anterior",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Large play pause
                    IconButton(
                        onClick = { viewModel.toggleTvPlay() },
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (viewModel.isTvPlaying) Color(0xFF4A89FF) else Color(0xFFFF3B30),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (viewModel.isTvPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pausa",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.selectNextChannel() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Canal Siguiente",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Live badge
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFF3B30), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (viewModel.isTvPlaying) "LIVE" else "PAUSE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Volume slider simulator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.tvVolume > 0.5f) Icons.Default.VolumeUp else if (viewModel.tvVolume > 0f) Icons.Default.VolumeDown else Icons.Default.VolumeMute,
                        contentDescription = "Volume Indicator",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )

                    Slider(
                        value = viewModel.tvVolume,
                        onValueChange = { viewModel.setTvVolumeLevel(it) },
                        modifier = Modifier.width(80.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF4A89FF)
                        )
                    )

                    Text(
                        text = "${(viewModel.tvVolume * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. ADVANCED EPG HEADER DETAILS PANEL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Program Preview image
                Card(
                    modifier = Modifier
                        .size(100.dp, 64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    AsyncImage(
                        model = selectedProgram.thumbnailUrl,
                        contentDescription = selectedProgram.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedProgram.category,
                            color = Color(0xFF4A89FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .background(
                                    Color(0xFF4A89FF).copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedProgram.startTime} - ${selectedProgram.endTime}  (${selectedProgram.durationHours} hrs)",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = selectedProgram.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = selectedProgram.description,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. PROFESSIONAL TIVIMATE-LIKE EPG GRID ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D101E))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
        ) {
            Column {
                // EPG TIMELINE HEADER ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(Color(0xFF141A30))
                ) {
                    // Placeholder for Channel Column Corner
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF181F38))
                            .drawBehind {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.15f),
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.15f),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GUÍA TV",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Times horizontal row synchronized scrolling
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(listState)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(hourWidth * (timelineEndDecimal - timelineStartDecimal))
                        ) {
                            var currentHour = timelineStartDecimal
                            while (currentHour < timelineEndDecimal) {
                                val displayHourInt = currentHour.toInt()
                                val isAm = displayHourInt < 12 || displayHourInt == 24
                                val hourNum = when {
                                    displayHourInt == 12 -> 12
                                    displayHourInt == 24 -> 12
                                    displayHourInt > 12 -> displayHourInt - 12
                                    else -> displayHourInt
                                }
                                val suffix = if (isAm) "AM" else "PM"
                                val label = String.format("%02d:00 %s", hourNum, suffix)

                                Box(
                                    modifier = Modifier
                                        .width(hourWidth)
                                        .fillMaxHeight()
                                        .drawBehind {
                                            drawLine(
                                                color = Color.White.copy(alpha = 0.08f),
                                                start = Offset(0f, 0f),
                                                end = Offset(0f, size.height),
                                                strokeWidth = 1f
                                            )
                                        }
                                        .padding(start = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                currentHour += 1.0f
                            }
                        }

                        // Playback live red-line pointer synchronized position
                        val lineOffset = (simulatedTimeDecimal - timelineStartDecimal) * hourWidth.value
                        Box(
                            modifier = Modifier
                                .offset(x = lineOffset.dp)
                                .fillMaxHeight()
                                .width(3.dp)
                                .background(Color(0xFFE53935))
                        )
                    }
                }

                // CHANNELS + PROGRAMS MATRIX (SCROLLABLE ROWS)
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.repository.channelsList) { channel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .drawBehind {
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.08f),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1f
                                    )
                                }
                        ) {
                            // Target fixed Channel info (Left anchor)
                            var rowFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF111529))
                                    .tvFocusEffect(
                                        shape = RoundedCornerShape(0.dp),
                                        focusedBorderColor = Color(0xFFFF9500),
                                        scaleAmount = 1.02f
                                    )
                                    .clickable {
                                        viewModel.selectChannel(channel)
                                        // Auto-load the currently running EPG program of that channel
                                        val running = viewModel.repository.programsList.find {
                                            it.channelId == channel.id && it.isActiveAt(simulatedTimeDecimal)
                                        }
                                        if (running != null) {
                                            viewModel.selectEpgProgram(running)
                                        }
                                    }
                                    .drawBehind {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.12f),
                                            start = Offset(size.width, 0f),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 1.5f
                                        )
                                    }
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${channel.number}",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(22.dp)
                                    )

                                    // logo
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = channel.logoUrl,
                                            contentDescription = channel.name,
                                            modifier = Modifier.size(26.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = channel.name,
                                            color = if (viewModel.selectedChannel.id == channel.id) Color(
                                                0xFF4A89FF
                                            ) else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = channel.category,
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 9.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Horizontal scrolling block list for programs (synchronized with first header timeline)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .horizontalScroll(rememberScrollState(listState.value))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(hourWidth * (timelineEndDecimal - timelineStartDecimal))
                                        .align(Alignment.CenterStart)
                                ) {
                                    // Fetch programs for this channel
                                    val channelPrograms = viewModel.repository.programsList.filter { it.channelId == channel.id }

                                    channelPrograms.forEach { program ->
                                        val programWidth = hourWidth * program.durationHours
                                        val isCurrentActive = program.isActiveAt(simulatedTimeDecimal)
                                        val isSelectedInDetails = selectedProgram.id == program.id

                                        // Background coloring
                                        val blockBg = when {
                                            isSelectedInDetails -> Color(0xFFD84B16) // Tivimate Orange selected
                                            isCurrentActive -> Color(0xFF1E284D)     // Blue for running
                                            else -> Color(0xFF161B33)                 // Muted grey for rest
                                        }

                                        val borderStrokeWidth = if (isSelectedInDetails) 2.dp else 1.dp
                                        val borderStrokeColor = if (isSelectedInDetails) Color(0xFFE65100) else Color.White.copy(alpha = 0.05f)

                                        Box(
                                            modifier = Modifier
                                                .width(programWidth)
                                                .fillMaxHeight()
                                                .padding(horizontal = 2.dp, vertical = 3.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(blockBg)
                                                .border(
                                                    width = borderStrokeWidth,
                                                    color = borderStrokeColor,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .tvFocusEffect(
                                                    shape = RoundedCornerShape(8.dp),
                                                    focusedBorderColor = Color(0xFFFF9500),
                                                    scaleAmount = 1.03f
                                                )
                                                .clickable {
                                                    viewModel.selectEpgProgram(program)
                                                    // Clicking program can also select that channel
                                                    val matchedChannel = viewModel.repository.channelsList.find { it.id == program.channelId }
                                                    if (matchedChannel != null) {
                                                        viewModel.selectChannel(matchedChannel)
                                                    }
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = program.title,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    if (isCurrentActive) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(Color.Red, CircleShape)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                    text = "${program.startTime} - ${program.endTime}",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Light
                                                )
                                            }
                                        }
                                    }
                                }

                                // Shared Vertical Red Line overlay inside row matrix too
                                val lineOffset = (simulatedTimeDecimal - timelineStartDecimal) * hourWidth.value
                                Box(
                                    modifier = Modifier
                                        .offset(x = lineOffset.dp)
                                        .fillMaxHeight()
                                        .width(1.5.dp)
                                        .background(Color(0xFFFF3B30).copy(alpha = 0.45f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
