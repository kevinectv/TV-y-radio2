package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CatalogCacheManager
import com.example.data.CatalogSyncManager
import com.example.ui.MediaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogDiagnosticsScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncManager = remember { CatalogSyncManager.getInstance(context) }
    val cacheManager = remember { CatalogCacheManager(context) }

    val diagnosticsState by syncManager.diagnostics.collectAsState()
    var cacheSizeStr by remember { mutableStateOf(cacheManager.getCacheSizeString()) }
    var isSyncing by remember { mutableStateOf(false) }

    // Update status on load
    LaunchedEffect(Unit) {
        syncManager.runDiagnosticCheck()
        cacheSizeStr = cacheManager.getCacheSizeString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico de Catálogos", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F1524)
                )
            )
        },
        containerColor = Color(0xFF090D16)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Connection Stats
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Estado de Servicios", fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 14.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TMDB API:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(
                            text = diagnosticsState.tmdbStatus,
                            fontWeight = FontWeight.SemiBold,
                            color = if (diagnosticsState.tmdbStatus.contains("🟢")) Color(0xFF00E676) else Color(0xFFFF5252),
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MDBList API:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(
                            text = diagnosticsState.mdblistStatus,
                            fontWeight = FontWeight.SemiBold,
                            color = if (diagnosticsState.mdblistStatus.contains("🟢")) Color(0xFF00E676) else Color(0xFFFF5252),
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Trakt API:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(
                            text = diagnosticsState.traktStatus,
                            fontWeight = FontWeight.SemiBold,
                            color = if (diagnosticsState.traktStatus.contains("🟢")) Color(0xFF00E676) else Color(0xFFFF5252),
                            fontSize = 13.sp
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Última Sincronización:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(
                            text = diagnosticsState.lastSyncTime,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Espacio en Caché:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(
                            text = cacheSizeStr,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Quick Actions Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isSyncing = true
                        coroutineScope.launch {
                            viewModel.catalogRepository?.let { repo ->
                                syncManager.performFullSync(repo)
                            }
                            cacheSizeStr = cacheManager.getCacheSizeString()
                            isSyncing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sincronizar", modifier = Modifier.size(14.dp))
                            Text("Sincronizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.catalogRepository?.let { repo ->
                                cacheManager.clearCache(repo)
                            }
                            cacheSizeStr = cacheManager.getCacheSizeString()
                            syncManager.addLog("Caché local limpiada con éxito.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Limpiar Caché", modifier = Modifier.size(14.dp))
                        Text("Limpiar Caché", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section: Detected Errors Alert
            if (diagnosticsState.errorsDetected.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Errores", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            Text("Errores Detectados", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252), fontSize = 13.sp)
                        }
                        diagnosticsState.errorsDetected.take(3).forEach { err ->
                            Text(err, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Section: Execution & Sincronización Logs
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Historial de Ejecución y Depuración", fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (diagnosticsState.logs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay registros de depuración aún.", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(diagnosticsState.logs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("🟢") || log.contains("exitosamente") || log.contains("éxito")) Color(0xFF00E676)
                                            else if (log.contains("🔴") || log.contains("Error") || log.contains("Fallo")) Color(0xFFFF5252)
                                            else Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
