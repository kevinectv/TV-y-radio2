package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CatalogItem
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import com.example.ui.components.responsive

@Composable
fun SearchScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWideLayout = context.resources.configuration.screenWidthDp >= 600
    
    Box(modifier = modifier.fillMaxSize()) {
        if (isWideLayout) {
            SearchScreenTv(viewModel) { viewModel.selectedDetailsItem.value = it }
        } else {
            SearchScreenMobile(viewModel) { viewModel.selectedDetailsItem.value = it }
        }
    }
}

@Composable
fun SearchScreenTv(
    viewModel: MediaViewModel,
    onItemClick: (CatalogItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = viewModel.mediaSearchResults
    val isSearching = viewModel.isMediaSearching

    LaunchedEffect(query) {
        if (query.length >= 2) {
            viewModel.searchMedia(query)
        } else if (query.isEmpty()) {
            viewModel.searchMedia("")
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar Search Input
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .background(Color.White.copy(alpha = 0.02f))
                .padding(24.dp)
        ) {
            Text(
                "BUSCAR",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            )
            Text(
                "Encuentra películas y series en Lumina",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .tvFocusEffect(),
                placeholder = { Text("Título...", color = Color.White.copy(alpha = 0.3f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00E5FF)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            if (isSearching) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = Color(0xFF00E5FF),
                    trackColor = Color.Transparent
                )
            }
        }

        // Results Grid
        Box(modifier = Modifier.weight(1f).padding(24.dp)) {
            if (results.isEmpty() && !isSearching && query.isNotEmpty()) {
                EmptySearchResults(query)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { item ->
                        CatalogSearchCard(item, onItemClick)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreenMobile(
    viewModel: MediaViewModel,
    onItemClick: (CatalogItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = viewModel.mediaSearchResults
    val isSearching = viewModel.isMediaSearching

    LaunchedEffect(query) {
        if (query.length >= 2) {
            viewModel.searchMedia(query)
        } else if (query.isEmpty()) {
            viewModel.searchMedia("")
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar películas o series...", color = Color.White.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00E5FF)) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.White)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00E5FF))
            }
        }

        if (results.isEmpty() && !isSearching && query.isNotEmpty()) {
            EmptySearchResults(query)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(results) { item ->
                    SearchListRow(item, onItemClick)
                }
            }
        }
    }
}

@Composable
fun CatalogSearchCard(item: CatalogItem, onClick: (CatalogItem) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick(item) }
            .onFocusChanged { isFocused = it.isFocused }
            .tvFocusEffect(shape = RoundedCornerShape(12.dp))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.67f),
            shape = RoundedCornerShape(12.dp),
            border = if (isFocused) BorderStroke(2.dp, Color(0xFF00E5FF)) else null
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = item.title,
            color = if (isFocused) Color(0xFF00E5FF) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
        )
        Text(
            text = "${item.year} • ${item.rating}",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun SearchListRow(item: CatalogItem, onClick: (CatalogItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick(item) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.posterUrl,
            contentDescription = item.title,
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${item.year} • ${item.genre}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(item.rating, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
    }
}

@Composable
fun EmptySearchResults(query: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No se encontraron resultados para \"$query\"",
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        Text(
            "Intenta con otras palabras clave",
            color = Color.White.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}
