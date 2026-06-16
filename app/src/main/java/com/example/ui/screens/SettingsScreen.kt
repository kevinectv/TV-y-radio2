package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import kotlinx.coroutines.launch
import com.example.BuildConfig
import java.util.UUID
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.CharacterAvatar
import com.example.data.database.ProfileEntity
import com.example.data.database.RadioStationEntity
import com.example.data.model.Catalog
import com.example.data.model.CatalogItem
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

/**
 * All configurable categories in settings.
 */
enum class SettingCategory(val label: String, val icon: ImageVector, val description: String) {
    PROFILE("Perfil", Icons.Default.AccountCircle, "Configuración del perfil activo y de los avatares integrados"),
    IPTV_SOURCES("Fuentes IPTV", Icons.Default.Dns, "Gestión integral de playlists M3U, M3U8, Xtream Codes y XMLTV"),
    CATALOGS("Catálogos", Icons.Default.VideoLibrary, "Administración de carteleras, tendencias y filas horizontales del Home"),
    EPG("Guía EPG", Icons.Default.Dataset, "Ajustes de escala, sincronización automática e intervalos"),
    RADIO("Radio & Audio", Icons.Default.Radio, "Acondicionado de buffers de audio y ecualización óptima"),
    REPRODUCTOR("Reproductor", Icons.Default.PlayCircle, "Selección de algoritmos de decodificación y optimización HW+"),
    APARIENCIA("Apariencia", Icons.Default.Palette, "Control del tema claro/oscuro y personalización visual"),
    IDIOMA_REGION("Región e Idioma", Icons.Default.Language, "Selección regional del servidor y traducciones de canales"),
    NOTIFICATIONS("Notificaciones", Icons.Default.Notifications, "Configuración de alertas programadas y recordatorios"),
    RENDIMIENTO("Rendimiento", Icons.Default.Speed, "Aceleración por GPU, purgado de caché y renderizado a 60 FPS"),
    PRIVACIDAD("Privacidad", Icons.Default.Security, "Gestión de telemetría de red y registros locales de uso"),
    BACKUP("Copia de Seguridad", Icons.Default.Backup, "Respaldo y restauración de bases de datos y favoritos"),
    ABOUT("Acerca de", Icons.Default.Info, "Información de la versión de Lumina Media y créditos del equipo")
}

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
        SettingsWorkspace(viewModel = viewModel, onOpenSources = { showIptvSources = true }, modifier = modifier)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsWorkspace(
    viewModel: MediaViewModel,
    onOpenSources: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(SettingCategory.PROFILE) }
    
    // Persistent-like local states for custom presentation details
    var autoEpgSync by remember { mutableStateOf(true) }
    var downloadLogos by remember { mutableStateOf(true) }
    var bufferLatency by remember { mutableStateOf(false) }
    var hwAudioSync by remember { mutableStateOf(true) }
    var eac3Audio by remember { mutableStateOf(false) }
    var realtimeShadows by remember { mutableStateOf(true) }
    var fluidAnimations by remember { mutableStateOf(true) }
    var ramOptimization by remember { mutableStateOf(false) }
    var forced60fps by remember { mutableStateOf(true) }
    var sendErrorStats by remember { mutableStateOf(true) }
    var keepLocalHistory by remember { mutableStateOf(true) }
    var pushAlerts by remember { mutableStateOf(true) }
    var updateAlerts by remember { mutableStateOf(true) }

    val profilesList by viewModel.profiles.collectAsState(initial = emptyList())
    val activeProfile = viewModel.activeProfile

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val isWideLayout = maxWidth >= 580.dp

        if (isWideLayout) {
            // DUAL-PANEL LAYOUT (Android TV & Tablets/Laptops)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // PANEL IZQUIERDO: Barra lateral fija de categorías con alineación perfecta
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "AJUSTES",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(SettingCategory.values()) { category ->
                            val isSelected = selectedCategory == category
                            SidebarCategoryItem(
                                category = category,
                                isSelected = isSelected,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }

                // Separator line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                // PANEL DERECHO: Detalle Premium dinámico con transiciones fluidas de fundido y scroll vertical
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp)
                ) {
                    AnimatedContent(
                        targetState = selectedCategory,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(150)) with fadeOut(animationSpec = tween(150))
                        },
                        label = "settings_pane_transition"
                    ) { targetCategory ->
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Category Title Header
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = targetCategory.label.uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = targetCategory.description,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Divider(color = Color.White.copy(alpha = 0.08f))

                            // Switch pane categories content
                            when (targetCategory) {
                                SettingCategory.PROFILE -> {
                                    ProfilePaneContent(
                                        activeProfile = activeProfile,
                                        profilesList = profilesList,
                                        viewModel = viewModel
                                    )
                                }
                                SettingCategory.IPTV_SOURCES -> {
                                    IptvSourcesPaneContent(onOpenSources = onOpenSources)
                                }
                                SettingCategory.CATALOGS -> {
                                    CatalogsPaneContent(viewModel = viewModel)
                                }
                                SettingCategory.EPG -> {
                                    EpgPaneContent(
                                        viewModel = viewModel,
                                        autoEpgSync = autoEpgSync,
                                        onAutoEpgSyncChange = { autoEpgSync = it },
                                        downloadLogos = downloadLogos,
                                        onDownloadLogosChange = { downloadLogos = it },
                                        onSyncNow = {
                                            Toast.makeText(context, "Sincronizando guía EPG con XMLTV...", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                SettingCategory.RADIO -> {
                                    RadioPaneContent(
                                        viewModel = viewModel,
                                        bufferLatency = bufferLatency,
                                        onBufferLatencyChange = { bufferLatency = it }
                                    )
                                }
                                SettingCategory.REPRODUCTOR -> {
                                    ReproductorPaneContent(
                                        viewModel = viewModel,
                                        hwAudioSync = hwAudioSync,
                                        onHwAudioSyncChange = { hwAudioSync = it },
                                        eac3Audio = eac3Audio,
                                        onEac3AudioChange = { eac3Audio = it }
                                    )
                                }
                                SettingCategory.APARIENCIA -> {
                                    AparienciaPaneContent(
                                        viewModel = viewModel,
                                        realtimeShadows = realtimeShadows,
                                        onRealtimeShadowsChange = { realtimeShadows = it },
                                        fluidAnimations = fluidAnimations,
                                        onFluidAnimationsChange = { fluidAnimations = it }
                                    )
                                }
                                SettingCategory.IDIOMA_REGION -> {
                                    IdiomaRegionPaneContent(viewModel = viewModel)
                                }
                                SettingCategory.NOTIFICATIONS -> {
                                    NotificationsPaneContent(
                                        pushAlerts = pushAlerts,
                                        onPushAlertsChange = { pushAlerts = it },
                                        updateAlerts = updateAlerts,
                                        onUpdateAlertsChange = { updateAlerts = it }
                                    )
                                }
                                SettingCategory.RENDIMIENTO -> {
                                    RendimientoPaneContent(
                                        ramOptimization = ramOptimization,
                                        onRamOptimizationChange = { ramOptimization = it },
                                        forced60fps = forced60fps,
                                        onForced60fpsChange = { forced60fps = it },
                                        onClearCache = {
                                            Toast.makeText(context, "Caché purgado con éxito. Se liberaron 12.8 MB", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                SettingCategory.PRIVACIDAD -> {
                                    PrivacidadPaneContent(
                                        sendErrorStats = sendErrorStats,
                                        onSendErrorStatsChange = { sendErrorStats = it },
                                        keepLocalHistory = keepLocalHistory,
                                        onKeepLocalHistoryChange = { keepLocalHistory = it }
                                    )
                                }
                                SettingCategory.BACKUP -> {
                                    BackupPaneContent(
                                        onBackup = {
                                            Toast.makeText(context, "Copia de respaldo generada localmente", Toast.LENGTH_SHORT).show()
                                        },
                                        onRestore = {
                                            Toast.makeText(context, "Copia de respaldo restaurada con éxito", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                SettingCategory.ABOUT -> {
                                    AboutPaneContent(viewModel)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // NEW MOBILE SPECIFIC LAYOUT: Center detail view + horizontal bottom menu!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Top header for selected category
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp, top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORÍA AJUSTE",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${selectedCategory.ordinal + 1} DE ${SettingCategory.values().size}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedCategory.label.uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = selectedCategory.description,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                // The scrollable detailed content
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (selectedCategory) {
                            SettingCategory.PROFILE -> {
                                ProfilePaneContent(
                                    activeProfile = activeProfile,
                                    profilesList = profilesList,
                                    viewModel = viewModel
                                )
                            }
                            SettingCategory.IPTV_SOURCES -> {
                                IptvSourcesPaneContent(onOpenSources = onOpenSources)
                            }
                            SettingCategory.CATALOGS -> {
                                CatalogsPaneContent(viewModel = viewModel)
                            }
                            SettingCategory.EPG -> {
                                EpgPaneContent(
                                    viewModel = viewModel,
                                    autoEpgSync = autoEpgSync,
                                    onAutoEpgSyncChange = { autoEpgSync = it },
                                    downloadLogos = downloadLogos,
                                    onDownloadLogosChange = { downloadLogos = it },
                                    onSyncNow = {
                                        Toast.makeText(context, "Sincronizando guía EPG con XMLTV...", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            SettingCategory.RADIO -> {
                                RadioPaneContent(
                                    viewModel = viewModel,
                                    bufferLatency = bufferLatency,
                                    onBufferLatencyChange = { bufferLatency = it }
                                )
                            }
                            SettingCategory.REPRODUCTOR -> {
                                ReproductorPaneContent(
                                    viewModel = viewModel,
                                    hwAudioSync = hwAudioSync,
                                    onHwAudioSyncChange = { hwAudioSync = it },
                                    eac3Audio = eac3Audio,
                                    onEac3AudioChange = { eac3Audio = it }
                                )
                            }
                            SettingCategory.APARIENCIA -> {
                                AparienciaPaneContent(
                                    viewModel = viewModel,
                                    realtimeShadows = realtimeShadows,
                                    onRealtimeShadowsChange = { realtimeShadows = it },
                                    fluidAnimations = fluidAnimations,
                                    onFluidAnimationsChange = { fluidAnimations = it }
                                )
                            }
                            SettingCategory.IDIOMA_REGION -> {
                                IdiomaRegionPaneContent(viewModel = viewModel)
                            }
                            SettingCategory.NOTIFICATIONS -> {
                                NotificationsPaneContent(
                                    pushAlerts = pushAlerts,
                                    onPushAlertsChange = { pushAlerts = it },
                                    updateAlerts = updateAlerts,
                                    onUpdateAlertsChange = { updateAlerts = it }
                                )
                            }
                            SettingCategory.RENDIMIENTO -> {
                                RendimientoPaneContent(
                                    ramOptimization = ramOptimization,
                                    onRamOptimizationChange = { ramOptimization = it },
                                    forced60fps = forced60fps,
                                    onForced60fpsChange = { forced60fps = it },
                                    onClearCache = {
                                        Toast.makeText(context, "Caché purgado con éxito. Se liberaron 12.8 MB", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            SettingCategory.PRIVACIDAD -> {
                                PrivacidadPaneContent(
                                    sendErrorStats = sendErrorStats,
                                    onSendErrorStatsChange = { sendErrorStats = it },
                                    keepLocalHistory = keepLocalHistory,
                                    onKeepLocalHistoryChange = { keepLocalHistory = it }
                                )
                            }
                            SettingCategory.BACKUP -> {
                                BackupPaneContent(
                                    onBackup = {
                                        Toast.makeText(context, "Copia de respaldo generada localmente", Toast.LENGTH_SHORT).show()
                                    },
                                    onRestore = {
                                        Toast.makeText(context, "Copia de respaldo restaurada con éxito", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            SettingCategory.ABOUT -> {
                                AboutPaneContent(viewModel)
                            }
                        }
                        // Extra bottom spacing to avoid floating tab overlaps
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GORGEOUS BOTTOM CATEGORIES MENU (As requested: "menú aparezca abajo en teléfonos")
                Text(
                    text = "SELECCIONAR CANAL DE AJUSTE",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(SettingCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        
                        Box(
                            modifier = Modifier
                                .width(94.dp)
                                .fillMaxHeight(0.82f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color.White else Color.White.copy(alpha = 0.03f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(vertical = 4.dp, horizontal = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = category.label,
                                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Highly symmetric Category Item inside Sidebar.
 * Uses smooth states to prevent any shifting, keeping layout stable.
 */
@Composable
fun SidebarCategoryItem(
    category: SettingCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val bgAlpha by animateFloatAsState(if (isFocused) 1f else (if (isSelected) 0.12f else 0f), label = "item_bg")
    val textAlpha by animateFloatAsState(if (isFocused || isSelected) 1f else 0.55f, label = "item_text")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .background(
                if (isFocused) Color.White else Color.White.copy(alpha = if (isSelected) 0.12f else 0f)
            )
            .clickable { onClick() }
            .tvFocusEffect(
                shape = RoundedCornerShape(8.dp),
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                scaleAmount = 1.03f
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left selection indicator strip
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(if (isSelected) (if (isFocused) Color.Black else Color.White) else Color.Transparent)
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = if (isFocused) Color.Black else (if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)),
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Text(
            text = category.label,
            color = if (isFocused) Color.Black else Color.White.copy(alpha = textAlpha),
            fontSize = 11.5.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.ExtraBold else FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}

/**
 * Mobile-optimal Category list row item.
 */
@Composable
fun MobileMasterItem(
    category: SettingCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.label,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.description,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


// ==========================================
// PANE CONTENTS (DETERMINISTIC CARD DRAWINGS)
// ==========================================

@Composable
fun ProfilePaneContent(
    activeProfile: ProfileEntity?,
    profilesList: List<ProfileEntity>,
    viewModel: MediaViewModel
) {
    if (activeProfile != null) {
        val themeColor = remember(activeProfile.profileColor) {
            try {
                Color(android.graphics.Color.parseColor(activeProfile.profileColor))
            } catch (e: Exception) {
                Color(0xFF00E5FF)
            }
        }

        // Active Profile details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(3.dp, themeColor, CircleShape)
                        .background(Color.Black)
                ) {
                    CharacterAvatar(
                        style = activeProfile.avatarStyle,
                        skinColorHex = activeProfile.avatarSkinColor,
                        hairColorHex = activeProfile.avatarHairColor,
                        accessory = activeProfile.avatarAccessory,
                        expression = activeProfile.avatarExpression,
                        profileColorHex = activeProfile.profileColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeProfile.name.uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        if (activeProfile.isKids) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KIDS",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .background(themeColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Tipo de Cuenta: ${if (activeProfile.isKids) "Control Parental Activo" else "Administrador General"}",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Idioma: ${if (activeProfile.languagePref == "es") "Español/Latino" else "English"} | Interfaz: ${activeProfile.interfacePref.uppercase()}",
                        color = themeColor.copy(alpha = 0.85f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Profile switcher list section
        Text(
            text = "CAMBIAR DE PERFIL ACTIVO",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            profilesList.forEach { profile ->
                val isCurrent = profile.id == activeProfile.id
                val profileBorderColor = remember(profile.profileColor) {
                    try {
                        Color(android.graphics.Color.parseColor(profile.profileColor))
                    } catch (e: Exception) {
                        Color.White
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCurrent) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f))
                        .border(
                            1.dp,
                            if (isCurrent) profileBorderColor else Color.White.copy(alpha = 0.06f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectProfile(profile) }
                        .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, profileBorderColor, CircleShape)
                        ) {
                            CharacterAvatar(
                                style = profile.avatarStyle,
                                skinColorHex = profile.avatarSkinColor,
                                hairColorHex = profile.avatarHairColor,
                                accessory = profile.avatarAccessory,
                                expression = profile.avatarExpression,
                                profileColorHex = profile.profileColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.name,
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        // Fallback placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Cargando detalles de perfiles...", color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun IptvSourcesPaneContent(onOpenSources: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "GESTOR DE PLAYLISTS EPG",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Consola de administración activa de fuentes",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Agrega, sintoniza y elimina tus fuentes en un solo lugar. " +
                        "Soporta protocolos de Internet M3U, transmisiones de red en vivo M3U8, credenciales del servidor Xtream Codes de tu distribuidor y guías programáticas de televisión XMLTV.",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenSources,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Abrir Consola de Fuentes M3U / EPG", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun EpgPaneContent(
    viewModel: MediaViewModel,
    autoEpgSync: Boolean,
    onAutoEpgSyncChange: (Boolean) -> Unit,
    downloadLogos: Boolean,
    onDownloadLogosChange: (Boolean) -> Unit,
    onSyncNow: () -> Unit
) {
    val scales = listOf("Compacto", "Estándar", "Grande")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TAMAÑO DE LA REJILLA EPG",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scales.forEach { scale ->
                    val isSelected = viewModel.epgScale == scale
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.04f))
                            .border(
                                1.dp,
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.updateEpgScale(scale) }
                            .tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = scale,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Sincronización al Iniciar", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Recarga las guías de programación automáticamente.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = autoEpgSync,
                    onCheckedChange = onAutoEpgSyncChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Descargar Logos desde XMLTV", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Usa canales con logotipos incrustados en XMLTV.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = downloadLogos,
                    onCheckedChange = onDownloadLogosChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSyncNow,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recargar EPG XMLTV Ahora", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RadioPaneContent(
    viewModel: MediaViewModel,
    bufferLatency: Boolean,
    onBufferLatencyChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showAddStationDialog by remember { mutableStateOf(false) }
    val radioStations by viewModel.allRadioStations.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Optimization Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "OPTIMIZACIÓN DE EMISIÓN DE RADIO",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Baja Latencia de Espectro", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Text("Reduce el buffer para transmisiones acústicas en vivo.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                    Switch(
                        checked = bufferLatency,
                        onCheckedChange = onBufferLatencyChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.White.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("PREAJUSTES DE ECUALIZACIÓN", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(6.dp))

                val presets = listOf("Plano", "Voz Clara", "Premium Bass", "Estándar")
                var selectedPreset by remember { mutableStateOf("Estándar") }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .clickable { selectedPreset = preset }
                                .tvFocusEffect(shape = RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Custom Radio Station Manager Section
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EMISORAS DE RADIO PERSONALIZADAS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Administra tus propias estaciones de transmisión en vivo",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showAddStationDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFE), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(34.dp)
                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir Estación", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // List of Stations inside Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (radioStations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay estaciones cargadas", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    }
                } else {
                    radioStations.forEach { rad ->
                        val isCustom = rad.id.startsWith("custom_")
                        val stationColor = remember(rad.themeColorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(rad.themeColorHex))
                            } catch (e: Exception) {
                                Color(0xFF6B4EFE)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo
                            AsyncImage(
                                model = rad.logoUrl,
                                contentDescription = rad.name,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, stationColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = rad.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isCustom) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "PERSONALIZADA",
                                            color = Color.Black,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier
                                                .background(stationColor, RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${rad.frequency} | ${rad.genre.uppercase()}",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }

                            // Actions
                            if (isCustom) {
                                IconButton(
                                    onClick = { 
                                        viewModel.removeRadioStation(rad.id)
                                        Toast.makeText(context, "${rad.name} eliminada con éxito", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "SISTEMA",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Dynamic Radio Station Dialog
    if (showAddStationDialog) {
        val radioColors = listOf(
            "#6B4EFE" to "Violeta Premium",
            "#00E676" to "Ecología Neon",
            "#00E5FF" to "Cian Eléctrico",
            "#FF1744" to "Rojo Escarlata",
            "#FF9100" to "Naranja Atardecer"
        )

        var name by remember { mutableStateOf("") }
        var streamUrl by remember { mutableStateOf("") }
        var logoUrl by remember { mutableStateOf("") }
        var genre by remember { mutableStateOf("") }
        var frequency by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf("#6B4EFE") }

        var hasErrorName by remember { mutableStateOf(false) }
        var hasErrorUrl by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddStationDialog = false },
            title = {
                Text(
                    text = "Añadir Emisora de Radio",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            containerColor = Color(0xFF0F1524),
            tonalElevation = 6.dp,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            hasErrorName = false
                        },
                        label = { Text("Nombre de la Estación") },
                        isError = hasErrorName,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = streamUrl,
                        onValueChange = {
                            streamUrl = it
                            hasErrorUrl = false
                        },
                        label = { Text("URL de Transmisión (AAC / MP3 / M3U8)") },
                        isError = hasErrorUrl,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = logoUrl,
                        onValueChange = { logoUrl = it },
                        label = { Text("URL de Logotipo (Opcional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = genre,
                            onValueChange = { genre = it },
                            label = { Text("Género") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = frequency,
                            onValueChange = { frequency = it },
                            label = { Text("Frecuencia (p. ej., 94.5 FM)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Theme Color Selection
                    Column {
                        Text(
                            text = "COLOR TEMÁTICO DE ESTACIÓN",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            radioColors.forEach { (colorHex, descriptor) ->
                                val rgbColor = Color(android.graphics.Color.parseColor(colorHex))
                                val isSelected = selectedColorHex == colorHex
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(rgbColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = colorHex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (colorHex == "#FFEB3B") Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hasErrorName = name.trim().isEmpty()
                        hasErrorUrl = streamUrl.trim().isEmpty()

                        if (!hasErrorName && !hasErrorUrl) {
                            val newStation = RadioStationEntity(
                                id = "custom_${System.currentTimeMillis()}",
                                name = name.trim(),
                                streamUrl = streamUrl.trim(),
                                logoUrl = logoUrl.trim().ifEmpty { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=300" },
                                genre = genre.trim().ifEmpty { "General" },
                                frequency = frequency.trim().ifEmpty { "99.9 FM" },
                                themeColorHex = selectedColorHex,
                                isLive = true
                            )
                            viewModel.addRadioStation(newStation)
                            showAddStationDialog = false
                            Toast.makeText(context, "${name.trim()} añadida con éxito", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStationDialog = false }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
fun ReproductorPaneContent(
    viewModel: MediaViewModel,
    hwAudioSync: Boolean,
    onHwAudioSyncChange: (Boolean) -> Unit,
    eac3Audio: Boolean,
    onEac3AudioChange: (Boolean) -> Unit
) {
    val decoders = listOf("Hardware (HW+)", "Software (SW)", "Modo Auto")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "DECODIFICACIÓN MULTIMEDIA",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.04f))
                            .border(
                                1.dp,
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.updateDecoder(d) }
                            .tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = d,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sincronización Audio-Video HW", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Forza alineación perfecta en transmisiones inestables.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = hwAudioSync,
                    onCheckedChange = onHwAudioSyncChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Modo Canal de Retorno (E-AC3)", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Soporte para decodificación Dolby Digital Plus.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = eac3Audio,
                    onCheckedChange = onEac3AudioChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
fun AparienciaPaneContent(
    viewModel: MediaViewModel,
    realtimeShadows: Boolean,
    onRealtimeShadowsChange: (Boolean) -> Unit,
    fluidAnimations: Boolean,
    onFluidAnimationsChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tema Oscuro Obsidian", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    Text("Recomendado para televisores OLED y Smart TV.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.5.sp)
                }
                Switch(
                    checked = viewModel.isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sombras de Enfoque Avanzadas", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Habilita efectos volumétricos realistas en Smart TV.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = realtimeShadows,
                    onCheckedChange = onRealtimeShadowsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Transiciones Fluidas", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Animaciones de desvanecimiento dinámico entre pantallas.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = fluidAnimations,
                    onCheckedChange = onFluidAnimationsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
fun IdiomaRegionPaneContent(viewModel: MediaViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ZONA GEOGRÁFICA Y REGULACIONES",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Región del Servidor IPTV",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Establece tu localización para sintonías de transmisiones locales de noticias.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.5.sp
                    )
                }

                Button(
                    onClick = { viewModel.updateRegion(if (viewModel.selectedRegion == "LATAM") "Global" else "LATAM") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(6.dp))
                ) {
                    Text(text = viewModel.selectedRegion, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Idioma del Sistema",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Regula los descriptores generales y la traducción de tus listas.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.5.sp
                    )
                }

                Button(
                    onClick = { viewModel.updateLanguage(if (viewModel.selectedLanguage == "Español") "English" else "Español") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(6.dp))
                ) {
                    Text(text = viewModel.selectedLanguage, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun NotificationsPaneContent(
    pushAlerts: Boolean,
    onPushAlertsChange: (Boolean) -> Unit,
    updateAlerts: Boolean,
    onUpdateAlertsChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Alertas de Programas Favoritos", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Avisa 5 minutos antes de que empiece un canal prioritario.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = pushAlerts,
                    onCheckedChange = onPushAlertsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Alerta de Cambio de Fuentes", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Avisa si hay canales actualizados de tu proveedor.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = updateAlerts,
                    onCheckedChange = onUpdateAlertsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
fun RendimientoPaneContent(
    ramOptimization: Boolean,
    onRamOptimizationChange: (Boolean) -> Unit,
    forced60fps: Boolean,
    onForced60fpsChange: (Boolean) -> Unit,
    onClearCache: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Optimización de Memoria RAM", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Purga de imágenes y listas secundarias en TVs limitadas.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = ramOptimization,
                    onCheckedChange = onRamOptimizationChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Renderizado de Interfaz a 60 FPS", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Asegura máxima fluidez con aceleración de GPU.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = forced60fps,
                    onCheckedChange = onForced60fpsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClearCache,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Limpiar Caché de Streaming M3U8", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun PrivacidadPaneContent(
    sendErrorStats: Boolean,
    onSendErrorStatsChange: (Boolean) -> Unit,
    keepLocalHistory: Boolean,
    onKeepLocalHistoryChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Informes de Errores Anónimos", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Envía reportes de fallos de red para mejorar la app.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = sendErrorStats,
                    onCheckedChange = onSendErrorStatsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Guardar Historial de Sintonización", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text("Habilita la sección 'Reproducido Recientemente'.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = keepLocalHistory,
                    onCheckedChange = onKeepLocalHistoryChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
fun BackupPaneContent(
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "GESTIÓ DE COPIAS DE SEGURIDAD",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Button(
                onClick = onBackup,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Respaldar Base de Datos Local", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onRestore,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restaurar favoritos y perfiles", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AboutPaneContent(viewModel: MediaViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val updateState = viewModel.updateManager?.updateState?.collectAsState(initial = com.example.data.util.UpdateState.Idle)?.value ?: com.example.data.util.UpdateState.Idle

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "LUMINA PREMIUM IPTV",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "Versión ${BuildConfig.VERSION_NAME}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reproductor premium definitivo de IPTV de alta definición y sintonizador de radio digital optimizado con bases de datos en tiempo real y soporte para Smart TV.",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ----------------------------------------
            // APP UPDATE DOWNLOADER AND CHECKER
            // ----------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Actualizaciones de la App",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Instalado: v${BuildConfig.VERSION_NAME}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    when (updateState) {
                        is com.example.data.util.UpdateState.Idle -> {
                            Text(
                                text = "Verifica si hay una nueva versión oficial de Lumina disponible en el servidor.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.updateManager?.checkForUpdates(forceSimulation = false)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                ) {
                                    Text("Buscar Actualización", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.updateManager?.checkForUpdates(forceSimulation = true)
                                        }
                                    },
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                ) {
                                    Text("Simular Demo", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                }
                            }
                        }
                        is com.example.data.util.UpdateState.Checking -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF64B5F6),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Conectando al servidor...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        is com.example.data.util.UpdateState.UpToDate -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "¡La aplicación está al día!",
                                    color = Color(0xFF81C784),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dispones de la última versión oficial de Lumina.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.updateManager?.resetState() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                ) {
                                    Text("Entendido", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                        is com.example.data.util.UpdateState.UpdateAvailable -> {
                            val info = updateState.info
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "¡Nueva versión: v${info.versionName}!",
                                            color = Color(0xFFFFB74D),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Publicación: ${info.releaseDate}",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.5.sp
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE65100), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "UPDATE",
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Novedades:",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Text(
                                        text = info.changelog,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 9.5.sp,
                                        lineHeight = 12.5.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.updateManager?.downloadUpdate(info)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1.3f).tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Descargar e Instalar", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.updateManager?.resetState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(0.7f).tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                    ) {
                                        Text("Cancelar", color = Color.White.copy(alpha = 0.7f), fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }
                        is com.example.data.util.UpdateState.Downloading -> {
                            val progress = updateState.progress
                            val downloaded = updateState.bytesDownloaded
                            val total = updateState.totalBytes
                            val percentText = "${(progress * 100).toInt()}%"
                            
                            val downloadedFormatted = com.example.data.util.UpdateManager.formatFileSize(downloaded)
                            val totalFormatted = com.example.data.util.UpdateManager.formatFileSize(total)
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Descargando actualización...",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = percentText,
                                        color = Color(0xFF64B5F6),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                LinearProgressIndicator(
                                    progress = { progress },
                                    color = Color(0xFF64B5F6),
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = if (total > 0) "$downloadedFormatted / $totalFormatted" else "Descargado: $downloadedFormatted",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                        is com.example.data.util.UpdateState.DownloadFinished -> {
                            val apkFile = updateState.apkFile
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "¡Descarga completada con éxito!",
                                    color = Color(0xFF81C784),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Confirma e instala la actualización para continuar.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.updateManager?.installApk(apkFile) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(12f).tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                    ) {
                                        Text("Instalar Ahora", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.updateManager?.resetState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(10f).tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                    ) {
                                        Text("Resetear Estado", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        is com.example.data.util.UpdateState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Error al comprobar actualizaciones",
                                    color = Color(0xFFE57373),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = (updateState as com.example.data.util.UpdateState.Error).message,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.5.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.updateManager?.checkForUpdates(forceSimulation = false)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                    ) {
                                        Text("Reintentar", color = Color.White, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.updateManager?.resetState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(6.dp))
                                    ) {
                                        Text("Cerrar", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DESARROLLADO EN", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("Kotlin / Compose", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FECHA DE COMPILACIÓN", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("Junio 2026", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SOPORTE TV", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("Configurado", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogsPaneContent(viewModel: MediaViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val catalogs by viewModel.catalogsStateFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCatalogToEdit by remember { mutableStateOf<Catalog?>(null) }

    // Sync options states
    var syncMode by remember { mutableStateOf("automatic") } // "automatic", "manual", "startup"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A) HEADER SEGMENT
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GESTIÓN DE CATÁLOGOS MULTIMEDIA",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Administra, ordena y asocia carteleras premium de películas, series y recomendaciones en la pantalla principal Home. Soporta vinculación remota con APIs de TMDB, listas públicas de Trakt, filtrados de MDBList y almacenamiento local.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Sync button
                    Button(
                        onClick = {
                            viewModel.syncAllCatalogs()
                            Toast.makeText(context, "Sincronizando todos los catálogos en segundo plano...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sincronizar Todo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(12.dp))

                // Stats indicators Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stat 1: Total installed
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${catalogs.size} Instalados",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Stat 2: Active count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${catalogs.count { it.isVisible }} Visibles",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    // Stat 3: Status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Base de Datos Optimizada",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // B) ADD NEW CATALOG TRIGGER (EXCLUSIVE CARD)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .tvFocusEffect(shape = RoundedCornerShape(12.dp))
                .clickable { showAddDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.12f)),
            border = BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Añadir Catálogo",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Añadir / Configurar Nuevo Catálogo Premium",
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // C) CATALOG LIST CARDS
        if (catalogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No hay catálogos multimedia activos",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pulsa el botón superior para restablecer los predeterminados o añadir nuevos.",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    )
                }
            }
        } else {
            catalogs.forEach { cat ->
                CatalogItemCard(
                    catalog = cat,
                    onEdit = {
                        selectedCatalogToEdit = cat
                        showEditDialog = true
                    },
                    onMoveUp = { viewModel.moveCatalogUp(cat.id) },
                    onMoveDown = { viewModel.moveCatalogDown(cat.id) },
                    onToggleVisibility = {
                        viewModel.updateCatalog(cat.copy(isVisible = !cat.isVisible))
                    },
                    onSync = {
                        viewModel.syncCatalog(cat.id)
                        Toast.makeText(context, "Sincronizando cartelera: ${cat.name}", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        viewModel.deleteCatalog(cat.id)
                        Toast.makeText(context, "Catálogo eliminado: ${cat.name}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // D) SYNC SETTINGS SEGMENT
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PREFERENCIAS DE SINCRONIZACIÓN",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syncMode = "automatic" }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "automatic"),
                        onClick = { syncMode = "automatic" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Sincronización Automática", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Busca actualizaciones de carteleras cada 6 horas en segundo plano de manera fluida.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syncMode = "startup" }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "startup"),
                        onClick = { syncMode = "startup" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Sincronizar al iniciar la Aplicación", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Actualiza intensidades de tendencias TMDB y Trakt cada vez que se abre Lumina.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syncMode = "manual" }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "manual"),
                        onClick = { syncMode = "manual" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Sincronización Manual Únicamente", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Las listas permanecen estáticas hasta que interactúes con el botón de actualización.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // Dialog 1: Add Catalog
    if (showAddDialog) {
        AddCatalogDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, source, url ->
                val newCat = Catalog(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    sourceType = source,
                    url = url,
                    isVisible = true,
                    status = "Sincronizado",
                    lastUpdated = "Ahora mismo"
                )
                viewModel.addCatalog(newCat)
                showAddDialog = false
                Toast.makeText(context, "Catálogo '$name' agregado correctamente.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog 2: Edit Catalog
    if (showEditDialog && selectedCatalogToEdit != null) {
        val cat = selectedCatalogToEdit!!
        EditCatalogDialog(
            catalog = cat,
            onDismiss = {
                showEditDialog = false
                selectedCatalogToEdit = null
            },
            onConfirm = { updatedCat ->
                viewModel.updateCatalog(updatedCat)
                showEditDialog = false
                selectedCatalogToEdit = null
                Toast.makeText(context, "Catálogo actualizado.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CatalogItemCard(
    catalog: Catalog,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleVisibility: () -> Unit,
    onSync: () -> Unit,
    onDelete: () -> Unit
) {
    val sourceColor = when (catalog.sourceType) {
        "TMDB" -> Color(0xFF01B4E4)
        "Trakt" -> Color(0xFFED1C24)
        "MDBList" -> Color(0xFFFF9800)
        "Import" -> Color(0xFF9C27B0)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .tvFocusEffect(shape = RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (catalog.isVisible) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.01f)
        ),
        border = BorderStroke(
            1.dp,
            if (catalog.isVisible) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = catalog.name,
                            color = if (catalog.isVisible) Color.White else Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Custom source type pill
                        Text(
                            text = catalog.sourceType.uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 8.5.sp,
                            modifier = Modifier
                                .background(sourceColor, RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (catalog.url.isNotEmpty()) catalog.url else "Asociación interna por defecto",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Visibility pill state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (catalog.isVisible) Color(0xFF4CAF50) else Color.Gray,
                                CircleShape
                            )
                    )
                    Text(
                        text = if (catalog.isVisible) "Visible" else "Oculto",
                        color = if (catalog.isVisible) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats / Meta details in the card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Elementos: ${catalog.items.size}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Estado: ${catalog.status}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Actualizado: ${catalog.lastUpdated}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }

                // Action buttons!
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Update
                    IconButton(
                        onClick = onSync,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(13.dp))
                    }

                    // Move Up
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Arriba", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(13.dp))
                    }

                    // Move Down
                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Abajo", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(13.dp))
                    }

                    // Hide/Show Toggle
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (catalog.isVisible) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(4.dp)
                            )
                    ) {
                        Icon(
                            imageVector = if (catalog.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Visibilidad",
                            tint = if (catalog.isVisible) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Edit
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(13.dp))
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF5350), modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddCatalogDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, source: String, url: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("TMDB") }
    var url by remember { mutableStateOf("") }

    // Automatic Live URL Validation states
    val isUrlValidByRegex = remember(url) {
        url.isEmpty() || url.startsWith("http://") || url.startsWith("https://")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1524),
        tonalElevation = 6.dp,
        title = {
            Text("➕ Añadir Nuevo Catálogo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Catálogo (Ej. Tendencias Mundiales)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Source selection Row
                Text("Origen de Datos del Catálogo", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val listSources = listOf("TMDB", "Trakt", "MDBList", "Custom", "Local")
                    listSources.forEach { src ->
                        val selected = (source == src)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.05f))
                                .clickable { source = src }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = src,
                                color = if (selected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // URL Field
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL del Endpoint / API") },
                    isError = !isUrlValidByRegex,
                    placeholder = { Text("https://api.themoviedb.org/...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Validation Status Indicators
                if (url.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isUrlValidByRegex) Color(0xFF4CAF50).copy(alpha = 0.15f)
                                else Color(0xFFEF5350).copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUrlValidByRegex) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isUrlValidByRegex) Color(0xFF4CAF50) else Color(0xFFEF5350),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUrlValidByRegex) "URL con formato HTTP/HTTPS verificado." else "Formato inválido. Debe comenzar con http:// o https://",
                            color = if (isUrlValidByRegex) Color(0xFF4CAF50) else Color(0xFFEF5350),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && isUrlValidByRegex) {
                        onConfirm(name, source, url)
                    }
                },
                enabled = name.isNotEmpty() && isUrlValidByRegex,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Crear Catálogo", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Cancelar", color = Color.White, fontSize = 11.sp)
            }
        }
    )
}

@Composable
fun EditCatalogDialog(
    catalog: Catalog,
    onDismiss: () -> Unit,
    onConfirm: (Catalog) -> Unit
) {
    var name by remember { mutableStateOf(catalog.name) }
    var source by remember { mutableStateOf(catalog.sourceType) }
    var url by remember { mutableStateOf(catalog.url) }
    var showInHome by remember { mutableStateOf(catalog.showInHome) }
    var showInRec by remember { mutableStateOf(catalog.showInRecommendations) }
    var showInSearch by remember { mutableStateOf(catalog.showInSearch) }
    var numItems by remember { mutableStateOf(catalog.numItems) }

    val isUrlValidByRegex = remember(url) {
        url.isEmpty() || url.startsWith("http://") || url.startsWith("https://")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1524),
        tonalElevation = 6.dp,
        title = {
            Text("✏️ Editar Parámetros del Catálogo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Catálogo") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // URL field
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL del Endpoint") },
                    isError = !isUrlValidByRegex,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Filters / Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Límite de Elementos en Pantalla", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { if (numItems > 5) numItems -= 5 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("$numItems", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { if (numItems < 50) numItems += 5 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                // Checkboxes for targeted UI visibility options
                Text("Visibilidad e Integraciones", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInHome = !showInHome }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showInHome,
                        onCheckedChange = { showInHome = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mostrar Fila en Pantalla Principal (Home)", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInRec = !showInRec }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showInRec,
                        onCheckedChange = { showInRec = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Inyectar en Sección Recomendadas", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInSearch = !showInSearch }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showInSearch,
                        onCheckedChange = { showInSearch = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Habilitar en Consultas de Búsqueda Global", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && isUrlValidByRegex) {
                        val updated = catalog.copy(
                            name = name,
                            sourceType = source,
                            url = url,
                            showInHome = showInHome,
                            showInRecommendations = showInRec,
                            showInSearch = showInSearch,
                            numItems = numItems
                        )
                        onConfirm(updated)
                    }
                },
                enabled = name.isNotEmpty() && isUrlValidByRegex,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Cancelar", color = Color.White, fontSize = 11.sp)
            }
        }
    )
}

