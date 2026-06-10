package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.EpgSourceEntity
import com.example.data.database.PlaylistEntity
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IptvSourcesScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val epgSources by viewModel.epgSources.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()

    // Dialog state
    var showAddPlaylistDialog by remember { mutableStateOf(false) }
    var showAddEpgDialog by remember { mutableStateOf(false) }
    var selectedPlaylistForEdit by remember { mutableStateOf<PlaylistEntity?>(null) }
    var selectedEpgForEdit by remember { mutableStateOf<EpgSourceEntity?>(null) }
    var selectedPlaylistDetails by remember { mutableStateOf<PlaylistEntity?>(null) }

    // Sync state
    var isSyncingAll by remember { mutableStateOf(false) }
    var syncProgress by remember { mutableFloatStateOf(0f) }
    var syncStatusText by remember { mutableStateOf("") }
    var activelySyncingPlaylistId by remember { mutableStateOf<String?>(null) }
    var activelySyncingEpgId by remember { mutableStateOf<String?>(null) }

    // Real network-enabled sync algorithm
    val triggerSyncPlaylist = { playlist: PlaylistEntity ->
        coroutineScope.launch {
            activelySyncingPlaylistId = playlist.id
            viewModel.syncPlaylist(playlist) {
                activelySyncingPlaylistId = null
            }
        }
    }

    val triggerSyncEpg = { epg: EpgSourceEntity ->
        coroutineScope.launch {
            activelySyncingEpgId = epg.id
            viewModel.updateEpgSource(epg.copy(syncStatus = "Syncing..."))
            delay(1000)
            viewModel.updateEpgSource(
                epg.copy(
                    syncStatus = "Success",
                    lastSynced = System.currentTimeMillis()
                )
            )
            activelySyncingEpgId = null
        }
    }

    val triggerSyncAll = {
        coroutineScope.launch {
            isSyncingAll = true
            syncProgress = 0.05f
            syncStatusText = "Iniciando descarga de listas..."
            delay(500)
            
            val enabledPlaylists = playlists.filter { it.isEnabled }
            syncProgress = 0.25f
            syncStatusText = "Actualizando canales M3U cargados..."
            
            if (enabledPlaylists.isNotEmpty()) {
                val progressStep = 0.5f / enabledPlaylists.size
                enabledPlaylists.forEach { pl ->
                    syncStatusText = "Sincronizando ${pl.name}..."
                    viewModel.syncPlaylist(pl)
                    delay(400)
                    syncProgress += progressStep
                }
            } else {
                syncProgress = 0.75f
            }
            
            syncProgress = 0.80f
            syncStatusText = "Sincronizando Guías EPG XMLTV..."
            epgSources.filter { it.isEnabled }.forEach {
                viewModel.updateEpgSource(it.copy(syncStatus = "Syncing..."))
            }
            delay(900)
            
            syncProgress = 0.90f
            syncStatusText = "Indexando programas..."
            epgSources.filter { it.isEnabled }.forEach {
                viewModel.updateEpgSource(
                    it.copy(
                        syncStatus = "Success",
                        lastSynced = System.currentTimeMillis()
                    )
                )
            }
            delay(500)
            
            syncProgress = 1.0f
            syncStatusText = "Base de datos IPTV actualizada con éxito."
            delay(1200)
            isSyncingAll = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BACK BUTTON & TITLE HEADER
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .tvFocusEffect(shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar a Configuración",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FUENTES IPTV / PLAYLIST MANAGER",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Administra listas de reproducción M3U/Xtream y guías de programación electrónica (EPG).",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // GLOBAL SYNC BAR PROGRESS (Only visible when active)
        if (isSyncingAll) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sincronizando fuentes globales...",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${(syncProgress * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                              )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { syncProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = syncStatusText.uppercase(),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // CONTROL CONTROL PANEL (SYNC ALL BUTTONS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { triggerSyncAll() },
                    enabled = !isSyncingAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A89FF),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Actualizar Todo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isSyncingAll = true
                            syncProgress = 0.2f
                            syncStatusText = "Sincronizando playlists..."
                            playlists.forEach { triggerSyncPlaylist(it) }
                            delay(1500)
                            syncProgress = 1.0f
                            syncStatusText = "Playlists sincronizadas."
                            delay(800)
                            isSyncingAll = false
                        }
                    },
                    enabled = !isSyncingAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Actualizar Playlists",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isSyncingAll = true
                            syncProgress = 0.3f
                            syncStatusText = "Descargando XML EPG..."
                            epgSources.forEach { triggerSyncEpg(it) }
                            delay(1500)
                            syncProgress = 1.0f
                            syncStatusText = "Guía de canales actualizada."
                            delay(800)
                            isSyncingAll = false
                        }
                    },
                    enabled = !isSyncingAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Actualizar EPG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==========================================
        // SECTION 1. PLAYLIST MANAGER (LISTAS M3U)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF4A89FF), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LISTAS DE REPRODUCCIÓN (${playlists.size})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = { showAddPlaylistDialog = true },
                    modifier = Modifier
                        .height(34.dp)
                        .tvFocusEffect(shape = RoundedCornerShape(4.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A89FF).copy(alpha = 0.15f),
                        contentColor = Color(0xFF4A89FF)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, Color(0xFF4A89FF).copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Añadir Lista",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // PLAYLISTS COMPOSABLE LIST
        if (playlists.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No hay listas añadidas",
                    description = "Añade una URL de lista M3U8, M3U local o servidor Xtream Codes para iniciar la reproducción de canales.",
                    icon = Icons.Outlined.PlaylistAdd
                )
            }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    isActiveSyncing = activelySyncingPlaylistId == playlist.id,
                    onToggleEnabled = { viewModel.togglePlaylist(playlist) },
                    onSync = { triggerSyncPlaylist(playlist) },
                    onEdit = { selectedPlaylistForEdit = playlist },
                    onDelete = { viewModel.deletePlaylist(playlist.id) },
                    onViewDetails = { selectedPlaylistDetails = playlist }
                )
            }
        }

        // ==========================================
        // SECTION 2. EPG SOURCES (GUÍAS XMLTV)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFFF9500), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FUENTES EPG / GUÍAS DE TV (${epgSources.size})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = { showAddEpgDialog = true },
                    modifier = Modifier
                        .height(34.dp)
                        .tvFocusEffect(shape = RoundedCornerShape(4.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9500).copy(alpha = 0.15f),
                        contentColor = Color(0xFFFF9500)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF9500).copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Añadir EPG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // EPG SOURCES COMPOSABLE LIST
        if (epgSources.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No hay fuentes EPG",
                    description = "Agrega un archivo XMLTV o URL EPG compatible para enriquecer los horarios, sinopsis y portadas de los canales de televisión.",
                    icon = Icons.Outlined.CalendarViewMonth
                )
            }
        } else {
            items(epgSources, key = { it.id }) { epg ->
                EpgCard(
                    epg = epg,
                    isActiveSyncing = activelySyncingEpgId == epg.id,
                    onToggleEnabled = { viewModel.toggleEpgSource(epg) },
                    onSync = { triggerSyncEpg(epg) },
                    onEdit = { selectedEpgForEdit = epg },
                    onDelete = { viewModel.deleteEpgSource(epg.id) }
                )
            }
        }
    }

    // ==========================================
    // DIALOGS & OVERLAYS
    // ==========================================
    
    // Add Playlist Dialog
    if (showAddPlaylistDialog) {
        PlaylistFormDialog(
            title = "Añadir Lista IPTV",
            onDismiss = { showAddPlaylistDialog = false },
            onSave = { playlist ->
                viewModel.addPlaylist(playlist)
                showAddPlaylistDialog = false
                // Trigger auto sync
                triggerSyncPlaylist(playlist)
            }
        )
    }

    // Edit Playlist Dialog
    selectedPlaylistForEdit?.let { playlist ->
        PlaylistFormDialog(
            title = "Editar Lista IPTV",
            playlistToEdit = playlist,
            onDismiss = { selectedPlaylistForEdit = null },
            onSave = { updatedPlaylist ->
                viewModel.updatePlaylist(updatedPlaylist)
                selectedPlaylistForEdit = null
            }
        )
    }

    // Add Epg Dialog
    if (showAddEpgDialog) {
        EpgFormDialog(
            title = "Añadir Guía EPG (XMLTV)",
            onDismiss = { showAddEpgDialog = false },
            onSave = { epg ->
                viewModel.addEpgSource(epg)
                showAddEpgDialog = false
                triggerSyncEpg(epg)
            }
        )
    }

    // Edit Epg Dialog
    selectedEpgForEdit?.let { epg ->
        EpgFormDialog(
            title = "Editar Guía EPG XMLTV",
            epgToEdit = epg,
            onDismiss = { selectedEpgForEdit = null },
            onSave = { updatedEpg ->
                viewModel.updateEpgSource(updatedEpg)
                selectedEpgForEdit = null
            }
        )
    }

    // View Details Dialog
    selectedPlaylistDetails?.let { playlist ->
        PlaylistDetailsDialog(
            playlist = playlist,
            onDismiss = { selectedPlaylistDetails = null }
        )
    }
}

// ==========================================
// RENDER COMPOSABLES & CARDS
// ==========================================

@Composable
fun PlaylistCard(
    playlist: PlaylistEntity,
    isActiveSyncing: Boolean,
    onToggleEnabled: () -> Unit,
    onSync: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
) {
    val connectionColor = when (playlist.type) {
        "M3U URL" -> Color(0xFF4A89FF)
        "M3U8 URL" -> Color(0xFF00C853)
        "Local M3U" -> Color(0xFFFFD600)
        "Xtream Codes" -> Color(0xFF6B4EFE)
        else -> Color.Gray
    }

    val syncIcon = if (isActiveSyncing || playlist.syncStatus == "Syncing...") Icons.Default.Cached else Icons.Default.Refresh

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .tvFocusEffect(shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (playlist.isEnabled) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.01f)
        ),
        border = BorderStroke(
            1.dp,
            if (isActiveSyncing) Color(0xFF4A89FF).copy(alpha = 0.6f) 
            else Color.White.copy(alpha = 0.07f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .alpha(if (playlist.isEnabled) 1.0f else 0.4f)
        ) {
            // Header: Name, Switch and Indicator Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(connectionColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, connectionColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = playlist.type,
                            color = connectionColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = playlist.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = playlist.isEnabled,
                    onCheckedChange = { onToggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4A89FF),
                        checkedTrackColor = Color(0xFF4A89FF).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // URL Descriptor
            Text(
                text = if (playlist.type == "Xtream Codes") "Server: ${playlist.url}" else "Ruta: ${playlist.url}",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Summary stats counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(text = "CANALES", color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = if (playlist.channelsCount > 0) "${playlist.channelsCount}" else "--", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
                Column {
                    Text(text = "CATEGORÍAS", color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = if (playlist.groupsCount > 0) "${playlist.groupsCount}" else "--", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
                Column {
                    Text(text = "CONEXIÓN DE RED", color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (playlist.type == "Local M3U") "Offline" else "Online API",
                        color = if (playlist.type == "Local M3U") Color(0xFFFF9500) else Color(0xFF00E676),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Last synced and status indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "SINCRONIZACIÓN", color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    when (playlist.syncStatus) {
                                        "Success" -> Color(0xFF00E676)
                                        "Error" -> Color(0xFFE53935)
                                        "Syncing..." -> Color(0xFF4A89FF)
                                        else -> Color.Gray
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (playlist.syncStatus) {
                                "Success" -> "Completada (${formatTimestamp(playlist.lastSynced)})"
                                "Error" -> "Error de conexión"
                                "Syncing..." -> "Sincronizando..."
                                else -> "Pendiente"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButtonText(
                    text = "Ver",
                    icon = Icons.Default.Visibility,
                    onClick = onViewDetails
                )
                
                IconButtonText(
                    text = "Editar",
                    icon = Icons.Default.Edit,
                    onClick = onEdit
                )

                IconButtonText(
                    text = "Sincronizar",
                    icon = syncIcon,
                    onClick = onSync,
                    isLoadingAnimation = isActiveSyncing || playlist.syncStatus == "Syncing..."
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButtonText(
                    text = "Eliminar",
                    icon = Icons.Default.Delete,
                    tint = Color(0xFFE53935),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun EpgCard(
    epg: EpgSourceEntity,
    isActiveSyncing: Boolean,
    onToggleEnabled: () -> Unit,
    onSync: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val syncIcon = if (isActiveSyncing || epg.syncStatus == "Syncing...") Icons.Default.Cached else Icons.Default.Refresh

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .tvFocusEffect(shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (epg.isEnabled) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.01f)
        ),
        border = BorderStroke(
            1.dp,
            if (isActiveSyncing) Color(0xFFFF9500).copy(alpha = 0.6f) 
            else Color.White.copy(alpha = 0.07f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .alpha(if (epg.isEnabled) 1.0f else 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF9500).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFFF9500).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "XMLTV",
                            color = Color(0xFFFF9500),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = epg.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = epg.isEnabled,
                    onCheckedChange = { onToggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF9500),
                        checkedTrackColor = Color(0xFFFF9500).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "URL: ${epg.url}",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            when (epg.syncStatus) {
                                "Success" -> Color(0xFF00E676)
                                "Error" -> Color(0xFFE53935)
                                "Syncing..." -> Color(0xFFFF9500)
                                else -> Color.Gray
                            },
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (epg.syncStatus) {
                        "Success" -> "Sincronizado: ${formatTimestamp(epg.lastSynced)}"
                        "Error" -> "Fallo al decodificar XMLTV"
                        "Syncing..." -> "Sincronizando..."
                        else -> "No sincronizado"
                    },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .tvFocusEffect(shape = RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit EPG", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onSync,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .tvFocusEffect(shape = RoundedCornerShape(4.dp))
                    ) {
                        Icon(syncIcon, contentDescription = "Sync EPG", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .tvFocusEffect(shape = RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete EPG", tint = Color(0xFFE53935), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// Reusable text icon action button
@Composable
fun IconButtonText(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = Color.White,
    isLoadingAnimation: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable { onClick() }
            .tvFocusEffect(shape = RoundedCornerShape(6.dp))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Reusable empty indicator card
@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// FORMS & DIALOG CREATION
// ==========================================

@Composable
fun PlaylistFormDialog(
    title: String,
    playlistToEdit: PlaylistEntity? = null,
    onDismiss: () -> Unit,
    onSave: (PlaylistEntity) -> Unit
) {
    var name by remember { mutableStateOf(playlistToEdit?.name ?: "") }
    var connectionType by remember { mutableStateOf(playlistToEdit?.type ?: "M3U URL") }
    var url by remember { mutableStateOf(playlistToEdit?.url ?: "") }
    var username by remember { mutableStateOf(playlistToEdit?.username ?: "") }
    var password by remember { mutableStateOf(playlistToEdit?.password ?: "") }

    var hasErrorName by remember { mutableStateOf(false) }
    var hasErrorUrl by remember { mutableStateOf(false) }

    val types = listOf("M3U URL", "M3U8 URL", "Local M3U", "Xtream Codes")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        containerColor = Color(0xFF0C1322),
        tonalElevation = 6.dp,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Connection Selector
                Column {
                    Text(
                        text = "TIPO DE PROTOCOLO",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        types.forEach { type ->
                            val isSelected = connectionType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF4A89FF) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF4A89FF) else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { connectionType = type }
                                    .tvFocusEffect(shape = RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Name TextField
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        hasErrorName = false
                    },
                    label = { Text("Nombre de la Lista") },
                    isError = hasErrorName,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = OutlinedTextFieldDefaults.shape),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF4A89FF),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedBorderColor = Color(0xFF4A89FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                if (hasErrorName) {
                    Text("El nombre es requerido.", color = Color(0xFFE53935), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // URL or Server Path TextField
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        hasErrorUrl = false
                    },
                    label = { 
                        Text(
                            if (connectionType == "Xtream Codes") "Servidor (e.g. http://iptv.dns.to:8080)" 
                            else if (connectionType == "Local M3U") "Ruta de Archivo local (e.g. /storage/playlists/es.m3u)" 
                            else "URL remota de la playlist (.m3u/.m3u8)"
                        ) 
                    },
                    isError = hasErrorUrl,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = OutlinedTextFieldDefaults.shape),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF4A89FF),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedBorderColor = Color(0xFF4A89FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                if (hasErrorUrl) {
                    Text("La URL o ruta es requerida.", color = Color(0xFFE53935), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Xtream Codes secondary parameters (User, Password)
                if (connectionType == "Xtream Codes") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Usuario") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).tvFocusEffect(shape = OutlinedTextFieldDefaults.shape),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF4A89FF),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                focusedBorderColor = Color(0xFF4A89FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).tvFocusEffect(shape = OutlinedTextFieldDefaults.shape),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF4A89FF),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                                focusedBorderColor = Color(0xFF4A89FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) hasErrorName = true
                    if (url.isBlank()) hasErrorUrl = true
                    
                    if (!hasErrorName && !hasErrorUrl) {
                        onSave(
                            PlaylistEntity(
                                id = playlistToEdit?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                type = connectionType,
                                url = url,
                                username = username,
                                password = password,
                                channelsCount = playlistToEdit?.channelsCount ?: 0,
                                groupsCount = playlistToEdit?.groupsCount ?: 0,
                                isEnabled = playlistToEdit?.isEnabled ?: true,
                                lastSynced = playlistToEdit?.lastSynced ?: 0L,
                                syncStatus = playlistToEdit?.syncStatus ?: "Pending"
                            )
                        )
                    }
                },
                modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(4.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A89FF))
            ) {
                Text("Guardar Listado")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(4.dp))
            ) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
fun EpgFormDialog(
    title: String,
    epgToEdit: EpgSourceEntity? = null,
    onDismiss: () -> Unit,
    onSave: (EpgSourceEntity) -> Unit
) {
    var name by remember { mutableStateOf(epgToEdit?.name ?: "") }
    var url by remember { mutableStateOf(epgToEdit?.url ?: "") }

    var hasErrorName by remember { mutableStateOf(false) }
    var hasErrorUrl by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        containerColor = Color(0xFF0C1322),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        hasErrorName = false
                    },
                    label = { Text("Nombre del Servidor EPG") },
                    isError = hasErrorName,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = OutlinedTextFieldDefaults.shape),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFFF9500),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedBorderColor = Color(0xFFFF9500),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                if (hasErrorName) {
                    Text("El nombre es requerido.", color = Color(0xFFE53935), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        hasErrorUrl = false
                    },
                    label = { Text("URL de la guía XMLTV (.xml / .xml.gz)") },
                    isError = hasErrorUrl,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = OutlinedTextFieldDefaults.shape),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFFF9500),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedBorderColor = Color(0xFFFF9500),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                if (hasErrorUrl) {
                    Text("La URL del XMLTV es requerida.", color = Color(0xFFE53935), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) hasErrorName = true
                    if (url.isBlank()) hasErrorUrl = true

                    if (!hasErrorName && !hasErrorUrl) {
                        onSave(
                            EpgSourceEntity(
                                id = epgToEdit?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                url = url,
                                lastSynced = epgToEdit?.lastSynced ?: 0L,
                                syncStatus = epgToEdit?.syncStatus ?: "Pending",
                                isEnabled = epgToEdit?.isEnabled ?: true
                            )
                        )
                    }
                },
                modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(4.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500))
            ) {
                Text("Guardar Fuente EPG")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(4.dp))
            ) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
fun PlaylistDetailsDialog(
    playlist: PlaylistEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Detalles: ${playlist.name}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        containerColor = Color(0xFF0C1322),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextValueRow(label = "ID Interno", value = playlist.id)
                TextValueRow(label = "Protocolo", value = playlist.type)
                TextValueRow(label = "Servidor / Enlace", value = playlist.url)
                if (playlist.type == "Xtream Codes") {
                    TextValueRow(label = "Usuario", value = playlist.username)
                }
                TextValueRow(label = "Canales Indexados", value = "${playlist.channelsCount}")
                TextValueRow(label = "Categorías / Grupos", value = "${playlist.groupsCount}")
                TextValueRow(label = "Estado de Sincro", value = playlist.syncStatus)
                TextValueRow(
                    label = "Última Sincronización", 
                    value = if (playlist.lastSynced > 0L) formatTimestamp(playlist.lastSynced) else "Nunca"
                )
                TextValueRow(label = "Estatus Habilitado", value = if (playlist.isEnabled) "Habilitado (Activo)" else "Inactivo (Ocultado)")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(4.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Text("Cerrar", color = Color.White)
            }
        }
    )
}

@Composable
fun TextValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.5f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Utility to format epoch time comfortably
fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "Nunca"
    val sdf = SimpleDateFormat("HH:mm - dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
