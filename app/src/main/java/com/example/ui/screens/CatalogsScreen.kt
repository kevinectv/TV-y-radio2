package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Catalog
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogsScreen(
    viewModel: MediaViewModel,
    onOpenApiSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val catalogs by viewModel.catalogsStateFlow.collectAsState()

    var showSearchScreen by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCatalogToEdit by remember { mutableStateOf<Catalog?>(null) }
    var showLayoutDialog by remember { mutableStateOf(false) }
    var selectedCatalogForLayout by remember { mutableStateOf<Catalog?>(null) }

    // Sync options states
    val sharedPrefs = remember { context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE) }
    var syncMode by remember { mutableStateOf(sharedPrefs.getString("catalog_sync_mode", "automatic") ?: "automatic") }

    if (showSearchScreen) {
        PremiumCatalogSearchScreen(
            viewModel = viewModel,
            onBack = { showSearchScreen = false }
        )
        return
    }

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

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(10.dp))

                val lastSyncTime by viewModel.lastSyncTime.collectAsState()
                val storedItemsCount by viewModel.storedItemsCount.collectAsState()
                val cacheSize by viewModel.cacheSize.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.catalogRepository?.updateStats()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PERSISTENCIA LOCAL (ROOM DATABASE)",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sincronización
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Última Sincro", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(lastSyncTime, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    // Elementos
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Almacenados", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$storedItemsCount items", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    // Caché
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tamaño Caché", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(cacheSize, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // B) PREMIUM DISCOVERY BANNER (RECOMMENDED)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .tvFocusEffect(shape = RoundedCornerShape(12.dp))
                .clickable { showSearchScreen = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00FF87).copy(alpha = 0.12f)),
            border = BorderStroke(1.5.dp, Color(0xFF00FF87).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar Catálogos",
                    tint = Color(0xFF00FF87),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BUSCAR CATÁLOGOS PREMIUM (RECOMENDADO)",
                        color = Color(0xFF00FF87),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Descubre e instala instantáneamente colecciones completas de Marvel, Star Wars, tendencias Anime, tops de IMDb y más, sin necesidad de copiar ni pegar URLs.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Explorar",
                    tint = Color(0xFF00FF87),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // C) MDBLIST REAL SEARCH ENGINE
        MdbListSearchSection(viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary manual/API rows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .tvFocusEffect(shape = RoundedCornerShape(12.dp))
                    .clickable { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Añadir Manual",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Añadir Manualmente",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .tvFocusEffect(shape = RoundedCornerShape(12.dp))
                    .clickable { onOpenApiSettings() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Estado APIs",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Estado de Servicios",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Diagnóstico y Depuración Card Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .tvFocusEffect(shape = RoundedCornerShape(12.dp))
                    .clickable { onOpenDiagnostics() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.08f)),
                border = BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Diagnóstico",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Diagnóstico de Catálogos y Caché Local",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
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
                    },
                    onLayoutClick = {
                        selectedCatalogForLayout = cat
                        showLayoutDialog = true
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
                        .clickable {
                            syncMode = "automatic"
                            sharedPrefs.edit().putString("catalog_sync_mode", "automatic").apply()
                            Toast.makeText(context, "Sincronización Automática configurada", Toast.LENGTH_SHORT).show()
                        }
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "automatic"),
                        onClick = null,
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
                        .clickable {
                            syncMode = "startup"
                            sharedPrefs.edit().putString("catalog_sync_mode", "startup").apply()
                            Toast.makeText(context, "Sincronizar al iniciar configurado", Toast.LENGTH_SHORT).show()
                        }
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "startup"),
                        onClick = null,
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
                        .clickable {
                            syncMode = "manual"
                            sharedPrefs.edit().putString("catalog_sync_mode", "manual").apply()
                            Toast.makeText(context, "Sincronización Manual Únicamente configurada", Toast.LENGTH_SHORT).show()
                        }
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "manual"),
                        onClick = null,
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
            onConfirm = { name, source, url, layoutType ->
                val newCat = Catalog(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    sourceType = source,
                    url = url,
                    isVisible = true,
                    layoutType = layoutType,
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

    // Dialog 3: Visual Layout Chooser Dialog with real-time preview (Diseño Visual)
    if (showLayoutDialog && selectedCatalogForLayout != null) {
        val cat = selectedCatalogForLayout!!
        VisualLayoutDialog(
            catalog = cat,
            onDismiss = {
                showLayoutDialog = false
                selectedCatalogForLayout = null
            },
            onConfirm = { layoutType ->
                viewModel.updateCatalog(cat.copy(layoutType = layoutType))
                showLayoutDialog = false
                selectedCatalogForLayout = null
                Toast.makeText(context, "Diseño de '${cat.name}' cambiado a: $layoutType", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CompactActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    containerColor: Color = Color.White.copy(alpha = 0.05f),
    contentColor: Color = Color.White,
    borderColor: Color = Color.White.copy(alpha = 0.1f)
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(28.dp)
            .tvFocusEffect(shape = RoundedCornerShape(6.dp)),
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
    onDelete: () -> Unit,
    onLayoutClick: () -> Unit
) {
    val sourceColor = when (catalog.sourceType) {
        "TMDB" -> Color(0xFF00E5FF)
        "Trakt" -> Color(0xFFED1C24)
        "MDBList" -> Color(0xFFFF9800)
        "Import" -> Color(0xFF9C27B0)
        else -> Color(0xFF00FF87)
    }

    val statusColor = when (catalog.status) {
        "Sincronizado" -> Color(0xFF00FF87)
        "Error" -> Color(0xFFFF4D4D)
        else -> Color(0xFFFFC107)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .tvFocusEffect(shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (catalog.isVisible) Color(0xFF131722) else Color(0xFF0C0F14)
        ),
        border = BorderStroke(
            1.dp,
            if (catalog.isVisible) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left vertical aesthetic indicator line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        if (catalog.isVisible) sourceColor else sourceColor.copy(alpha = 0.3f),
                        RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Main Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = catalog.name,
                                color = if (catalog.isVisible) Color.White else Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Custom source type pill
                            Text(
                                text = catalog.sourceType.uppercase(),
                                color = sourceColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp,
                                modifier = Modifier
                                    .background(sourceColor.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                    .border(0.5.dp, sourceColor.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )

                            // Layout Display Type pill
                            Text(
                                text = catalog.layoutType.uppercase(),
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                modifier = Modifier
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                    .border(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }

                    // Visibility Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (catalog.isVisible) Color(0xFF00FF87) else Color.Gray,
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (catalog.isVisible) "Visible" else "Oculto",
                            color = if (catalog.isVisible) Color(0xFF00FF87) else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Stats Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Element count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${catalog.items.size} ítems",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.5.sp
                        )
                    }

                    // Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = catalog.status,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.5.sp
                        )
                    }

                    // Last Sync
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Sincro: ${catalog.lastUpdated}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Compact Action Bar (Scrollable Row of Action Chips)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✏️ Editar
                    CompactActionButton(
                        icon = Icons.Default.Edit,
                        text = "✏️ Editar",
                        onClick = onEdit,
                        containerColor = Color.White.copy(alpha = 0.03f),
                        contentColor = Color.White,
                        borderColor = Color.White.copy(alpha = 0.12f)
                    )

                    // 🔄 Sincronizar
                    CompactActionButton(
                        icon = Icons.Default.Refresh,
                        text = "🔄 Actualizar",
                        onClick = onSync,
                        containerColor = Color(0xFF00E5FF).copy(alpha = 0.06f),
                        contentColor = Color(0xFF00E5FF),
                        borderColor = Color(0xFF00E5FF).copy(alpha = 0.25f)
                    )

                    // ⬆ Subir
                    CompactActionButton(
                        icon = Icons.Default.ArrowUpward,
                        text = "⬆ Subir",
                        onClick = onMoveUp,
                        containerColor = Color.White.copy(alpha = 0.03f),
                        contentColor = Color.White.copy(alpha = 0.85f),
                        borderColor = Color.White.copy(alpha = 0.1f)
                    )

                    // ⬇ Bajar
                    CompactActionButton(
                        icon = Icons.Default.ArrowDownward,
                        text = "⬇ Bajar",
                        onClick = onMoveDown,
                        containerColor = Color.White.copy(alpha = 0.03f),
                        contentColor = Color.White.copy(alpha = 0.85f),
                        borderColor = Color.White.copy(alpha = 0.1f)
                    )

                    // 👁 Mostrar / Ocultar
                    CompactActionButton(
                        icon = if (catalog.isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        text = if (catalog.isVisible) "👁 Ocultar" else "👁 Mostrar",
                        onClick = onToggleVisibility,
                        containerColor = Color.White.copy(alpha = 0.03f),
                        contentColor = if (catalog.isVisible) Color.White.copy(alpha = 0.7f) else Color(0xFF00FF87),
                        borderColor = Color.White.copy(alpha = 0.12f)
                    )

                    // ⚙ Configurar Diseño
                    CompactActionButton(
                        icon = Icons.Default.Layers,
                        text = "⚙ Diseño",
                        onClick = onLayoutClick,
                        containerColor = Color(0xFF00FF87).copy(alpha = 0.06f),
                        contentColor = Color(0xFF00FF87),
                        borderColor = Color(0xFF00FF87).copy(alpha = 0.25f)
                    )

                    // 🗑 Eliminar (Danger style!)
                    CompactActionButton(
                        icon = Icons.Default.Delete,
                        text = "🗑 Eliminar",
                        onClick = onDelete,
                        containerColor = Color(0xFFFF4D4D).copy(alpha = 0.08f),
                        contentColor = Color(0xFFFF4D4D),
                        borderColor = Color(0xFFFF4D4D).copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCatalogDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, source: String, url: String, layoutType: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("TMDB") }
    var url by remember { mutableStateOf("") }
    var layoutType by remember { mutableStateOf("Horizontal") } // "Horizontal" or "Vertical"

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
                    modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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

                // Layout Choice Row
                Text("Diseño de Visualización (Home)", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val choices = listOf(
                        listOf("Horizontal Poster Row", "Vertical Poster Row"),
                        listOf("Landscape Row", "Banner Row"),
                        listOf("Large Featured Row", "Compact Row")
                    )
                    choices.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { choice ->
                                val selected = (layoutType == choice)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.05f))
                                        .clickable { layoutType = choice }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = choice.uppercase(),
                                        color = if (selected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // URL Field
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL del Endpoint / API") },
                    isError = !isUrlValidByRegex,
                    placeholder = { Text("URL de configuración...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
                        onConfirm(name, source, url, layoutType)
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
    var layoutType by remember { mutableStateOf(catalog.layoutType) } // "Horizontal" or "Vertical"

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
                    modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
                    modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
                            onClick = { if (numItems > 25) numItems -= 25 else numItems = 25 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("$numItems", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { if (numItems < 1000) numItems += 25 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Layout Choice selector (Edit)
                Text("Diseño de Visualización (Home)", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val choices = listOf(
                        listOf("Horizontal Poster Row", "Vertical Poster Row"),
                        listOf("Landscape Row", "Banner Row"),
                        listOf("Large Featured Row", "Compact Row")
                    )
                    choices.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { choice ->
                                val selected = (layoutType == choice)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.05f))
                                        .clickable { layoutType = choice }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = choice.uppercase(),
                                        color = if (selected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
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
                            numItems = numItems,
                            layoutType = layoutType
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

@Composable
fun VisualLayoutDialog(
    catalog: Catalog,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedLayout by remember { mutableStateOf(catalog.layoutType) }

    val options = listOf(
        "Horizontal Poster Row" to "Póster horizontal estándar (películas).",
        "Vertical Poster Row" to "Pósters verticales suntuosos (anime/estelares).",
        "Landscape Row" to "Formato panorámico 16:9 (series/shows).",
        "Banner Row" to "Banners anchos refinados (promocionales).",
        "Large Featured Row" to "Súper pósters de gran formato (grandes éxitos).",
        "Compact Row" to "Miniaturas de alta densidad (segundarios/cortos)."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1524),
        tonalElevation = 8.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configurar Diseño Visual",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Selecciona cómo se mostrarán los elementos de '${catalog.name}' en la pantalla Inicio de forma automática:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                // LIVE PREVIEW SIMULATOR MOCKUP BOX (Real-time Preview!)
                Text(
                    text = "VISTA PREVIA EN TIEMPO REAL",
                    color = Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = catalog.name.uppercase(),
                            color = Color(0xFF00E5FF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) { idx ->
                                var cardWidth = 55.dp
                                var cardHeight = 75.dp
                                val color = when (idx) {
                                    0 -> Color(0xFFE50914).copy(alpha = 0.15f)
                                    1 -> Color(0xFF00E5FF).copy(alpha = 0.15f)
                                    else -> Color.White.copy(alpha = 0.05f)
                                }
                                val borderColor = when (idx) {
                                    0 -> Color(0xFFE50914).copy(alpha = 0.5f)
                                    1 -> Color(0xFF00E5FF).copy(alpha = 0.5f)
                                    else -> Color.White.copy(alpha = 0.15f)
                                }

                                when (selectedLayout) {
                                    "Horizontal Poster Row", "Horizontal" -> {
                                        cardWidth = 55.dp
                                        cardHeight = 75.dp
                                    }
                                    "Vertical Poster Row", "Vertical" -> {
                                        cardWidth = 62.dp
                                        cardHeight = 88.dp
                                    }
                                    "Landscape Row" -> {
                                        cardWidth = 85.dp
                                        cardHeight = 50.dp
                                    }
                                    "Banner Row" -> {
                                        cardWidth = 100.dp
                                        cardHeight = 40.dp
                                    }
                                    "Large Featured Row" -> {
                                        cardWidth = 75.dp
                                        cardHeight = 95.dp
                                    }
                                    "Compact Row" -> {
                                        cardWidth = 45.dp
                                        cardHeight = 60.dp
                                    }
                                    else -> {
                                        cardWidth = 55.dp
                                        cardHeight = 75.dp
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(width = cardWidth, height = cardHeight)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                        .border(1.dp, borderColor, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = if (idx == 0) Color(0xFFE50914) else if (idx == 1) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // OPTION CHIPS
                options.forEach { (optionType, optDescription) ->
                    val selected = (selectedLayout == optionType)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLayout = optionType }
                            .tvFocusEffect(shape = RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) Color(0xFF00E5FF).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selected) Color(0xFF00E5FF).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                  Text(
                                      text = optionType,
                                      color = if (selected) Color(0xFF00E5FF) else Color.White,
                                      fontWeight = FontWeight.Bold,
                                      fontSize = 12.sp
                                  )
                                  if (selected) {
                                      Icon(
                                          imageVector = Icons.Default.Check,
                                          contentDescription = "Seleccionado",
                                          tint = Color(0xFF00E5FF),
                                          modifier = Modifier.size(14.dp)
                                      )
                                  }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = optDescription,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedLayout) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Confirmar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
