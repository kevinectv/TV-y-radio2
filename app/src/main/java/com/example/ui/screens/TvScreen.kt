package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.data.model.EPGProgram
import com.example.ui.MediaViewModel
import androidx.compose.ui.platform.LocalDensity
import com.example.ui.components.tvFocusEffect
import java.util.Calendar

@Composable
fun TvScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    // Simulated EPG timeline variables
    val hourWidth = 240.dp
    val timelineStartDecimal = 8.0f // Starts at 08:00 AM
    val timelineEndDecimal = 24.0f  // Ends at 12:00 AM (16 hours scale)

    // Current live clock and timeline values (synchronized with standard system clock)
    var currentTimeDecimal by remember { mutableStateOf(13.4f) }
    var currentTimeString by remember { mutableStateOf("01:24 PM") }

    LaunchedEffect(Unit) {
        while (true) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            
            // Decimal representing current high-precision hourly timeline offset
            val actualDecimal = hour.toFloat() + (minute.toFloat() / 60.0f) + (second.toFloat() / 3600.0f)
            
            // Format 12-hour AM/PM matching the top right clock exactly (e.g. "8:29 PM")
            val displayHour = calendar.get(Calendar.HOUR)
            val actualHour = if (displayHour == 0) 12 else displayHour
            val amPmString = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            
            currentTimeString = String.format("%d:%02d %s", actualHour, minute, amPmString)
            currentTimeDecimal = actualDecimal
            
            kotlinx.coroutines.delay(1000)
        }
    }

    // Active program selections
    val selectedProgram = viewModel.selectedEpgProgram

    // Category Filter system
    var selectedCategoryFilter by remember { mutableStateOf("All channels") }
    var dateMenuExpanded by remember { mutableStateOf(false) }
    var catMenuExpanded by remember { mutableStateOf(false) }
    val datesList = listOf("Today", "Tomorrow", "Wednesday", "Thursday")
    var selectedDate by remember { mutableStateOf("Today") }
    var searchQuery by remember { mutableStateOf("") }

    // Synchronized horizontal scroll state for EPG grid
    val horizontalScrollState = rememberScrollState()

    val allChannels by viewModel.allChannels.collectAsState()

    // Filter channels list dynamically based on category selection AND search query
    val filteredChannels = remember(allChannels, selectedCategoryFilter, searchQuery) {
        val catFiltered = if (selectedCategoryFilter == "All channels") {
            allChannels
        } else {
            allChannels.filter {
                it.category.equals(selectedCategoryFilter, ignoreCase = true)
            }
        }
        if (searchQuery.isEmpty()) {
            catFiltered
        } else {
            catFiltered.filter {
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Effect to automatically synchronize focus details when selected channel changes or hour shifts
    LaunchedEffect(viewModel.selectedChannel, currentTimeDecimal) {
        val programs = viewModel.getProgramsForChannel(viewModel.selectedChannel.id)
        val runningProgram = programs.find {
            it.isActiveAt(currentTimeDecimal)
        }
        if (runningProgram != null) {
            viewModel.selectEpgProgram(runningProgram)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04060C)) // Fondo secundario negro / ultra-oscuro
    ) {
        val screenWidth = maxWidth
        val density = LocalDensity.current
        var hasAutoScrolled by remember { mutableStateOf(false) }

        // Auto-scroll to center the timeline indicator on launch
        LaunchedEffect(currentTimeDecimal) {
            if (!hasAutoScrolled && currentTimeDecimal > timelineStartDecimal && currentTimeDecimal < timelineEndDecimal) {
                val lineOffsetVal = (currentTimeDecimal - timelineStartDecimal) * hourWidth.value
                val viewportWidthVal = screenWidth.value - 116f // 115.dp fixed column + 1.dp separator
                val targetScrollDpVal = lineOffsetVal - (viewportWidthVal / 2f)
                if (targetScrollDpVal > 0f) {
                    val targetScrollPx = with(density) { targetScrollDpVal.dp.toPx().toInt() }
                    horizontalScrollState.animateScrollTo(targetScrollPx)
                }
                hasAutoScrolled = true
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==========================================
            // 1. PANEL SUPERIOR INTEGRADO DE REPRODUCCIÓN E INFORMACIÓN
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(265.dp) // Proporcionado para estilo Android TV de alta gama
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF161616), // Fondo grafito elegante
                                Color(0xFF000000)  // Negro obsidian puro
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Datos del programa sobrepuestos a la izquierda
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "PROGRAMACIÓN EN VIVO",
                            color = Color.White, // Color de acento blanco de gran estilo
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // Nombre de canal | Nombre del programa
                        Text(
                            text = if (viewModel.selectedChannel.id != "no_channel") {
                                "${viewModel.selectedChannel.name} | ${selectedProgram.title}"
                            } else {
                                "Ningún Canal Cargado"
                            },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Metadatos formateados idénticamente al mockup
                        val durationMin = (selectedProgram.durationHours * 60).toInt()
                        val metaTimeLabel = if (viewModel.selectedChannel.id != "no_channel") {
                            "08.06.2026 13:24 | ${selectedProgram.category.uppercase()} | $durationMin MIN. | ${selectedProgram.startTime} - ${selectedProgram.endTime}"
                        } else {
                            "IPTV | CARGAR LISTA M3U"
                        }
                        Text(
                            text = metaTimeLabel,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Descripción del programa (hasta 2 líneas para balancear con el reproductor widget)
                        Text(
                            text = if (viewModel.selectedChannel.id != "no_channel") {
                                selectedProgram.description
                            } else {
                                "Por favor, dirígete a la pestaña 'Fuentes' para agregar e importar tu lista M3U."
                            },
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // REPRODUCTOR EN MINIATURA DE 16:9 INTEGRADO A LA DERECHA
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Card(
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .fillMaxHeight()
                                .border(1.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Black)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (viewModel.isTvPlaying && viewModel.selectedChannel.streamUrl.isNotEmpty() && viewModel.selectedChannel.id != "no_channel") {
                                    androidx.compose.ui.viewinterop.AndroidView(
                                        factory = { ctx ->
                                            android.widget.VideoView(ctx).apply {
                                                layoutParams = android.view.ViewGroup.LayoutParams(
                                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                            }
                                        },
                                        update = { videoView ->
                                            val streamUrl = viewModel.selectedChannel.streamUrl
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
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    if (viewModel.selectedChannel.id != "no_channel") {
                                        AsyncImage(
                                            model = selectedProgram.thumbnailUrl,
                                            contentDescription = selectedProgram.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            Color(0xFF262626),
                                                            Color(0xFF030303)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Tv,
                                                    contentDescription = "Sin lista cargada",
                                                    tint = Color.White.copy(alpha = 0.35f),
                                                    modifier = Modifier.size(52.dp)
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "No hay una lista de reproducción activa",
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Importa una lista M3U en la sección de 'Fuentes' para comenzar.",
                                                    color = Color.White.copy(alpha = 0.55f),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
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

            // ==========================================
            // 2. PANEL DE FILTROS Y BÚSQUEDA DEL TIMELINE
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector de fecha (Today dropdown)
                Box {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF333842), RoundedCornerShape(8.dp))
                            .clickable { dateMenuExpanded = true }
                            .tvFocusEffect(
                                shape = RoundedCornerShape(8.dp),
                                focusedBorderColor = Color.White,
                                borderWidth = 3.dp,
                                scaleAmount = 1.05f
                            )
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedDate,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Fecha opciones",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dateMenuExpanded,
                        onDismissRequest = { dateMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF1F2228))
                    ) {
                        datesList.forEach { date ->
                            DropdownMenuItem(
                                text = { Text(text = date, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    selectedDate = date
                                    dateMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Selector de Categoría (All channels dropdown)
                Box {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF333842), RoundedCornerShape(8.dp))
                            .clickable { catMenuExpanded = true }
                            .tvFocusEffect(
                                shape = RoundedCornerShape(8.dp),
                                focusedBorderColor = Color.White,
                                borderWidth = 3.dp,
                                scaleAmount = 1.05f
                            )
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedCategoryFilter,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Canales opciones",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = catMenuExpanded,
                        onDismissRequest = { catMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF1F2228))
                    ) {
                        val categories = remember(allChannels) {
                            listOf("All channels") + allChannels.map { it.category }.distinct().sorted()
                        }
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    selectedCategoryFilter = category
                                    catMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // BÚSQUEDA INTERACTIVA EN TIEMPO REAL
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar canal por nombre...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp) },
                    modifier = Modifier
                        .width(280.dp)
                        .height(38.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = Color(0xFF222222),
                        unfocusedContainerColor = Color(0xFF111111),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // ==========================================
            // 3. TIMELINE HORIZONTAL SUPERIOR DE LA RECUADRICULA
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color(0xFF090F1B)) // Color de la barra de la línea de tiempo
            ) {
                // Espacio fijo para alinearse con la columna de los logos
                Box(
                    modifier = Modifier
                        .width(115.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF080D1A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CANALES",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                // Separador vertical fino para alinear exactamente con la cuadrícula de abajo (1.dp)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                // Barra de horas scrollable
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(hourWidth * (timelineEndDecimal - timelineStartDecimal))
                    ) {
                        var hourCounter = timelineStartDecimal
                        while (hourCounter <= timelineEndDecimal) {
                            val currentHourInt = hourCounter.toInt()
                            val hourString = if (currentHourInt >= 24) "00:00" else String.format("%02d:00", currentHourInt)

                            // Exact position on the horizontal scale for this hour
                            val tickOffset = (hourCounter - timelineStartDecimal) * hourWidth.value

                            Column(
                                modifier = Modifier
                                    .offset(x = (tickOffset - 30).dp)
                                    .width(60.dp)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = hourString,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            hourCounter += 1.0f
                        }

                        // Cápsula blanca indicadora de la hora actual en el timeline (sincronizada y centrada)
                        val currentPointerOffset = (currentTimeDecimal - timelineStartDecimal) * hourWidth.value
                        Box(
                            modifier = Modifier
                                .offset(x = (currentPointerOffset - 38).dp)
                                .width(76.dp)
                                .height(24.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .align(Alignment.CenterStart),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentTimeString,
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Separador delgado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            // ==========================================
            // 4. CUADRÍCULA EPG LAZY 2D OPTIMIZADA (SINFÍN SIN LAG)
            // ==========================================
            if (filteredChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF04060C)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Sin canales",
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron canales",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Importa una lista M3U o cambia tus filtros.",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF04060C))
                ) {
                    items(filteredChannels, key = { it.id }) { channel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        ) {
                            // COLUMNA FIJA DE CANAL (Solo logo alineado)
                            val isChannelSelected = viewModel.selectedChannel.id == channel.id
                            Box(
                                modifier = Modifier
                                    .width(115.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF080D1A))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChannelSelected) Color(0xFF2C313C) else Color(0xFF1E222A))
                                    .border(
                                        width = if (isChannelSelected) 3.dp else 1.dp,
                                        color = if (isChannelSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.selectChannel(channel)
                                        val running = viewModel.getProgramsForChannel(channel.id).find {
                                            it.isActiveAt(currentTimeDecimal)
                                        }
                                        if (running != null) {
                                            viewModel.selectEpgProgram(running)
                                        }
                                    }
                                    .tvFocusEffect(
                                        shape = RoundedCornerShape(8.dp),
                                        focusedBorderColor = Color.White,
                                        borderWidth = 3.dp,
                                        scaleAmount = 1.05f
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (channel.logoUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = channel.logoUrl,
                                        contentDescription = channel.name,
                                        modifier = Modifier.size(38.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        text = channel.name.take(3).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Separador vertical grueso
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .background(Color.White.copy(alpha = 0.08f))
                            )

                            // CONTENEDOR DE PROGRAMACIONES DESPLAZABLE EN SINCRONÍA HORIZONTAL (REUTILIZANDO SCROLL STATE)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(hourWidth * (timelineEndDecimal - timelineStartDecimal))
                                        .drawBehind {
                                            drawLine(
                                                color = Color.White.copy(alpha = 0.06f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 1f
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val channelPrograms = viewModel.getProgramsForChannel(channel.id)

                                    channelPrograms.forEach { program ->
                                        val programWidth = hourWidth * program.durationHours
                                        val isSelectedInDetails = selectedProgram.id == program.id

                                        // Animaciones fluidas para enfocar y seleccionar
                                        val blockBgColor by animateColorAsState(
                                            targetValue = if (isSelectedInDetails) Color.White else Color(0xFF242730), // Blanco vs Gris oscuro
                                            animationSpec = tween(durationMillis = 200),
                                            label = "bg_color"
                                        )

                                        val borderStrokeColorByState = if (isSelectedInDetails) Color.White else Color.White.copy(alpha = 0.08f)
                                        val borderStrokeWidth = if (isSelectedInDetails) 3.dp else 1.dp

                                        Box(
                                            modifier = Modifier
                                                .width(programWidth)
                                                .fillMaxHeight()
                                                .padding(horizontal = 3.dp, vertical = 6.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(blockBgColor)
                                                .border(
                                                    width = borderStrokeWidth,
                                                    color = borderStrokeColorByState,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    viewModel.selectEpgProgram(program)
                                                    val matchedChannel = allChannels.find { it.id == program.channelId }
                                                    if (matchedChannel != null) {
                                                        viewModel.selectChannel(matchedChannel)
                                                    }
                                                }
                                                .onFocusChanged { focusState ->
                                                    if (focusState.isFocused) {
                                                        viewModel.selectEpgProgram(program)
                                                        val matchedChannel = allChannels.find { it.id == program.channelId }
                                                        if (matchedChannel != null) {
                                                            viewModel.selectChannel(matchedChannel)
                                                        }
                                                    }
                                                }
                                                .tvFocusEffect(
                                                    shape = RoundedCornerShape(8.dp),
                                                    focusedBorderColor = Color.White,
                                                    borderWidth = 3.dp,
                                                    scaleAmount = 1.04f
                                                )
                                                .zIndex(if (isSelectedInDetails) 2f else 1f)
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Column {
                                                Text(
                                                    text = program.title,
                                                    color = if (isSelectedInDetails) Color.Black else Color.White,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${program.startTime} - ${program.endTime}",
                                                    color = if (isSelectedInDetails) Color.Black.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.55f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                // Línea vertical roja indicadora de tiempo en cada fila en su contenedor
                                val lineOffset = (currentTimeDecimal - timelineStartDecimal) * hourWidth.value
                                Box(
                                    modifier = Modifier
                                        .offset(x = lineOffset.dp)
                                        .fillMaxHeight()
                                        .width(1.5.dp)
                                        .background(Color.White.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
