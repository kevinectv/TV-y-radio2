package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MdbListService
import com.example.data.TmdbService
import com.example.data.TraktService
import kotlinx.coroutines.launch
import com.example.ui.components.tvFocusEffect
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("lumina_prefs", Context.MODE_PRIVATE) }

    var tmdbKey by remember { mutableStateOf(prefs.getString("tmdb_api_key", "") ?: "") }
    var mdblistKey by remember { mutableStateOf(prefs.getString("mdblist_api_key", "") ?: "") }
    var traktKey by remember { mutableStateOf(prefs.getString("trakt_api_key", "") ?: "") }
    var traktSecret by remember { mutableStateOf(prefs.getString("trakt_api_secret", "") ?: "") }

    val tmdbService = remember { TmdbService(context) }
    val mdbListService = remember { MdbListService(context) }
    val traktService = remember { TraktService(context) }

    var tmdbStatus by remember { mutableStateOf("Desconocido") }
    var mdbStatus by remember { mutableStateOf("Desconocido") }
    var traktStatus by remember { mutableStateOf("Desconocido") }

    var isTestingTmdb by remember { mutableStateOf(false) }
    var isTestingMdb by remember { mutableStateOf(false) }
    var isTestingTrakt by remember { mutableStateOf(false) }

    var showSavedMessage by remember { mutableStateOf(false) }

    // Init statuses on load if keys exist
    LaunchedEffect(Unit) {
        if (tmdbKey.isNotEmpty()) {
            val res = tmdbService.testConnection(tmdbKey)
            tmdbStatus = if (res.first) "🟢 Conectado" else "🔴 Error de Conexión"
        } else {
            tmdbStatus = "🟡 Falta API Key"
        }
        
        if (mdblistKey.isNotEmpty()) {
            val res = mdbListService.testConnection(mdblistKey)
            mdbStatus = if (res.first) "🟢 Conectado" else "🔴 Error de Conexión"
        } else {
            mdbStatus = "🟡 Falta API Key"
        }
        
        if (traktKey.isNotEmpty()) {
            val res = traktService.testConnection(traktKey)
            traktStatus = if (res.first) "🟢 Conectado" else "🔴 Error de Conexión"
        } else {
            traktStatus = "🟡 Falta Client ID"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes de APIs", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(8.dp))) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configura tus APIs para alimentar automáticamente el Home de Lumina con metadatos de calidad premium, trailers, repartos y pósteres.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Section 1: TMDB API
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("The Movie Database (TMDB)", fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 15.sp)
                    
                    OutlinedTextField(
                        value = tmdbKey,
                        onValueChange = { tmdbKey = it },
                        label = { Text("TMDB API Key (v3)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado: $tmdbStatus",
                            color = if (tmdbStatus.contains("🟢")) Color(0xFF00E676) else if (tmdbStatus.contains("🔴")) Color(0xFFFF5252) else if (tmdbStatus.contains("🟡")) Color(0xFFFFD600) else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        Button(
                            onClick = {
                                isTestingTmdb = true
                                coroutineScope.launch {
                                    val res = tmdbService.testConnection(tmdbKey)
                                    tmdbStatus = if (res.first) "🟢 Conectado" else "🔴 Error de Conexión"
                                    isTestingTmdb = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.12f), contentColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(6.dp),
                            enabled = !isTestingTmdb,
                            modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(6.dp))
                        ) {
                            if (isTestingTmdb) {
                                CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Probar Conexión", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 2: MDBList API
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MDBList", fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 15.sp)
                    
                    OutlinedTextField(
                        value = mdblistKey,
                        onValueChange = { mdblistKey = it },
                        label = { Text("MDBList API Key") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado: $mdbStatus",
                            color = if (mdbStatus.contains("🟢")) Color(0xFF00E676) else if (mdbStatus.contains("🔴")) Color(0xFFFF5252) else if (mdbStatus.contains("🟡")) Color(0xFFFFD600) else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        Button(
                            onClick = {
                                isTestingMdb = true
                                coroutineScope.launch {
                                    val res = mdbListService.testConnection(mdblistKey)
                                    mdbStatus = if (res.first) "🟢 Conectado" else "🔴 Error de Conexión"
                                    isTestingMdb = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.12f), contentColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(6.dp),
                            enabled = !isTestingMdb,
                            modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(6.dp))
                        ) {
                            if (isTestingMdb) {
                                CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Probar Conexión", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 3: Trakt API
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Trakt.tv", fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 15.sp)
                    
                    OutlinedTextField(
                        value = traktKey,
                        onValueChange = { traktKey = it },
                        label = { Text("Trakt Client ID") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = traktSecret,
                        onValueChange = { traktSecret = it },
                        label = { Text("Trakt Client Secret (Opcional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado: $traktStatus",
                            color = if (traktStatus.contains("🟢")) Color(0xFF00E676) else if (traktStatus.contains("🔴")) Color(0xFFFF5252) else if (traktStatus.contains("🟡")) Color(0xFFFFD600) else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        Button(
                            onClick = {
                                isTestingTrakt = true
                                coroutineScope.launch {
                                    val res = traktService.testConnection(traktKey)
                                    traktStatus = if (res.first) "🟢 Conectado" else "🔴 Error de Conexión"
                                    isTestingTrakt = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.12f), contentColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(6.dp),
                            enabled = !isTestingTrakt,
                            modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(6.dp))
                        ) {
                            if (isTestingTrakt) {
                                CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Probar Conexión", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showSavedMessage) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Guardado", tint = Color.White)
                        Text("Configuración guardada correctamente.", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = {
                    prefs.edit()
                        .putString("tmdb_api_key", tmdbKey.trim())
                        .putString("mdblist_api_key", mdblistKey.trim())
                        .putString("trakt_api_key", traktKey.trim())
                        .putString("trakt_api_secret", traktSecret.trim())
                        .apply()
                    showSavedMessage = true
                    coroutineScope.launch {
                        // Keep message visible for 2.5 seconds
                        kotlinx.coroutines.delay(2500)
                        showSavedMessage = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Guardar Configuración", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
