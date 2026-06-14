package com.example.ui.screens

import android.widget.VideoView
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.data.model.EPGProgram
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FullscreenPlayerScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val miniEpgFocusRequester = remember { FocusRequester() }
    val upcomingFocusRequester = remember { FocusRequester() }
    val quickChannelGridFocusRequester = remember { FocusRequester() }
    val quickActionsFocusRequester = remember { FocusRequester() }
    val signalQualityFocusRequester = remember { FocusRequester() }
    
    // Panel expansion states
    var showMiniEpg by remember { mutableStateOf(false) }
    var showInfoPanel by remember { mutableStateOf(false) }
    var showUpcomingPanel by remember { mutableStateOf(false) }
    var showQuickChannelGrid by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }
    var showSignalQualityPanel by remember { mutableStateOf(false) }

    // Panel Expansion Focused Auto-Leaping trigger logic
    LaunchedEffect(showSignalQualityPanel) {
        if (showSignalQualityPanel) {
            try {
                delay(60)
                signalQualityFocusRequester.requestFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(showMiniEpg) {
        if (showMiniEpg) {
            try {
                delay(60)
                miniEpgFocusRequester.requestFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(showUpcomingPanel) {
        if (showUpcomingPanel) {
            try {
                delay(60)
                upcomingFocusRequester.requestFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(showQuickChannelGrid) {
        if (showQuickChannelGrid) {
            try {
                delay(60)
                quickChannelGridFocusRequester.requestFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(showQuickActions) {
        if (showQuickActions) {
            try {
                delay(60)
                quickActionsFocusRequester.requestFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // HUD overlay (touch-accessible options for phone)
    var showTouchHUD by remember { mutableStateOf(false) }

    // Video aspect ratio mode: "Stretch", "16:9", "4:3", "Zoom"
    var aspectRatioMode by remember { mutableStateOf("Stretch") }
    
    // Quality simulated configuration
    var selectedQuality by remember { mutableStateOf(viewModel.streamingQuality) }
    var selectedDecoder by remember { mutableStateOf(viewModel.playerDecoder) }

    // Channel stability and buffer real-time monitoring states
    var simulatedBuffer by remember { mutableStateOf(1.8f) }
    var simulatedSignal by remember { mutableStateOf(97) }
    var simulatedStability by remember { mutableStateOf(99.8f) }
    var isReconnectingSignal by remember { mutableStateOf(false) }

    // Stream URL and current channel
    val currentChannel = viewModel.selectedChannel
    val isPlaying = viewModel.isTvPlaying

    LaunchedEffect(isPlaying, isReconnectingSignal) {
        if (isReconnectingSignal) {
            simulatedBuffer = 0.0f
            simulatedSignal = 0
            simulatedStability = 0.0f
            kotlinx.coroutines.delay(1200) // Simulated connection and buffer reload duration
            isReconnectingSignal = false
        }
        while (!isReconnectingSignal) {
            kotlinx.coroutines.delay(1500)
            // Gently fluctuate values to feel realistic and responsive
            simulatedBuffer = (2.2f + Math.sin(System.currentTimeMillis() / 6000.0).toFloat() * 0.6f).coerceIn(1.2f, 4.5f)
            simulatedSignal = (94 + (System.currentTimeMillis() % 6).toInt()).coerceIn(0, 100)
            simulatedStability = (99.4f + (System.currentTimeMillis() % 6) / 10f).coerceIn(0.0f, 100.0f)
        }
    }

    // Collect all channels for quick selection lists
    val allChannels by viewModel.allChannels.collectAsState()
    val isMobile = LocalConfiguration.current.screenWidthDp < 580

    // Time-keeping sync variables for EPG calculations
    var currentTimeDecimal by remember { mutableStateOf(13.4f) }
    var currentTimeString by remember { mutableStateOf("01:24 PM") }

    LaunchedEffect(Unit) {
        while (true) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            
            currentTimeDecimal = hour.toFloat() + (minute.toFloat() / 60.0f) + (second.toFloat() / 3600.0f)
            
            val displayHour = calendar.get(Calendar.HOUR)
            val actualHour = if (displayHour == 0) 12 else displayHour
            val amPmString = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            currentTimeString = String.format("%d:%02d %s", actualHour, minute, amPmString)
            
            delay(1000)
        }
    }

    // Active program information calculated dynamically
    val currentProgram = remember(currentChannel, currentTimeDecimal) {
        val programs = viewModel.getProgramsForChannel(currentChannel.id)
        programs.find { it.isActiveAt(currentTimeDecimal) } ?: viewModel.selectedEpgProgram
    }

    // User interaction sequence token to drive auto-hide panel behavior (5 seconds timeout)
    var userInteractionCounter by remember { mutableStateOf(0) }

    // Reset timer coroutine
    LaunchedEffect(userInteractionCounter, showMiniEpg, showInfoPanel, showUpcomingPanel, showQuickChannelGrid, showQuickActions, showTouchHUD, showSignalQualityPanel) {
        if (showMiniEpg || showInfoPanel || showUpcomingPanel || showQuickChannelGrid || showQuickActions || showTouchHUD || showSignalQualityPanel) {
            delay(8000) // Auto-hide after 8 seconds of complete stillness to allow viewing signal details
            showMiniEpg = false
            showInfoPanel = false
            showUpcomingPanel = false
            showQuickChannelGrid = false
            showQuickActions = false
            showTouchHUD = false
            showSignalQualityPanel = false
        }
    }

    // Reclaim focus to the main video player container when all panels are closed to prevent focus from getting stuck
    val anyPanelOpen = showMiniEpg || showUpcomingPanel || showQuickChannelGrid || showQuickActions || showInfoPanel || showTouchHUD || showSignalQualityPanel
    LaunchedEffect(anyPanelOpen) {
        if (!anyPanelOpen) {
            try {
                delay(120) // Let animations settle slightly
                focusRequester.requestFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Capture interaction to reset timeout
    fun pinInteraction() {
        userInteractionCounter++
    }

    // Helper to safely dismiss all overlay panels
    fun dismissAllPanels(): Boolean {
        var dismissedSomething = false
        if (showMiniEpg) { showMiniEpg = false; dismissedSomething = true }
        if (showInfoPanel) { showInfoPanel = false; dismissedSomething = true }
        if (showUpcomingPanel) { showUpcomingPanel = false; dismissedSomething = true }
        if (showQuickChannelGrid) { showQuickChannelGrid = false; dismissedSomething = true }
        if (showQuickActions) { showQuickActions = false; dismissedSomething = true }
        if (showTouchHUD) { showTouchHUD = false; dismissedSomething = true }
        if (showSignalQualityPanel) { showSignalQualityPanel = false; dismissedSomething = true }
        return dismissedSomething
    }

    val isAnyPanelOpen = showMiniEpg || showInfoPanel || showUpcomingPanel || showQuickChannelGrid || showQuickActions || showSignalQualityPanel
    BackHandler(enabled = viewModel.isFullscreenPlayerActive) {
        if (isAnyPanelOpen) {
            dismissAllPanels()
        } else {
            viewModel.isFullscreenPlayerActive = false
        }
    }

    // Helper to change channel (Up is previous, Down is next)
    fun changeChannel(next: Boolean) {
        if (allChannels.isEmpty()) return
        val currentIndex = allChannels.indexOfFirst { it.id == currentChannel.id }
        if (currentIndex == -1) {
            viewModel.selectChannel(allChannels.first())
        } else {
            val targetIndex = if (next) {
                (currentIndex + 1) % allChannels.size
            } else {
                (currentIndex - 1 + allChannels.size) % allChannels.size
            }
            viewModel.selectChannel(allChannels[targetIndex])
        }
        // Auto-show the info panel with new channel details and trigger timer refresh
        showInfoPanel = true
        pinInteraction()
    }

    // Request initial focus on load so Dpad captures key inputs immediately
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    pinInteraction()
                    val heavyPanelOpen = showMiniEpg || showUpcomingPanel || showQuickChannelGrid || showQuickActions || showSignalQualityPanel
                    when (keyEvent.key) {
                        Key.DirectionLeft -> {
                            if (!heavyPanelOpen) {
                                showSignalQualityPanel = true
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionRight -> {
                            if (!heavyPanelOpen) {
                                showUpcomingPanel = true
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionUp -> {
                            if (!heavyPanelOpen) {
                                changeChannel(next = false) // Previous channel
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionDown -> {
                            if (!heavyPanelOpen) {
                                changeChannel(next = true) // Next channel
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionCenter, Key.Enter -> {
                            if (!heavyPanelOpen) {
                                showInfoPanel = !showInfoPanel
                                true
                            } else {
                                false
                            }
                        }
                        Key.Back, Key.Escape -> {
                            if (heavyPanelOpen) {
                                dismissAllPanels()
                                true
                            } else {
                                // Default option: Return to prior screens instantly without getting stuck/frozen
                                viewModel.isFullscreenPlayerActive = false
                                true
                            }
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pinInteraction()
                // On Mobile or direct tap, toggle HUD so overlay triggers can be tapped
                showTouchHUD = !showTouchHUD
                if (!showTouchHUD) {
                    dismissAllPanels()
                }
            }
    ) {

        // ==========================================
        // 1. REPRODUCTOR DE VIDEO COMPLETO (ExoPlayer/VideoView base compatible)
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { testTag = "fullscreen_video_frame" },
            contentAlignment = Alignment.Center
        ) {
            val videoModifier = when (aspectRatioMode) {
                "16:9" -> Modifier.aspectRatio(16f / 9f)
                "4:3" -> Modifier.aspectRatio(4f / 3f)
                "Zoom" -> Modifier.fillMaxSize().scale(1.23f)
                else -> Modifier.fillMaxSize() // Stretch / default
            }

            if (isPlaying && currentChannel.id != "no_channel" && viewModel.isFullscreenPlayerActive) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { videoView ->
                        val streamUrl = currentChannel.streamUrl
                        if (videoView.tag != streamUrl && streamUrl.isNotEmpty()) {
                            videoView.tag = streamUrl
                            try {
                                videoView.stopPlayback()
                                videoView.setVideoPath(streamUrl)
                                videoView.setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    try {
                                        mp.setVolume(viewModel.tvVolume, viewModel.tvVolume)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    videoView.start()
                                }
                                videoView.setOnErrorListener { _, _, _ ->
                                    true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onRelease = { videoView ->
                        try {
                            videoView.stopPlayback()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = videoModifier
                )
            } else {
                // High fidelity static cover art when suspended/empty to maintain aesthetic balance
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentProgram.thumbnailUrl,
                        contentDescription = "Cover Art Backdrop",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f))
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = "Suspended",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Reproducción en pausa",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Presiona OK para reanudar el canal",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Elegant top gradient to guarantee action elements eligibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
                .align(Alignment.TopCenter)
        )

        // Close/Escape Button for mobile or direct click convenience
        IconButton(
            onClick = {
                pinInteraction()
                if (dismissAllPanels()) {
                    // Closed some panel, stay fullscreen
                } else {
                    viewModel.isFullscreenPlayerActive = false
                }
            },
            modifier = Modifier
                .padding(20.dp)
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                .align(Alignment.TopStart)
                .semantics { testTag = "fullscreen_exit_button" }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Salir a Guía",
                tint = Color.White
            )
        }

        // ==========================================
        // 2. HUD DE ATAJOS FLOTANTES TÁCTILES (DISEÑO MÓVIL RESPONSIVE)
        // ==========================================
        AnimatedVisibility(
            visible = showTouchHUD && isMobile && !showMiniEpg && !showUpcomingPanel && !showQuickChannelGrid && !showQuickActions,
            enter = fadeIn(animationSpec = tween(220)) + scaleIn(),
            exit = fadeOut(animationSpec = tween(220)) + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signal & Quality Shortcut (Left - Menu / "tres rayas")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { pinInteraction(); showSignalQualityPanel = true; showTouchHUD = false },
                        modifier = Modifier.size(50.dp).background(Color.Black.copy(alpha = 0.75f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Menu, "Señal/Calidad", tint = Color.White)
                    }
                    Text("Señal/Calidad", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }
                
                // Quick channel list Shortcut (Up)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { pinInteraction(); showQuickChannelGrid = true; showTouchHUD = false },
                        modifier = Modifier.size(50.dp).background(Color.Black.copy(alpha = 0.75f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Tv, "Canales", tint = Color.White)
                    }
                    Text("Canales", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }

                // Info Shortcut (Center)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { pinInteraction(); showInfoPanel = !showInfoPanel; showTouchHUD = false },
                        modifier = Modifier.size(58.dp).background(Color(0xFF2196F3), CircleShape).border(1.dp, Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Info, "Info", tint = Color.White)
                    }
                    Text("Detalles", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }

                // Actions Menu Shortcut (Down)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { pinInteraction(); showQuickActions = true; showTouchHUD = false },
                        modifier = Modifier.size(50.dp).background(Color.Black.copy(alpha = 0.75f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, "Ajustes", tint = Color.White)
                    }
                    Text("Acciones", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }

                // Upcoming schedule Shortcut (Right)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { pinInteraction(); showUpcomingPanel = true; showTouchHUD = false },
                        modifier = Modifier.size(50.dp).background(Color.Black.copy(alpha = 0.75f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Schedule, "Próximos", tint = Color.White)
                    }
                    Text("Próximos", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        // ==========================================
        // 3. MINI GUÍA EPG LATERAL (IZQUIERDA - DETALLES RAPIDOS)
        // ==========================================
        AnimatedVisibility(
            visible = showMiniEpg,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(if (isMobile) 260.dp else 340.dp)
                .background(Color.Black.copy(alpha = 0.85f))
                .border(2.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MINI GUÍA EPG",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = { showMiniEpg = false }) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(allChannels) { index, channel ->
                        val isSel = channel.id == currentChannel.id
                        val isFocusTarget = if (allChannels.any { it.id == currentChannel.id }) isSel else index == 0
                        val programs = viewModel.getProgramsForChannel(channel.id)
                        val runningProg = programs.find { it.isActiveAt(currentTimeDecimal) }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f))
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .then(if (isFocusTarget) Modifier.focusRequester(miniEpgFocusRequester) else Modifier)
                                .onFocusChanged { if (it.isFocused) pinInteraction() }
                                .clickable {
                                    pinInteraction()
                                    viewModel.selectChannel(channel)
                                    showMiniEpg = false
                                }
                                .tvFocusEffect(
                                    shape = RoundedCornerShape(10.dp),
                                    focusedBorderColor = Color.White,
                                    borderWidth = 3.dp,
                                    scaleAmount = 1.03f
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (channel.logoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = channel.logoUrl,
                                    contentDescription = channel.name,
                                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(34.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(channel.name.take(3).uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = channel.name,
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = runningProg?.title ?: "No info",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. PRÓXIMOS PROGRAMAS (DERECHA - EPG DETALLADA)
        // ==========================================
        AnimatedVisibility(
            visible = showUpcomingPanel,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(if (isMobile) 260.dp else 340.dp)
                .background(Color.Black.copy(alpha = 0.85f))
                .border(2.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRÓXIMOS PROGRAMAS",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = { showUpcomingPanel = false }) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                    }
                }

                Text(
                    text = currentChannel.name.uppercase(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val upcomingPrograms = remember(currentChannel) {
                    viewModel.getProgramsForChannel(currentChannel.id)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(upcomingPrograms) { index, program ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (index == 0) Modifier.focusRequester(upcomingFocusRequester) else Modifier)
                                .onFocusChanged { if (it.isFocused) pinInteraction() }
                                .focusable()
                                .tvFocusEffect(
                                    shape = RoundedCornerShape(8.dp),
                                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                                    borderWidth = 2.dp,
                                    scaleAmount = 1.02f
                                )
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = program.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${program.startTime}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = program.description,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. LISTA RÁPIDA DE CANALES (RIBBON SUPERIOR - DPAD UP)
        // ==========================================
        AnimatedVisibility(
            visible = showQuickChannelGrid,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 80.dp) // Below back icon boundary safely
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.82f))
                .border(2.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LISTA RÁPIDA DE CANALES",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = { showQuickChannelGrid = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(allChannels) { index, channel ->
                        val isSelected = channel.id == currentChannel.id
                        val isFocusTarget = if (allChannels.any { it.id == currentChannel.id }) isSelected else index == 0
                        Column(
                            modifier = Modifier
                                .width(96.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .then(if (isFocusTarget) Modifier.focusRequester(quickChannelGridFocusRequester) else Modifier)
                                .onFocusChanged { if (it.isFocused) pinInteraction() }
                                .clickable {
                                    pinInteraction()
                                    viewModel.selectChannel(channel)
                                    showQuickChannelGrid = false
                                }
                                .tvFocusEffect(
                                    shape = RoundedCornerShape(10.dp),
                                    focusedBorderColor = Color.White,
                                    borderWidth = 3.dp,
                                    scaleAmount = 1.05f
                                )
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (channel.logoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = channel.logoUrl,
                                    contentDescription = channel.name,
                                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(34.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(channel.name.take(3).uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = channel.name,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. PANEL COMPACTO DE INFORMACIÓN (OK / ENTER)
        // ==========================================
        AnimatedVisibility(
            visible = showInfoPanel,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = if (isMobile) 16.dp else 32.dp,
                    end = if (isMobile) 16.dp else 32.dp,
                    bottom = if (isMobile) 16.dp else 32.dp
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.82f))
                .border(2.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo
                    if (currentChannel.logoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = currentChannel.logoUrl,
                            contentDescription = currentChannel.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentChannel.name.take(3).uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${currentChannel.name} — EN VIVO",
                            color = Color(0xFF4A89FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentProgram.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Resolution and FPS pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Resolution pill
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (selectedQuality == "Auto") "1080p FHD" else selectedQuality.replace(" (FHD)", "").replace(" (HD)", "").replace(" (UHD)", "").replace(" (SD)", ""),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // FPS pill (as requested: "25 fPS al ladito")
                        val currentFps = when {
                            selectedQuality.contains("4K") -> "60 FPS"
                            selectedQuality.contains("1080") -> "50 FPS"
                            selectedQuality.contains("720") -> "25 FPS"
                            else -> "25 FPS"
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00FFD1).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF00FFD1).copy(alpha = 0.30f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentFps,
                                color = Color(0xFF00FFD1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar calculation
                val progress = remember(currentProgram, currentTimeDecimal) {
                    val duration = currentProgram.durationHours
                    if (duration > 0f) {
                        val start = currentProgram.startHourDecimal
                        ((currentTimeDecimal - start) / duration).coerceIn(0f, 1f)
                    } else 0.5f
                }

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentProgram.startTime} - ${currentProgram.endTime}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "CAT: ${currentProgram.category.uppercase()}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentProgram.description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // RESPONSIVE SIGNAL AND QUALITY DASHBOARD REMOVED FOR CLEANLINESS
                if (false) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                    // Title for context
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ESTADO DE CONEXIÓN Y RESOLUCIÓN HD",
                            color = Color(0xFF00FFD1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.75.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(if (isReconnectingSignal) Color.Red.copy(alpha = 0.15f) else Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isReconnectingSignal) "RECONECTANDO" else "SEÑAL ACTIVA",
                                color = if (isReconnectingSignal) Color.Red else Color(0xFF4CAF50),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isMobile) {
                        // Mobile / Compact stacking layout to avoid squeezing
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1st: Quality Row with inline Restart
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val qualitySteps = listOf("480p (SD)", "720p (HD)", "1080p (FHD)", "4K (UHD)")
                                val currentIdx = qualitySteps.indexOf(selectedQuality).coerceAtLeast(0)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Bajar HD Quality (-)
                                    IconButton(
                                        onClick = {
                                            pinInteraction()
                                            if (currentIdx > 0) {
                                                val newQuality = qualitySteps[currentIdx - 1]
                                                selectedQuality = newQuality
                                                viewModel.updateStreamQuality(newQuality)
                                            }
                                        },
                                        enabled = currentIdx > 0 && !isReconnectingSignal,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(if (currentIdx > 0 && !isReconnectingSignal) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Default.Remove, "Bajar Calidad", tint = if (currentIdx > 0) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
                                    }

                                    // The Middle button of Quality (Clickable state)
                                    Button(
                                        onClick = {
                                            pinInteraction()
                                            if (!isReconnectingSignal) {
                                                val nextIdx = (currentIdx + 1) % qualitySteps.size
                                                val newQuality = qualitySteps[nextIdx]
                                                selectedQuality = newQuality
                                                viewModel.updateStreamQuality(newQuality)
                                            }
                                        },
                                        enabled = !isReconnectingSignal,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = selectedQuality.replace(" (FHD)", "").replace(" (HD)", "").replace(" (UHD)", "").replace(" (SD)", ""),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    // Subir HD Quality (+)
                                    IconButton(
                                        onClick = {
                                            pinInteraction()
                                            if (currentIdx < qualitySteps.lastIndex) {
                                                val newQuality = qualitySteps[currentIdx + 1]
                                                selectedQuality = newQuality
                                                viewModel.updateStreamQuality(newQuality)
                                            }
                                        },
                                        enabled = currentIdx < qualitySteps.lastIndex && !isReconnectingSignal,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(if (currentIdx < qualitySteps.lastIndex && !isReconnectingSignal) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Default.Add, "Subir Calidad", tint = if (currentIdx < qualitySteps.lastIndex) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp))
                                    }
                                }

                                // Reset Stream Button
                                Button(
                                    onClick = {
                                        pinInteraction()
                                        isReconnectingSignal = true
                                        viewModel.restartTvPlay()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFD1)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Refresh, "Reiniciar", tint = Color.Black, modifier = Modifier.size(13.dp))
                                        Text("REINICIAR", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 2nd: Signals list (Buffer, Signal, Stability)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Buffer
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Buffer", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                                        Text(text = if (isReconnectingSignal) "0s" else String.format("%.1fs", simulatedBuffer), color = Color(0xFF00D1FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (simulatedBuffer / 5.0f).coerceIn(0f, 1f) },
                                        color = Color(0xFF00D1FF),
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp))
                                    )
                                }

                                // Signal
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Señal", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                                        Text(text = if (isReconnectingSignal) "0%" else "$simulatedSignal%", color = Color(0xFF00FFD1), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (simulatedSignal / 100f).coerceIn(0f, 1f) },
                                        color = Color(0xFF00FFD1),
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp))
                                    )
                                }

                                // Stability
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Estable", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                                        Text(text = if (isReconnectingSignal) "0%" else String.format("%.0f%%", simulatedStability), color = Color(0xFFFFD54F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (simulatedStability / 100f).coerceIn(0f, 1f) },
                                        color = Color(0xFFFFD54F),
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp))
                                    )
                                }
                            }
                        }
                    } else {
                        // Desktop / TV side-by-side elegant layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quality row
                            Row(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val qualitySteps = listOf("480p (SD)", "720p (HD)", "1080p (FHD)", "4K (UHD)")
                                val currentIdx = qualitySteps.indexOf(selectedQuality).coerceAtLeast(0)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            pinInteraction()
                                            if (currentIdx > 0) {
                                                val newQuality = qualitySteps[currentIdx - 1]
                                                selectedQuality = newQuality
                                                viewModel.updateStreamQuality(newQuality)
                                            }
                                        },
                                        enabled = currentIdx > 0 && !isReconnectingSignal,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(if (currentIdx > 0 && !isReconnectingSignal) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Default.Remove, "Bajar Calidad", tint = if (currentIdx > 0) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = {
                                            pinInteraction()
                                            if (!isReconnectingSignal) {
                                                val nextIdx = (currentIdx + 1) % qualitySteps.size
                                                val newQuality = qualitySteps[nextIdx]
                                                selectedQuality = newQuality
                                                viewModel.updateStreamQuality(newQuality)
                                            }
                                        },
                                        enabled = !isReconnectingSignal,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = selectedQuality.replace(" (FHD)", "").replace(" (HD)", "").replace(" (UHD)", "").replace(" (SD)", ""),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            pinInteraction()
                                            if (currentIdx < qualitySteps.lastIndex) {
                                                val newQuality = qualitySteps[currentIdx + 1]
                                                selectedQuality = newQuality
                                                viewModel.updateStreamQuality(newQuality)
                                            }
                                        },
                                        enabled = currentIdx < qualitySteps.lastIndex && !isReconnectingSignal,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(if (currentIdx < qualitySteps.lastIndex && !isReconnectingSignal) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Default.Add, "Subir Calidad", tint = if (currentIdx < qualitySteps.lastIndex) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                                    }
                                }

                                Button(
                                    onClick = {
                                        pinInteraction()
                                        isReconnectingSignal = true
                                        viewModel.restartTvPlay()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFD1)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).tvFocusEffect(scaleAmount = 1.05f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Refresh, "Reiniciar", tint = Color.Black, modifier = Modifier.size(14.dp))
                                        Text("REINICIAR", color = Color.Black, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Signal monitors row
                            Row(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Buffer Monitor
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Icon(Icons.Default.Timer, "Buffer", tint = Color(0xFF00D1FF), modifier = Modifier.size(11.dp))
                                            Text("Buffer", color = Color.White.copy(alpha = 0.7f), fontSize = 9.5.sp)
                                        }
                                        Text(text = if (isReconnectingSignal) "0.0s" else String.format("%.1fs", simulatedBuffer), color = Color(0xFF00D1FF), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (simulatedBuffer / 5.0f).coerceIn(0f, 1f) },
                                        color = Color(0xFF00D1FF),
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 3.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp))
                                    )
                                }

                                // Signal strength
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Icon(Icons.Default.Wifi, "Señal", tint = Color(0xFF00FFD1), modifier = Modifier.size(11.dp))
                                            Text("Señal", color = Color.White.copy(alpha = 0.7f), fontSize = 9.5.sp)
                                        }
                                        Text(text = if (isReconnectingSignal) "Buscando" else "$simulatedSignal%", color = Color(0xFF00FFD1), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (simulatedSignal / 100f).coerceIn(0f, 1f) },
                                        color = Color(0xFF00FFD1),
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 3.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp))
                                    )
                                }

                                // Stability
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Icon(Icons.Default.SignalCellularAlt, "Estabilidad", tint = Color(0xFFFFD54F), modifier = Modifier.size(11.dp))
                                            Text("Estable", color = Color.White.copy(alpha = 0.7f), fontSize = 9.5.sp)
                                        }
                                        Text(text = if (isReconnectingSignal) "0.0%" else String.format("%.0f%%", simulatedStability), color = Color(0xFFFFD54F), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (simulatedStability / 100f).coerceIn(0f, 1f) },
                                        color = Color(0xFFFFD54F),
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 3.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp))
                                    )
                                }
                            }
                        }
                    }
                }
                } // closes if (false)
            }
        }

        // ==========================================
        // 7. MENÚ DE ACCIONES RÁPIDAS (ABAJO - DPAD DOWN)
        // ==========================================
        AnimatedVisibility(
            visible = showQuickActions,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = if (isMobile) 16.dp else 40.dp,
                    end = if (isMobile) 16.dp else 40.dp,
                    bottom = if (isMobile) 16.dp else 40.dp
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.88f))
                .border(2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Settings, "Configuraciones", tint = Color(0xFF00FFD1), modifier = Modifier.size(16.dp))
                        Text(
                            text = "ACCIONES RÁPIDAS Y CONFIGURACIÓN DE SEÑAL",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = { showQuickActions = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                val isFav = remember(currentChannel) { mutableStateOf(false) }
                LaunchedEffect(currentChannel) {
                    isFav.value = viewModel.isChannelFavorite(currentChannel.id)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. ADD / REMOVE FAVORITES
                    Button(
                        onClick = {
                            pinInteraction()
                            viewModel.toggleChannelFavorite(currentChannel.id)
                            isFav.value = !isFav.value
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFav.value) Color(0xFFFF4081) else Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(quickActionsFocusRequester)
                            .onFocusChanged { if (it.isFocused) pinInteraction() }
                            .tvFocusEffect(scaleAmount = 1.05f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isFav.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorito", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFav.value) "En Favoritos" else "Llevar a Faved", color = Color.White, fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    // 2. TOGGLE ASPECT RATIO
                    Button(
                        onClick = {
                            pinInteraction()
                            aspectRatioMode = when (aspectRatioMode) {
                                "Stretch" -> "16:9"
                                "16:9" -> "4:3"
                                "4:3" -> "Zoom"
                                else -> "Stretch"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { if (it.isFocused) pinInteraction() }
                            .tvFocusEffect(scaleAmount = 1.05f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AspectRatio, "Aspect Ratio", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Relación: $aspectRatioMode", color = Color.White, fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    // 3. TOGGLE PLAYER DECODER
                    Button(
                        onClick = {
                            pinInteraction()
                            selectedDecoder = when (selectedDecoder) {
                                "Hardware (HW+)" -> "Software (SW)"
                                else -> "Hardware (HW+)"
                            }
                            viewModel.updateDecoder(selectedDecoder)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { if (it.isFocused) pinInteraction() }
                            .tvFocusEffect(scaleAmount = 1.05f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, "Decoder", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Formato: $selectedDecoder", color = Color.White, fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    // 4. EXIT PLAYER BUTTON
                    Button(
                        onClick = {
                            pinInteraction()
                            viewModel.isFullscreenPlayerActive = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .weight(1.5f)
                            .onFocusChanged { if (it.isFocused) pinInteraction() }
                            .tvFocusEffect(scaleAmount = 1.05f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Launch, "Exit", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abrir Guía completa", color = Color.White, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                // Divider line
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                // 2. SUBIR/BAJAR CALIDAD HD SECTION (As requested: "subir la calidad bajar la calidad del HD")
                Column {
                    Text(
                        text = "CONTROL DE CALIDAD DE TRANSMISIÓN (HD / SD)",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val qualitySteps = listOf("480p (SD)", "720p (HD)", "1080p (FHD)", "4K (UHD)")
                        val currentIdx = qualitySteps.indexOf(selectedQuality).coerceAtLeast(0)

                        // Bajar Calidad button (-)
                        IconButton(
                            onClick = {
                                pinInteraction()
                                if (currentIdx > 0) {
                                    val newQuality = qualitySteps[currentIdx - 1]
                                    selectedQuality = newQuality
                                    viewModel.updateStreamQuality(newQuality)
                                }
                            },
                            enabled = currentIdx > 0 && !isReconnectingSignal,
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (currentIdx > 0 && !isReconnectingSignal) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.Remove, "Bajar Calidad", tint = if (currentIdx > 0) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                        }

                        // Display Current Quality Highlight
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val dotColor = if (selectedQuality.contains("4K") || selectedQuality.contains("1080")) Color(0xFF00FFD1) else Color(0xFFFFA726)
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Text(
                                    text = selectedQuality.uppercase(),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            val bandwidthInfo = when (selectedQuality) {
                                "4K (UHD)" -> "Requiere ~25 Mbps. Latencia óptima y ultra definición."
                                "1080p (FHD)" -> "Requiere ~10 Mbps. Balance ideal fluido de alta definición."
                                "720p (HD)" -> "Requiere ~5 Mbps. Modo estándar HD de menor consumo."
                                else -> "Requiere ~2 Mbps. Ahorro extremo de buffer estabilidad constante."
                            }
                            Text(
                                text = bandwidthInfo,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Subir Calidad button (+)
                        IconButton(
                            onClick = {
                                pinInteraction()
                                if (currentIdx < qualitySteps.lastIndex) {
                                    val newQuality = qualitySteps[currentIdx + 1]
                                    selectedQuality = newQuality
                                    viewModel.updateStreamQuality(newQuality)
                                }
                            },
                            enabled = currentIdx < qualitySteps.lastIndex && !isReconnectingSignal,
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (currentIdx < qualitySteps.lastIndex && !isReconnectingSignal) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.Add, "Subir Calidad", tint = if (currentIdx < qualitySteps.lastIndex) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // 3. BUFFER AND SIGNAL MONITORING WITH RESTART
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Column: Monitor Dashboard values
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ESTADO DE SEÑAL Y BUFFER EN TIEMPO REAL",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        // Progress representation of the buffer latency
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Timer, "Buffer Size", tint = Color(0xFF00D1FF), modifier = Modifier.size(15.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Buffer Integrado", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                    Text(
                                        text = if (isReconnectingSignal) "0.0s (Cargando...)" else String.format("%.1fs (Estable)", simulatedBuffer),
                                        color = if (simulatedBuffer > 1.5f && !isReconnectingSignal) Color(0xFF00D1FF) else Color(0xFFFF4081),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (simulatedBuffer / 5.0f).coerceIn(0f, 1f) },
                                    color = Color(0xFF00D1FF),
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }

                        // Signal power meter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Wifi, "Network Signal", tint = Color(0xFF00FFD1), modifier = Modifier.size(15.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Señal de Transmisión", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                    Text(
                                        text = if (isReconnectingSignal) "Buscando..." else "$simulatedSignal% (-${100 - simulatedSignal} dBm)",
                                        color = if (simulatedSignal > 80 && !isReconnectingSignal) Color(0xFF00FFD1) else Color(0xFFFFC107),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (simulatedSignal / 100f).coerceIn(0f, 1f) },
                                    color = Color(0xFF00FFD1),
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }

                        // stability index representation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.SignalCellularAlt, "Channel Stability status", tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
                                Text("Estabilidad Canal:", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isReconnectingSignal) Color.Red else Color(0xFF4CAF50))
                                )
                                Text(
                                    text = if (isReconnectingSignal) "0.0% (Reconectando)" else String.format("%.1f%%", simulatedStability),
                                    color = if (isReconnectingSignal) Color.Red else Color(0xFF4CAF50),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Right Column: Restart Button
                    Button(
                        onClick = {
                            pinInteraction()
                            isReconnectingSignal = true
                            viewModel.restartTvPlay()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFD1)),
                        modifier = Modifier
                            .weight(0.7f)
                            .height(130.dp)
                            .align(Alignment.CenterVertically)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .tvFocusEffect(scaleAmount = 1.05f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reiniciar Canal",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "REINICIAR CANAL",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Recargar buffer",
                                color = Color.Black.copy(alpha = 0.7f),
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 8. PANEL DE ESTADO DE SEÑAL Y CALIDAD DE TRANSMISIÓN (TRES RAYAS / DPAD LEFT)
        // ==========================================
        AnimatedVisibility(
            visible = showSignalQualityPanel,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = if (isMobile) 16.dp else 40.dp,
                    end = if (isMobile) 16.dp else 40.dp,
                    bottom = if (isMobile) 16.dp else 40.dp
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.92f))
                .border(2.dp, Color(0xFF00FFD1).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header (Icon, Title, Close Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Menu, "Señal y Calidad", tint = Color(0xFF00FFD1), modifier = Modifier.size(18.dp))
                        Text(
                            text = "ESTADO DE CONEXIÓN Y RESOLUCIÓN HD",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.75.sp
                        )
                    }
                    IconButton(
                        onClick = { showSignalQualityPanel = false }, 
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Interactive HD Controls (SD to UHD)
                    Column(
                        modifier = Modifier.weight(1.3f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ajustar resolución de transmisión:",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val qualitySteps = listOf("480p (SD)", "720p (HD)", "1080p (FHD)", "4K (UHD)")
                            val currentIdx = qualitySteps.indexOf(selectedQuality).coerceAtLeast(0)

                            val canGoMinus = currentIdx > 0 && !isReconnectingSignal
                            val canGoPlus = currentIdx < qualitySteps.lastIndex && !isReconnectingSignal

                            // Bajar Quality Button
                            IconButton(
                                onClick = {
                                    pinInteraction()
                                    if (currentIdx > 0) {
                                        val newQuality = qualitySteps[currentIdx - 1]
                                        selectedQuality = newQuality
                                        viewModel.updateStreamQuality(newQuality)
                                    }
                                },
                                enabled = canGoMinus,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (canGoMinus) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                                    .focusRequester(signalQualityFocusRequester) // AUTO-FOCUS TARGET!
                                    .onFocusChanged { if (it.isFocused) pinInteraction() }
                                    .tvFocusEffect(scaleAmount = 1.05f)
                            ) {
                                Icon(Icons.Default.Remove, "Bajar Calidad", tint = if (canGoMinus) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                            }

                            // Current Quality Box
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedQuality,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Subir Quality Button
                            IconButton(
                                onClick = {
                                    pinInteraction()
                                    if (currentIdx < qualitySteps.lastIndex) {
                                        val newQuality = qualitySteps[currentIdx + 1]
                                        selectedQuality = newQuality
                                        viewModel.updateStreamQuality(newQuality)
                                    }
                                },
                                enabled = canGoPlus,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (canGoPlus) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                                    .onFocusChanged { if (it.isFocused) pinInteraction() }
                                    .tvFocusEffect(scaleAmount = 1.05f)
                            ) {
                                Icon(Icons.Default.Add, "Subir Calidad", tint = if (canGoPlus) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                            }
                        }

                        // Informative message
                        val helpText = when (selectedQuality) {
                            "4K (UHD)" -> "Requiere ~25 Mbps. Latencia óptima y ultra definición (60 FPS)."
                            "1080p (FHD)" -> "Requiere ~10 Mbps. Balance ideal fluido de alta definición (50 FPS)."
                            "720p (HD)" -> "Requiere ~5 Mbps. Modo estándar HD de menor consumo (25 FPS)."
                            else -> "Modo básico SD para redes inestables o móviles (25 FPS)."
                        }
                        Text(
                            text = helpText,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Middle Column: Live Signals (Buffer, Signal Strength, Stability)
                    Column(
                        modifier = Modifier.weight(1.3f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Buffer Size
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Timer, "Buffer", tint = Color(0xFF00D1FF), modifier = Modifier.size(12.dp))
                                    Text("Buffer:", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                }
                                Text(text = if (isReconnectingSignal) "0.0s" else String.format("%.1fs", simulatedBuffer), color = Color(0xFF00D1FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { (simulatedBuffer / 5.0f).coerceIn(0f, 1f) },
                                color = Color(0xFF00D1FF),
                                trackColor = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                            )
                        }

                        // Wi-Fi / Connection Signal Strength
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Wifi, "Señal", tint = Color(0xFF00FFD1), modifier = Modifier.size(12.dp))
                                    Text("Señal Wifi:", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                }
                                Text(text = if (isReconnectingSignal) "0%" else "$simulatedSignal%", color = Color(0xFF00FFD1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { (simulatedSignal / 100f).coerceIn(0f, 1f) },
                                color = Color(0xFF00FFD1),
                                trackColor = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                            )
                        }

                        // Channel Stability Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.SignalCellularAlt, "Estabilidad", tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                                Text("Estabilidad:", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                            Text(
                                text = if (isReconnectingSignal) "0.0%" else String.format("%.1f%%", simulatedStability),
                                color = if (isReconnectingSignal) Color.Red else Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Right Column: Restart Buffer / Reiniciar Canal (Fully focusable & TV-Navigable!)
                    Button(
                        onClick = {
                            pinInteraction()
                            isReconnectingSignal = true
                            viewModel.restartTvPlay()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFD1)),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(90.dp)
                            .align(Alignment.CenterVertically)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .focusable() // Guarantee focus capability on TV!
                            .onFocusChanged { if (it.isFocused) pinInteraction() }
                            .tvFocusEffect(scaleAmount = 1.05f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(6.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reiniciar Canal",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "REINICIAR CANAL",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Recargar buffer",
                                color = Color.Black.copy(alpha = 0.7f),
                                fontSize = 7.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
