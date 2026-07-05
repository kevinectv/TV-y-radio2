package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MdbListService
import com.example.data.TmdbService
import com.example.data.TraktService
import com.example.data.util.ApiConfig
import kotlinx.coroutines.launch
import com.example.ui.components.tvFocusEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val tmdbService = remember { TmdbService(context) }
    val mdbListService = remember { MdbListService(context) }
    val traktService = remember { TraktService(context) }

    var tmdbStatus by remember { mutableStateOf("Verificando...") }
    var mdbStatus by remember { mutableStateOf("Verificando...") }
    var traktStatus by remember { mutableStateOf("Verificando...") }

    var isRefreshing by remember { mutableStateOf(false) }

    fun refreshStatuses() {
        isRefreshing = true
        coroutineScope.launch {
            val tmdbRes = tmdbService.testConnection()
            tmdbStatus = if (tmdbRes.first) "🟢 Conectado" else "🔴 Error: ${tmdbRes.second}"
            
            val mdbRes = mdbListService.testConnection()
            mdbStatus = if (mdbRes.first) "🟢 Conectado" else "🔴 Error: ${mdbRes.second}"
            
            val traktRes = traktService.testConnection()
            traktStatus = if (traktRes.first) "🟢 Conectado" else "🔴 Error: ${traktRes.second}"
            
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        refreshStatuses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de Servicios", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(8.dp))) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refreshStatuses() },
                        enabled = !isRefreshing,
                        modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(8.dp))
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                        }
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00E5FF))
                    Text(
                        text = "Esta sección es informativa para desarrolladores. Las APIs están configuradas internamente y no requieren intervención del usuario.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Text(
                text = "Conexiones del Sistema",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            ServiceStatusItem(
                name = "The Movie Database (TMDB)",
                status = tmdbStatus,
                description = "Provee posters, backdrops, trailers y metadatos extendidos."
            )

            ServiceStatusItem(
                name = "MDBList",
                status = mdbStatus,
                description = "Provee catálogos dinámicos y búsqueda de listas premium."
            )

            ServiceStatusItem(
                name = "Trakt.tv",
                status = traktStatus,
                description = "Provee sincronización de listas y metadatos de usuario (opcional)."
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Versión de Configuración: 2.1 (Interna)",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ServiceStatusItem(
    name: String,
    status: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2F)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = name, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 15.sp)
            Text(
                text = status,
                color = when {
                    status.contains("🟢") -> Color(0xFF00E676)
                    status.contains("🔴") -> Color(0xFFFF5252)
                    else -> Color.White.copy(alpha = 0.5f)
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
