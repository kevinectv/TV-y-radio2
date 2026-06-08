package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import com.example.ui.components.tvFocusEffect

@Composable
fun TvScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    // Simulated EPG timeline variables
    val hourWidth = 240.dp
    val timelineStartDecimal = 8.0f // Starts at 08:00 AM
    val timelineEndDecimal = 24.0f  // Ends at 12:00 AM (16 hours scale)

    // Current pointer simulation: 13.4 decimal corresponds precisely to 13:24 PM (1:24 PM)
    val simulatedTimeDecimal = 13.4f
    val simulatedHour = simulatedTimeDecimal.toInt()
    val simulatedMinute = ((simulatedTimeDecimal - simulatedHour) * 60).toInt()
    val simulatedTimeLabel = String.format("%02d:%02d", simulatedHour, simulatedMinute)

    // Active program selections
    val selectedProgram = viewModel.selectedEpgProgram

    // Category Filter system
    var selectedCategoryFilter by remember { mutableStateOf("All channels") }
    var dateMenuExpanded by remember { mutableStateOf(false) }
    var catMenuExpanded by remember { mutableStateOf(false) }
    val datesList = listOf("Today", "Tomorrow", "Wednesday", "Thursday")
    var selectedDate by remember { mutableStateOf("Today") }

    // Synchronized scroll states for 2D Grid
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Filter channels list dynamically based on category selection
    val filteredChannels = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == "All channels") {
            viewModel.repository.channelsList
        } else {
            viewModel.repository.channelsList.filter {
                it.category.equals(selectedCategoryFilter, ignoreCase = true)
            }
        }
    }

    // Effect to automatically synchronize focus details when selected channel changes
    LaunchedEffect(viewModel.selectedChannel) {
        val runningProgram = viewModel.repository.programsList.find {
            it.channelId == viewModel.selectedChannel.id && it.isActiveAt(simulatedTimeDecimal)
        }
        if (runningProgram != null) {
            viewModel.selectEpgProgram(runningProgram)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04060C)) // Fondo secundario negro / ultra-oscuro
    ) {
        // ==========================================
        // 1. PANEL SUPERIOR DE INFORMACIÓN DEL PROGRAMA
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp) // Proporcionado para estilo Android TV de alta gama
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B1222), // Fondo azul marino oscuro profundo
                            Color(0xFF04060C)  // Degradado a negro sutil
                        )
                    )
                )
        ) {
            // Imagen de portada del programa alineada a la derecha
            Row(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(1f)) // Deja espacio libre a la izquierda para los textos
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                ) {
                    AsyncImage(
                        model = selectedProgram.thumbnailUrl,
                        contentDescription = selectedProgram.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Degradado horizontal para fusionar la foto a la izquierda con el azul marino oscuro
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0D162B),
                                        Color(0xFF0B1222).copy(alpha = 0.85f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    // Degradado vertical inferior para fusionar con la cuadrícula
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF04060C).copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )
                }
            }

            // Datos del programa sobrepuestos a la izquierda
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.58f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PROGRAM",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Nombre de canal | Nombre del programa
                    Text(
                        text = "${viewModel.selectedChannel.name} | ${selectedProgram.title}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Metadatos formateados idénticamente al mockup
                    val durationMin = (selectedProgram.durationHours * 60).toInt()
                    val metaTimeLabel = "08.06.2026 13:24. | ${selectedProgram.category.uppercase()} | $durationMin MIN. | ${selectedProgram.startTime} - ${selectedProgram.endTime}"
                    Text(
                        text = metaTimeLabel,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Descripción del programa (hasta 3 líneas)
                    Text(
                        text = selectedProgram.description,
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Selector de filtros grandes debajo de la descripción
                Row(
                    modifier = Modifier.padding(top = 8.dp),
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
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = selectedDate,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Fecha opciones",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
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
                                    text = { Text(text = date, color = Color.White, fontSize = 13.sp) },
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
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = selectedCategoryFilter,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Canales opciones",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = catMenuExpanded,
                            onDismissRequest = { catMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF1F2228))
                        ) {
                            val categories = listOf("All channels", "Science", "Cinema", "Sci-Fi", "Tech", "Adventure", "Documentary")
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(text = category, color = Color.White, fontSize = 13.sp) },
                                    onClick = {
                                        selectedCategoryFilter = category
                                        catMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. TIMELINE HORIZONTAL SUPERIOR DE LA RECUADRICULA
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "|",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                        hourCounter += 1.0f
                    }

                    // Cápsula roja indicadora de la hora actual en el timeline
                    val currentPointerOffset = (simulatedTimeDecimal - timelineStartDecimal) * hourWidth.value
                    Box(
                        modifier = Modifier
                            .offset(x = (currentPointerOffset - 24).dp)
                            .width(48.dp)
                            .height(22.dp)
                            .background(Color(0xFFE53935), RoundedCornerShape(4.dp))
                            .align(Alignment.CenterStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = simulatedTimeLabel,
                            color = Color.White,
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
        // 3. CUADRÍCULA EPG PRINCIPAL SÍNCRONA 2D
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // COLUMNA FIJA DE CANALES (Solo logos alineados)
            Column(
                modifier = Modifier
                    .width(115.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF080D1A)) // Azul marino oscuro
                    .verticalScroll(verticalScrollState)
            ) {
                filteredChannels.forEach { channel ->
                    val isChannelSelected = viewModel.selectedChannel.id == channel.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isChannelSelected) Color(0xFF2C313C) else Color(0xFF1E222A)) // Gris oscuro
                            .border(
                                width = if (isChannelSelected) 3.dp else 1.dp,
                                color = if (isChannelSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                viewModel.selectChannel(channel)
                                val running = viewModel.repository.programsList.find {
                                    it.channelId == channel.id && it.isActiveAt(simulatedTimeDecimal)
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
                        AsyncImage(
                            model = channel.logoUrl,
                            contentDescription = channel.name,
                            modifier = Modifier.size(38.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Separador vertical grueso
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            // CONTENEDOR DE PROGRAMACIONES DESPLAZABLE VERTICAL Y HORIZONTALMENTE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(verticalScrollState)
                        .width(hourWidth * (timelineEndDecimal - timelineStartDecimal))
                ) {
                    filteredChannels.forEach { channel ->
                        Row(
                            modifier = Modifier
                                .height(64.dp)
                                .fillMaxWidth()
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
                            val channelPrograms = viewModel.repository.programsList.filter { it.channelId == channel.id }

                            channelPrograms.forEach { program ->
                                val programWidth = hourWidth * program.durationHours
                                val isSelectedInDetails = selectedProgram.id == program.id

                                // Animaciones fluidas para enfocar y seleccionar
                                val blockBgColor by animateColorAsState(
                                    targetValue = if (isSelectedInDetails) Color(0xFFF95D02) else Color(0xFF242730), // Naranja vs Gris oscuro
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
                                            val matchedChannel = viewModel.repository.channelsList.find { it.id == program.channelId }
                                            if (matchedChannel != null) {
                                                viewModel.selectChannel(matchedChannel)
                                            }
                                        }
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                viewModel.selectEpgProgram(program)
                                                val matchedChannel = viewModel.repository.channelsList.find { it.id == program.channelId }
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
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${program.startTime} - ${program.endTime}",
                                            color = if (isSelectedInDetails) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.55f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Línea vertical roja indicadora de tiempo que atraviesa toda la cuadrícula
                val lineOffset = (simulatedTimeDecimal - timelineStartDecimal) * hourWidth.value
                Box(
                    modifier = Modifier
                        .offset(x = lineOffset.dp)
                        .fillMaxHeight()
                        .width(1.5.dp)
                        .background(Color(0xFFE53935))
                )
            }
        }
    }
}
