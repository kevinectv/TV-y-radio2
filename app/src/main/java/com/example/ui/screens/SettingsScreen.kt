package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect

@Composable
fun SettingsScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    var showIptvSources by remember { mutableStateOf(false) }

    if (showIptvSources) {
        IptvSourcesScreen(
            viewModel = viewModel,
            onBack = { showIptvSources = false },
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Broad Overview Title
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "CONFIGURACIÓN GENERAL",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Configura la calidad de vídeo, la escala de la guía EPG y opciones del reproductor multimedia.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            // IPTV PLAYLIST MANAGER ENTRANCE
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIptvSources = true }
                        .tvFocusEffect(shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF4A89FF).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = Color(0xFF4A89FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PLAYLIST MANAGER / FUENTES IPTV",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Configura y administra tus listas de canales M3U, M3U8, credenciales de Xtream Codes y guías EPG XMLTV.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

        // 1. STYLE & THEMING CARD
        item {
            SettingCategoryCard(title = "Aspecto y Temas", icon = Icons.Default.Palette) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tema Visual de la Interfaz",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alterna entre tono oscuro (recomendado para Smart TV) y claro.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = viewModel.isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4A89FF),
                            checkedTrackColor = Color(0xFF4A89FF).copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        // 2. STREAMING QUALITY SELECTOR
        item {
            val qualities = listOf("Auto", "1080p (FHD)", "720p (HD)", "480p (SD)")
            SettingCategoryCard(title = "Calidad de Streaming IPTV", icon = Icons.Default.HighQuality) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Calidad Predeterminada de Reproducción",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Selecciona la resolución por defecto para transmisiones M3U8 y videos en vivo.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        qualities.forEach { q ->
                            val isSelected = viewModel.streamingQuality == q
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF4A89FF) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF4A89FF) else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateStreamQuality(q) }
                                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = q,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. EPG SCALE PICKER
        item {
            val epgScales = listOf("Compacto", "Estándar", "Grande")
            SettingCategoryCard(title = "Guía de Canales EPG", icon = Icons.Default.Dataset) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Proporciones y Tamaño de EPG",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ajusta la altura y el tamaño del texto para las rejillas EPG estilo Tivimate.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        epgScales.forEach { scale ->
                            val isSelected = viewModel.epgScale == scale
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFFF9500) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFFF9500) else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateEpgScale(scale) }
                                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = scale,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. PLAYER DECODER PREFERENCES
        item {
            val decoders = listOf("Hardware (HW+)", "Software (SW)", "Modo Auto")
            SettingCategoryCard(title = "Decodificador de Reproducción", icon = Icons.Default.SettingsInputHdmi) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Algoritmo de Decodificación",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Utiliza aceleración de vídeo de hardware para renderizar a 60FPS estables.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        decoders.forEach { d ->
                            val isSelected = viewModel.playerDecoder == d
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF6B4EFE) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF6B4EFE) else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateDecoder(d) }
                                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = d,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. REGIONS AND COUNTRIES SELECTOR
        item {
            val regions = listOf("Global", "LATAM", "N. América", "Europa")
            SettingCategoryCard(title = "Idioma y Regiones", icon = Icons.Default.Language) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                               text = "Región del Servidor IPTV",
                               color = Color.White,
                               fontSize = 13.sp,
                               fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Establece fuentes m3u de sintonización geográfica.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.updateRegion(if (viewModel.selectedRegion == "LATAM") "Global" else "LATAM") },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = viewModel.selectedRegion,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.06f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                               text = "Idioma del Sistema",
                               color = Color.White,
                               fontSize = 13.sp,
                               fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Regula los nombres descriptivos EPG.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.updateLanguage(if (viewModel.selectedLanguage == "Español") "English" else "Español") },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = viewModel.selectedLanguage,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

// Visual reusable category block
@Composable
fun SettingCategoryCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF4A89FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }

            content()
        }
    }
}
