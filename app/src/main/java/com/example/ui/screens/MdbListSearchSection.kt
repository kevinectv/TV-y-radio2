package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Catalog
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import com.example.data.model.MdbListSearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MdbListSearchSection(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val isSearching = viewModel.isMdbListSearching
    val searchResults = viewModel.mdbListSearchResults

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "MDBList",
                    tint = Color(0xFFFF2E93),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BUSCAR CATÁLOGOS MDBLIST",
                    color = Color(0xFFFF2E93),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Busca listas públicas creadas por la comunidad de MDBList (Marvel, Anime, Tops).",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ej: Marvel", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                        focusedBorderColor = Color(0xFFFF2E93),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                )

                Button(
                    onClick = { 
                        if (searchQuery.isNotBlank()) {
                            viewModel.searchMdbLists(searchQuery)
                        } else {
                            Toast.makeText(context, "Ingresa un término de búsqueda", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isSearching,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF2E93),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFFF2E93).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(8.dp))
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Buscar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Resultados (${searchResults.size})", 
                        color = Color.White.copy(alpha = 0.5f), 
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(searchResults) { result ->
                            MdbListResultItem(
                                result = result,
                                onAdd = {
                                    val catalog = Catalog(
                                        id = "mdblist_${result.id}",
                                        name = result.name,
                                        sourceType = "MDBList",
                                        url = result.url,
                                        isVisible = true,
                                        showInHome = true,
                                        numItems = if (result.itemCount > 0) result.itemCount else 50,
                                        layoutType = "Horizontal Poster Row"
                                    )
                                    viewModel.addCatalog(catalog)
                                    // Also run the sync process
                                    viewModel.syncCatalog(catalog.id)
                                    Toast.makeText(context, "Catálogo añadido y sincronizando...", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                }
            } else if (viewModel.mdbListSearchQuery.isNotBlank() && !isSearching) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No se encontraron resultados. Verifica tu API Key o prueba otro término.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun MdbListResultItem(
    result: MdbListSearchResult,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${result.name} (${result.itemCount} títulos)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = result.description.ifBlank { "Sin descripción" },
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FF87).copy(alpha = 0.15f),
                contentColor = Color(0xFF00FF87)
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(30.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Agregar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
