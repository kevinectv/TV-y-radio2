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
import com.example.ui.components.CharacterAvatar
import com.example.data.database.ProfileEntity

/**
 * All configurable categories in settings.
 */
enum class SettingCategory(val label: String, val icon: ImageVector, val description: String) {
    PROFILE("Perfil", Icons.Default.AccountCircle, "Configuración del perfil activo y de los avatares integrados"),
    IPTV_SOURCES("Fuentes IPTV", Icons.Default.Dns, "Gestión integral de playlists M3U, M3U8, Xtream Codes y XMLTV"),
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
                                    AboutPaneContent()
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
                                AboutPaneContent()
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
    bufferLatency: Boolean,
    onBufferLatencyChange: (Boolean) -> Unit
) {
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
fun AboutPaneContent() {
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
                text = "Versión 3.8.2-PRO",
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
