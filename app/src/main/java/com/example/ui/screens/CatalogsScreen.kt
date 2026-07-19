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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Catalog
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import java.util.UUID

enum class AddCatalogTab {
    CURATED,
    MDBLIST,
    MANUAL
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogsScreen(
    viewModel: MediaViewModel,
    onOpenApiSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val context = LocalContext.current
    val catalogs by viewModel.catalogsStateFlow.collectAsState()

    var showAddCatalogMainDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCatalogToEdit by remember { mutableStateOf<Catalog?>(null) }

    // Sync options states
    val sharedPrefs = remember { context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE) }
    var syncMode by remember { mutableStateOf(sharedPrefs.getString("catalog_sync_mode", "automatic") ?: "automatic") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ----------------- PREMIUM TV HEADER -----------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CATÁLOGOS MULTIMEDIA",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Organiza, ordena y personaliza las filas de películas, series y anime en tu pantalla principal. Soporta vinculación de APIs, Trakt, filtros avanzados de MDBList y almacenamiento local.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Sync all button
                    Button(
                        onClick = {
                            viewModel.syncAllCatalogs()
                            Toast.makeText(context, "Sincronizando todos los catálogos en segundo plano...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.tvFocusEffect(
                            shape = RoundedCornerShape(10.dp),
                            focusedBorderColor = Color.White,
                            liftOnFocus = true,
                            scaleAmount = 1.05f
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sincronizar Todo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(16.dp))

                // Stats Dashboard Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Stat 1: Total installed
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${catalogs.size} Instalados",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Stat 2: Active count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF00FF87),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${catalogs.count { it.isVisible }} Visibles",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    // Stat 3: Database Status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00FF87),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Room Database Optimizada",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(12.dp))

                // Room DB Live Stats
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
                        text = "ESTADÍSTICAS DE PERSISTENCIA LOCAL",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Última Sincro", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(lastSyncTime, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Almacenados", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$storedItemsCount items", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tamaño Caché", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(cacheSize, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ----------------- GIANT MAIN PRIMARY ACTION BUTTON -----------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .tvFocusEffect(
                    shape = RoundedCornerShape(16.dp),
                    focusedBorderColor = Color(0xFF00FF87),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    borderWidth = 3.dp,
                    liftOnFocus = true,
                    scaleAmount = 1.03f
                )
                .clickable { showAddCatalogMainDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.08f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Añadir Catálogo",
                    tint = Color(0xFF00FF87),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AÑADIR CATÁLOGO",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // ----------------- AUXILIARY API / DIAGNOSTICS ROW -----------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .tvFocusEffect(shape = RoundedCornerShape(12.dp), liftOnFocus = true, scaleAmount = 1.04f)
                    .clickable { onOpenApiSettings() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .tvFocusEffect(shape = RoundedCornerShape(12.dp), liftOnFocus = true, scaleAmount = 1.04f)
                    .clickable { onOpenDiagnostics() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Diagnóstico & Caché Local",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ----------------- CATALOG LIST CARDS -----------------
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
                        text = "No hay catálogos multimedia instalados",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Presiona 'Añadir catálogo' para descubrir carteleras recomendadas o configurar listas custom.",
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
                    viewModel = viewModel,
                    onEdit = {
                        selectedCatalogToEdit = cat
                        showEditDialog = true
                    }
                )
            }
        }

        // ----------------- SYNC PREFERENCES SEGMENT -----------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "PREFERENCIAS DE SINCRONIZACIÓN",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
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
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp), liftOnFocus = true, scaleAmount = 1.02f)
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "automatic"),
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Sincronización Automática", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Busca actualizaciones de carteleras cada 6 horas en segundo plano.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
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
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp), liftOnFocus = true, scaleAmount = 1.02f)
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "startup"),
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Sincronizar al iniciar la Aplicación", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Actualiza intensidades de tendencias TMDB y Trakt cada vez que se abre Lumina.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            syncMode = "manual"
                            sharedPrefs.edit().putString("catalog_sync_mode", "manual").apply()
                            Toast.makeText(context, "Sincronización Manual configurada", Toast.LENGTH_SHORT).show()
                        }
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp), liftOnFocus = true, scaleAmount = 1.02f)
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (syncMode == "manual"),
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Sincronización Manual Únicamente", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Las listas permanecen estáticas hasta que interactúes con el botón de actualización.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // ----------------- MODERN FLOATING DIALOG OVERLAY (Añadir Catálogo) -----------------
    if (showAddCatalogMainDialog) {
        Dialog(
            onDismissRequest = { showAddCatalogMainDialog = false }
        ) {
            var activeTab by remember { mutableStateOf(AddCatalogTab.CURATED) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
                border = BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Dialog Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = Color(0xFF00FF87),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AÑADIR NUEVO CATÁLOGO",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = { showAddCatalogMainDialog = false },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .tvFocusEffect(shape = CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tabs = listOf(
                            AddCatalogTab.CURATED to "✨ Catálogos Curados",
                            AddCatalogTab.MDBLIST to "🔍 Comunidad MDBList",
                            AddCatalogTab.MANUAL to "📝 Configuración Manual"
                        )

                        tabs.forEach { (tab, label) ->
                            val isSelected = activeTab == tab
                            val targetBgColor = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.04f)
                            val targetTextColor = if (isSelected) Color.Black else Color.White

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(targetBgColor)
                                    .clickable { activeTab = tab }
                                    .tvFocusEffect(
                                        shape = RoundedCornerShape(10.dp),
                                        focusedBorderColor = Color.White,
                                        liftOnFocus = true,
                                        scaleAmount = 1.04f
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = targetTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Content Pane based on selected tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (activeTab) {
                            AddCatalogTab.CURATED -> {
                                PremiumCatalogSearchPane(
                                    viewModel = viewModel,
                                    onDismiss = { showAddCatalogMainDialog = false }
                                )
                            }
                            AddCatalogTab.MDBLIST -> {
                                MdbListSearchSection(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            AddCatalogTab.MANUAL -> {
                                ManualAddCatalogForm(
                                    onDismiss = { showAddCatalogMainDialog = false },
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
                                        showAddCatalogMainDialog = false
                                        Toast.makeText(context, "Catálogo '$name' agregado correctamente.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ----------------- EDIT DIALOG -----------------
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
            .height(34.dp)
            .padding(vertical = 2.dp)
            .tvFocusEffect(
                shape = RoundedCornerShape(10.dp),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = borderColor,
                liftOnFocus = true,
                scaleAmount = 1.08f
            ),
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CatalogItemCard(
    catalog: Catalog,
    viewModel: MediaViewModel,
    onEdit: () -> Unit
) {
    val context = LocalContext.current

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

    val isVertical = catalog.layoutType == "Vertical Poster Row" || catalog.layoutType == "Vertical"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .tvFocusEffect(shape = RoundedCornerShape(14.dp), liftOnFocus = true, scaleAmount = 1.01f),
        shape = RoundedCornerShape(14.dp),
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
            // Accent bar decoration
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(
                        if (catalog.isVisible) sourceColor else sourceColor.copy(alpha = 0.3f),
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header of catalog card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = catalog.name,
                                color = if (catalog.isVisible) Color.White else Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Source tag badge
                            Text(
                                text = catalog.sourceType.uppercase(),
                                color = sourceColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp,
                                modifier = Modifier
                                    .background(sourceColor.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                    .border(0.5.dp, sourceColor.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )

                            // Layout tag badge
                            Text(
                                text = if (isVertical) "VERTICAL" else "HORIZONTAL",
                                color = Color(0xFF00FF87),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                modifier = Modifier
                                    .background(Color(0xFF00FF87).copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                    .border(0.5.dp, Color(0xFF00FF87).copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }

                    // Visibility dot status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Metadata Stats section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${catalog.items.size} ítems",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.5.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = catalog.status,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.5.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Sincro: ${catalog.lastUpdated}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.5.sp
                        )
                    }
                }

                // TV-Action Row: Scrollable chips for focus
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        onClick = {
                            viewModel.syncCatalog(catalog.id)
                            Toast.makeText(context, "Sincronizando cartelera: ${catalog.name}", Toast.LENGTH_SHORT).show()
                        },
                        containerColor = Color(0xFF00E5FF).copy(alpha = 0.06f),
                        contentColor = Color(0xFF00E5FF),
                        borderColor = Color(0xFF00E5FF).copy(alpha = 0.25f)
                    )

                    // ↕ Layout Mode Toggle (No Dialog, direct horizontal/vertical toggle)
                    CompactActionButton(
                        icon = if (isVertical) Icons.Default.SwapVert else Icons.Default.SwapHoriz,
                        text = if (isVertical) "↕️ Vertical" else "↔️ Horizontal",
                        onClick = {
                            val newLayout = if (isVertical) "Horizontal Poster Row" else "Vertical Poster Row"
                            viewModel.updateCatalog(catalog.copy(layoutType = newLayout))
                            Toast.makeText(context, "Diseño cambiado a: ${if (isVertical) "Horizontal" else "Vertical"}", Toast.LENGTH_SHORT).show()
                        },
                        containerColor = Color(0xFF00FF87).copy(alpha = 0.06f),
                        contentColor = Color(0xFF00FF87),
                        borderColor = Color(0xFF00FF87).copy(alpha = 0.25f)
                    )

                    // ⬆ Subir (Move Index Up)
                    CompactActionButton(
                        icon = Icons.Default.ArrowUpward,
                        text = "⬆ Subir",
                        onClick = { viewModel.moveCatalogUp(catalog.id) },
                        containerColor = Color.White.copy(alpha = 0.03f),
                        contentColor = Color.White.copy(alpha = 0.85f),
                        borderColor = Color.White.copy(alpha = 0.1f)
                    )

                    // ⬇ Bajar (Move Index Down)
                    CompactActionButton(
                        icon = Icons.Default.ArrowDownward,
                        text = "⬇ Bajar",
                        onClick = { viewModel.moveCatalogDown(catalog.id) },
                        containerColor = Color.White.copy(alpha = 0.03f),
                        contentColor = Color.White.copy(alpha = 0.85f),
                        borderColor = Color.White.copy(alpha = 0.1f)
                    )

                    // 👁 Mostrar / Ocultar (Toggle Visibility)
                    CompactActionButton(
                        icon = if (catalog.isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        text = if (catalog.isVisible) "👁 Ocultar" else "👁 Mostrar",
                        onClick = { viewModel.updateCatalog(catalog.copy(isVisible = !catalog.isVisible)) },
                        containerColor = Color.White.copy(alpha = 0.03f),
                        contentColor = if (catalog.isVisible) Color.White.copy(alpha = 0.7f) else Color(0xFF00FF87),
                        borderColor = Color.White.copy(alpha = 0.12f)
                    )

                    // 🗑 Eliminar (Danger action)
                    CompactActionButton(
                        icon = Icons.Default.Delete,
                        text = "🗑 Eliminar",
                        onClick = {
                            viewModel.deleteCatalog(catalog.id)
                            Toast.makeText(context, "Catálogo eliminado: ${catalog.name}", Toast.LENGTH_SHORT).show()
                        },
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
    var layoutType by remember { mutableStateOf(catalog.layoutType) }

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

                // Count limits
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
                        listOf("Landscape Row", "Banner Row")
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
fun PremiumCatalogSearchPane(
    viewModel: MediaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val installedCatalogs by viewModel.catalogsStateFlow.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val premiumCatalogs = remember {
        listOf(
            DiscoverableCatalog(
                id = "premium_mcu",
                name = "Marvel Cinematic Universe (Saga Completa)",
                sourceType = "TMDB Collections",
                url = "https://lumina-api-coral.vercel.app/api/list/8254719",
                posterUrl = "https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=600",
                numItems = 45,
                description = "Todas las películas y series canon de Marvel Studios ordenadas cronológicamente por fases.",
                category = "Marvel"
            ),
            DiscoverableCatalog(
                id = "premium_marvel_timeline",
                name = "Marvel Timeline & Story Order",
                sourceType = "Trakt Lists",
                url = "https://lumina-api-coral.vercel.app/api/trakt/lists/marvel-cinematic-universe-chronological",
                posterUrl = "https://images.unsplash.com/photo-1608889175123-8ec330b86f84?q=80&w=600",
                numItems = 38,
                description = "Orden definitivo de la narrativa de Marvel incluyendo agentes de S.H.I.E.L.D, Daredevil y Disney+.",
                category = "Marvel"
            ),
            DiscoverableCatalog(
                id = "premium_marvel_anim",
                name = "Marvel Animation Essentials",
                sourceType = "MDBList",
                url = "https://lumina-api-coral.vercel.app/api/discover/movie?with_companies=420&with_genres=16",
                posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=600",
                numItems = 20,
                description = "Series icónicas de los 90s, X-Men '97, Spider-Man: TAS, What If...? y películas animadas clásicas.",
                category = "Marvel"
            ),
            DiscoverableCatalog(
                id = "premium_marvel_top",
                name = "Top Marvel Movies & Specials",
                sourceType = "Lumina",
                url = "https://lumina-api-coral.vercel.app/api/discover/movie?with_companies=420&sort_by=vote_average.desc&vote_count.gte=1000",
                posterUrl = "https://images.unsplash.com/photo-1569003339405-ea396a5a8a90?q=80&w=600",
                numItems = 15,
                description = "Las obras del universo Marvel mejor puntuadas por la crítica global de IMDb y Rotten Tomatoes.",
                category = "Marvel"
            ),
            DiscoverableCatalog(
                id = "premium_anime_trending",
                name = "Anime Trending Worldwide",
                sourceType = "Trakt",
                url = "https://lumina-api-coral.vercel.app/api/discover/tv?with_genres=16&with_original_language=ja&sort_by=popularity.desc",
                posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=600",
                numItems = 30,
                description = "Las series y películas de anime de las que todo el mundo está hablando esta temporada.",
                category = "Anime"
            ),
            DiscoverableCatalog(
                id = "premium_anime_popular",
                name = "Top Rated Anime Masterpieces",
                sourceType = "TMDB Lists",
                url = "https://lumina-api-coral.vercel.app/api/discover/tv?with_genres=16&with_original_language=ja&sort_by=vote_average.desc&vote_count.gte=100",
                posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=600",
                numItems = 40,
                description = "Colección legendaria de los mejores animes calificados históricamente (MyAnimeList & TMDB).",
                category = "Anime"
            ),
            DiscoverableCatalog(
                id = "premium_anime_new",
                name = "Nuevos Estrenos Anime",
                sourceType = "MDBList",
                url = "https://lumina-api-coral.vercel.app/api/discover/tv?with_genres=16&with_original_language=ja&sort_by=first_air_date.desc",
                posterUrl = "https://images.unsplash.com/photo-1528360983277-13d401ccd795?q=80&w=600",
                numItems = 25,
                description = "Las series más recientes que acaban de ser transmitidas y estrenadas en Japón.",
                category = "Anime"
            ),
            DiscoverableCatalog(
                id = "premium_anime_movies",
                name = "Grandes Películas de Anime",
                sourceType = "Lumina",
                url = "https://lumina-api-coral.vercel.app/api/discover/movie?with_genres=16&with_original_language=ja&sort_by=popularity.desc",
                posterUrl = "https://images.unsplash.com/photo-1541562232579-512a21360020?q=80&w=600",
                numItems = 20,
                description = "Una selección premium de películas icónicas de Studio Ghibli, Makoto Shinkai y Mamoru Hosoda.",
                category = "Anime"
            ),
            DiscoverableCatalog(
                id = "premium_starwars",
                name = "Star Wars: Saga Skywalker & Series",
                sourceType = "TMDB Lists",
                url = "https://lumina-api-coral.vercel.app/api/list/8254720",
                posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=600",
                numItems = 28,
                description = "Que la fuerza te acompañe. Películas de la saga principal, spin-offs y series oficiales de Disney+.",
                category = "Ciencia Ficción"
            ),
            DiscoverableCatalog(
                id = "premium_cyberpunk",
                name = "Sci-Fi & Cyberpunk Classics",
                sourceType = "Lumina",
                url = "https://lumina-api-coral.vercel.app/api/discover/movie?with_genres=878&sort_by=vote_average.desc&vote_count.gte=1500",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=600",
                numItems = 25,
                description = "Películas distópicas, futuristas, robótica avanzada y mundos neones de culto.",
                category = "Ciencia Ficción"
            ),
            DiscoverableCatalog(
                id = "premium_disney",
                name = "Clásicos Animados Disney & Pixar",
                sourceType = "TMDB Collections",
                url = "https://lumina-api-coral.vercel.app/api/discover/movie?with_companies=2|3|34&with_genres=16&sort_by=popularity.desc",
                posterUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?q=80&w=600",
                numItems = 35,
                description = "La magia de tu infancia. Desde Blancanieves hasta los últimos éxitos tridimensionales de Pixar.",
                category = "Familiar"
            ),
            DiscoverableCatalog(
                id = "premium_oscar",
                name = "Películas Ganadoras del Óscar",
                sourceType = "MDBList",
                url = "https://lumina-api-coral.vercel.app/api/discover/movie?sort_by=vote_average.desc&vote_count.gte=10000",
                posterUrl = "https://images.unsplash.com/photo-1598257006458-087169a1f08d?q=80&w=600",
                numItems = 45,
                description = "Todas las cintas memorables coronadas con el premio de la Academia de Cine a Mejor Película.",
                category = "Clásicos"
            ),
            DiscoverableCatalog(
                id = "premium_imdb_top250",
                name = "IMDb Top 250 de la Historia",
                sourceType = "MDBList",
                url = "https://lumina-api-coral.vercel.app/api/discover/movie?sort_by=vote_average.desc&vote_count.gte=15000",
                posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=600",
                numItems = 50,
                description = "El estándar de oro del cine mundial. Las 250 mejores películas de todos los tiempos.",
                category = "Clásicos"
            ),
            DiscoverableCatalog(
                id = "premium_horror_trends",
                name = "Terror & Thriller Spooktacular",
                sourceType = "Trakt",
                url = "https://lumina-api-coral.vercel.app/api/discover/movie?with_genres=27&sort_by=popularity.desc",
                posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600",
                numItems = 25,
                description = "Cine de horror, suspenso psicológico y monstruos espeluznantes seleccionados para mentes valientes.",
                category = "Terror"
            )
        )
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) premiumCatalogs
        else premiumCatalogs.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar catálogo curado...", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00E5FF)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.03f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.01f),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { premium ->
                val isInstalled = installedCatalogs.any { it.name.lowercase().trim() == premium.name.lowercase().trim() || it.url == premium.url }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusEffect(shape = RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isInstalled) Color(0xFF00FF87).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.07f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Compact Image preview
                        AsyncImage(
                            model = premium.posterUrl,
                            contentDescription = premium.name,
                            modifier = Modifier
                                .size(width = 50.dp, height = 70.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Content text details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = premium.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Text(
                                    text = premium.category.uppercase(),
                                    color = Color(0xFF00E5FF),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color(0xFF00E5FF).copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = premium.description,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Install action button
                        Button(
                            onClick = {
                                if (!isInstalled) {
                                    val catalog = Catalog(
                                        id = "premium_${premium.id}",
                                        name = premium.name,
                                        sourceType = when (premium.sourceType) {
                                            "Trakt", "Trakt Lists" -> "Trakt"
                                            "MDBList" -> "MDBList"
                                            "Lumina" -> "Local"
                                            else -> "TMDB"
                                        },
                                        url = premium.url,
                                        isVisible = true,
                                        showInHome = true,
                                        numItems = premium.numItems,
                                        layoutType = "Horizontal Poster Row"
                                    )
                                    viewModel.addCatalog(catalog)
                                    Toast.makeText(context, "¡Instalado correctamente en Inicio: ${premium.name}!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isInstalled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInstalled) Color(0xFF00FF87).copy(alpha = 0.12f) else Color(0xFF00E5FF),
                                contentColor = if (isInstalled) Color(0xFF00FF87) else Color.Black,
                                disabledContainerColor = Color(0xFF00FF87).copy(alpha = 0.08f),
                                disabledContentColor = Color(0xFF00FF87).copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).tvFocusEffect(shape = RoundedCornerShape(6.dp))
                        ) {
                            if (isInstalled) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Agregado", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Instalar", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManualAddCatalogForm(
    onDismiss: () -> Unit,
    onConfirm: (name: String, source: String, url: String, layoutType: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("TMDB") }
    var url by remember { mutableStateOf("") }
    var layoutType by remember { mutableStateOf("Horizontal Poster Row") }

    val isUrlValidByRegex = remember(url) {
        url.isEmpty() || url.startsWith("http://") || url.startsWith("https://")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del Catálogo (ej. Mis Recomendaciones)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        // Origen Source Selector Type
        Text("Origen de Datos", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val listSources = listOf("TMDB", "Trakt", "MDBList", "Custom", "Local", "Import")
            listSources.forEach { src ->
                val selected = (source == src)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.05f))
                        .clickable { source = src }
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp))
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

        // Layout choices
        Text("Diseño Inicial", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val choices = listOf("Horizontal Poster Row", "Vertical Poster Row")
            choices.forEach { choice ->
                val selected = (layoutType == choice)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.05f))
                        .clickable { layoutType = choice }
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = choice.uppercase(),
                        color = if (selected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // URL input field (Pegar URL / Añadir URL)
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Pegar URL del API / Endpoint (Añadir URL)") },
            isError = !isUrlValidByRegex,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        if (url.isNotEmpty()) {
            val statusColor = if (isUrlValidByRegex) Color(0xFF4CAF50) else Color(0xFFEF5350)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isUrlValidByRegex) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isUrlValidByRegex) "Formato de URL verificado." else "Formato inválido. Debe empezar con http:// o https://",
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Text("Cancelar", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (name.isNotEmpty() && isUrlValidByRegex) {
                        onConfirm(name, source, url, layoutType)
                    }
                },
                enabled = name.isNotEmpty() && isUrlValidByRegex,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87), contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).tvFocusEffect(shape = RoundedCornerShape(8.dp))
            ) {
                Text("Crear Catálogo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
