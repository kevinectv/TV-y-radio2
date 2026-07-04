package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.example.data.database.ProfileEntity
import com.example.ui.MediaViewModel
import com.example.ui.components.CharacterAvatar
import com.example.ui.components.tvFocusEffect
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.components.responsive
import com.example.ui.components.getResponsiveScale
import java.util.Locale

enum class ProfileScreenMode {
    SELECT,
    MANAGE,
    CREATE,
    EDIT
}

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun ProfileSelectionScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val profilesList by viewModel.profiles.collectAsState()
    var screenMode by remember { mutableStateOf(ProfileScreenMode.SELECT) }
    var selectedProfileForEdit by remember { mutableStateOf<ProfileEntity?>(null) }
    val manageButtonFocusRequester = remember { FocusRequester() }

    val configuration = LocalConfiguration.current
    val isMobile = configuration.screenWidthDp < 580

    // Custom Profile Creator/Editor Temporary States
    var tempName by remember { mutableStateOf("") }
    var tempStyle by remember { mutableStateOf("ninja") }
    var tempSkinColor by remember { mutableStateOf("#FFD1A4") }
    var tempHairColor by remember { mutableStateOf("#FFCC00") }
    var tempAccessory by remember { mutableStateOf("headband") }
    var tempExpression by remember { mutableStateOf("cool") }
    var tempProfileColor by remember { mutableStateOf("#00E5FF") }
    var tempIsKids by remember { mutableStateOf(false) }

    // Predefined colors & values for picker controls
    val categoryPresets = listOf(
        "Niños", "Niñas", "Aventura", "Fantasía", "Ciencia Ficción", 
        "Héroes", "Vikingos", "Ninja", "Astronautas", "Robots", 
        "Animales", "Dragones", "Dinosaurios", "Monstruos", "Especiales"
    )

    val avatarPresetsByCategory = mapOf(
        "Niños" to listOf("boy"),
        "Niñas" to listOf("girl"),
        "Aventura" to listOf("explorer", "pirate"),
        "Fantasía" to listOf("wizard", "fantasy"),
        "Ciencia Ficción" to listOf("futuristic", "astronaut"),
        "Héroes" to listOf("superhero"),
        "Vikingos" to listOf("viking"),
        "Ninja" to listOf("ninja"),
        "Astronautas" to listOf("astronaut"),
        "Robots" to listOf("robot"),
        "Animales" to listOf("panda"),
        "Dragones" to listOf("dragon"),
        "Monstruos" to listOf("monster"),
        "Especiales" to listOf("kids")
    )

    var selectedCategory by remember { mutableStateOf("Niños") }

    val stylePresets = avatarPresetsByCategory[selectedCategory] ?: listOf("boy")
    val colorPresets = listOf("#6200EE", "#03DAC6", "#FF0266", "#FFDE03", "#00E676", "#3D5AFE", "#FF5722", "#9C27B0", "#607D8B")
    val skinPresets = listOf("#FFD1A4", "#E0A96D", "#8C583C", "#FFF0E0", "#F5CDA2")
    val hairPresets = listOf("#FFCC00", "#3F2B1E", "#9C27B0", "#00E676", "#ECEFF1", "#E50914", "#FF007F")
    val accessoryPresets = listOf("none", "visor", "glasses", "eyepatch", "headphones", "headband")
    val expressionPresets = listOf("smile", "wink", "smirk", "cool", "determined")

    val outerBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0F10),
            Color(0xFF070708),
            Color(0xFF000000)
        )
    )

    // Helper to generate a fully randomized character
    fun randomizeAvatar() {
        tempStyle = stylePresets.random()
        tempSkinColor = skinPresets.random()
        tempHairColor = hairPresets.random()
        tempAccessory = accessoryPresets.random()
        tempExpression = expressionPresets.random()
        tempProfileColor = colorPresets.random()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(outerBackgroundBrush)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = 1000.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp)
        ) {
            // --- HEADER ---
            if (screenMode == ProfileScreenMode.SELECT || screenMode == ProfileScreenMode.MANAGE) {
                Text(
                    text = "LUMINA",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (screenMode == ProfileScreenMode.SELECT) "¿Quién está viendo?" else "Administrar Perfiles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }

            // --- ANIMATED CONTENT BY MODE ---
            AnimatedContent(
                targetState = screenMode,
                transitionSpec = {
                    fadeIn() with fadeOut()
                },
                label = "profile_screens"
            ) { mode ->
                when (mode) {
                    ProfileScreenMode.SELECT, ProfileScreenMode.MANAGE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isMobile) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    val itemsCount = profilesList.size + (if (profilesList.size < 6) 1 else 0)
                                    val rowsCount = (itemsCount + 1) / 2
                                    
                                    for (i in 0 until rowsCount) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            val firstIdx = i * 2
                                            if (firstIdx < profilesList.size) {
                                                ProfileItemView(
                                                    profile = profilesList[firstIdx],
                                                    index = firstIdx,
                                                    profilesList = profilesList,
                                                    screenMode = screenMode,
                                                    viewModel = viewModel,
                                                    manageButtonFocusRequester = manageButtonFocusRequester,
                                                    onEditProfile = { p ->
                                                        selectedProfileForEdit = p
                                                        tempName = p.name
                                                        tempStyle = p.avatarStyle
                                                        tempSkinColor = p.avatarSkinColor
                                                        tempHairColor = p.avatarHairColor
                                                        tempAccessory = p.avatarAccessory
                                                        tempExpression = p.avatarExpression
                                                        tempProfileColor = p.profileColor
                                                        tempIsKids = p.isKids
                                                        screenMode = ProfileScreenMode.EDIT
                                                    },
                                                    onSelectProfile = { p ->
                                                        viewModel.selectProfile(p)
                                                    }
                                                )
                                            } else if (firstIdx == profilesList.size && profilesList.size < 6) {
                                                AddProfileItemView(
                                                    onAddClick = {
                                                        tempName = ""
                                                        randomizeAvatar()
                                                        tempIsKids = false
                                                        screenMode = ProfileScreenMode.CREATE
                                                    },
                                                    manageButtonFocusRequester = manageButtonFocusRequester
                                                )
                                            }

                                            val secondIdx = firstIdx + 1
                                            if (secondIdx < profilesList.size) {
                                                ProfileItemView(
                                                    profile = profilesList[secondIdx],
                                                    index = secondIdx,
                                                    profilesList = profilesList,
                                                    screenMode = screenMode,
                                                    viewModel = viewModel,
                                                    manageButtonFocusRequester = manageButtonFocusRequester,
                                                    onEditProfile = { p ->
                                                        selectedProfileForEdit = p
                                                        tempName = p.name
                                                        tempStyle = p.avatarStyle
                                                        tempSkinColor = p.avatarSkinColor
                                                        tempHairColor = p.avatarHairColor
                                                        tempAccessory = p.avatarAccessory
                                                        tempExpression = p.avatarExpression
                                                        tempProfileColor = p.profileColor
                                                        tempIsKids = p.isKids
                                                        screenMode = ProfileScreenMode.EDIT
                                                    },
                                                    onSelectProfile = { p ->
                                                        viewModel.selectProfile(p)
                                                    }
                                                )
                                            } else if (secondIdx == profilesList.size && profilesList.size < 6) {
                                                AddProfileItemView(
                                                    onAddClick = {
                                                        tempName = ""
                                                        randomizeAvatar()
                                                        tempIsKids = false
                                                        screenMode = ProfileScreenMode.CREATE
                                                    },
                                                    manageButtonFocusRequester = manageButtonFocusRequester
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp)
                                        .padding(vertical = 24.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    profilesList.forEachIndexed { index, profile ->
                                        ProfileItemView(
                                            profile = profile,
                                            index = index,
                                            profilesList = profilesList,
                                            screenMode = screenMode,
                                            viewModel = viewModel,
                                            manageButtonFocusRequester = manageButtonFocusRequester,
                                            onEditProfile = { p ->
                                                selectedProfileForEdit = p
                                                tempName = p.name
                                                tempStyle = p.avatarStyle
                                                tempSkinColor = p.avatarSkinColor
                                                tempHairColor = p.avatarHairColor
                                                tempAccessory = p.avatarAccessory
                                                tempExpression = p.avatarExpression
                                                tempProfileColor = p.profileColor
                                                tempIsKids = p.isKids
                                                screenMode = ProfileScreenMode.EDIT
                                            },
                                            onSelectProfile = { p ->
                                                viewModel.selectProfile(p)
                                            }
                                        )
                                    }
                                    if (profilesList.size < 6) {
                                        AddProfileItemView(
                                            onAddClick = {
                                                tempName = ""
                                                randomizeAvatar()
                                                tempIsKids = false
                                                screenMode = ProfileScreenMode.CREATE
                                            },
                                            manageButtonFocusRequester = manageButtonFocusRequester
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(48.dp))

                            // Action Menu button at the bottom of standard select
                            val manageInteractionSource = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .focusRequester(manageButtonFocusRequester)
                                    .tvFocusEffect(
                                        shape = RoundedCornerShape(12.dp),
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                        borderWidth = 1.2.dp,
                                        scaleAmount = 1.15f,
                                        liftOnFocus = true,
                                        interactionSource = manageInteractionSource
                                    )
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = manageInteractionSource,
                                        indication = LocalIndication.current,
                                        onClick = {
                                            screenMode = if (screenMode == ProfileScreenMode.SELECT) {
                                                ProfileScreenMode.MANAGE
                                            } else {
                                                ProfileScreenMode.SELECT
                                            }
                                        }
                                    )
                                    .semantics { testTag = "manage_profiles_button" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (screenMode == ProfileScreenMode.SELECT) "Administrar Perfiles" else "Listo (Guardar)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    ProfileScreenMode.CREATE, ProfileScreenMode.EDIT -> {
                        // --- EDIT / CREATE PROFILE FORM SCREEN ---
                        // Reducing size and adding premium glass effect
                        Box(
                            modifier = Modifier
                                .widthIn(max = 820.dp)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Close button at top right
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                                    IconButton(
                                        onClick = { screenMode = ProfileScreenMode.SELECT },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                            .tvFocusEffect(shape = CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Text(
                                    text = if (screenMode == ProfileScreenMode.CREATE) "Crear perfil" else "Editar perfil",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // PANEL IZQUIERDO: Avatar, Botón Imagen, Nombre
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(20.dp),
                                        modifier = Modifier.width(220.dp)
                                    ) {
                                        // Avatar Preview
                                        Box(
                                            modifier = Modifier
                                                .size(180.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .border(
                                                    width = 4.dp,
                                                    color = Color(tempProfileColor.toColorIntOrFallback(0xFF6200EE.toInt())),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .background(Color.White.copy(alpha = 0.05f))
                                        ) {
                                            CharacterAvatar(
                                                style = tempStyle,
                                                skinColorHex = tempSkinColor,
                                                hairColorHex = tempHairColor,
                                                accessory = tempAccessory,
                                                expression = tempExpression,
                                                profileColorHex = tempProfileColor,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            
                                            // Edit icon overlay
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(8.dp)
                                                    .size(24.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        // Change image button
                                        Button(
                                            onClick = { randomizeAvatar() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .tvFocusEffect(shape = RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Cambiar imagen", fontSize = 13.sp)
                                        }

                                        // Name Field with character counter
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text("Nombre del perfil", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                                            OutlinedTextField(
                                                value = tempName,
                                                onValueChange = { if (it.length <= 20) tempName = it },
                                                placeholder = { Text("Mi perfil", color = Color.White.copy(alpha = 0.3f)) },
                                                modifier = Modifier.fillMaxWidth().tvFocusEffect(shape = RoundedCornerShape(8.dp)),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = Color(tempProfileColor.toColorIntOrFallback(0xFF6200EE.toInt())),
                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                                                ),
                                                singleLine = true
                                            )
                                            Text(
                                                text = "${tempName.length}/20",
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 11.sp,
                                                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                                            )
                                        }
                                    }

                                    // PANEL DERECHO: Avatares, Colores, Categorías, Opciones
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        ProfileFormFields(
                                            tempName = tempName,
                                            onNameChange = { tempName = it },
                                            tempProfileColor = tempProfileColor,
                                            onProfileColorChange = { tempProfileColor = it },
                                            tempStyle = tempStyle,
                                            onStyleChange = { tempStyle = it },
                                            tempHairColor = tempHairColor,
                                            onHairColorChange = { tempHairColor = it },
                                            tempIsKids = tempIsKids,
                                            onIsKidsChange = { tempIsKids = it },
                                            colorPresets = colorPresets,
                                            stylePresets = stylePresets,
                                            hairPresets = hairPresets,
                                            categoryPresets = categoryPresets,
                                            selectedCategory = selectedCategory,
                                            onCategoryChange = { selectedCategory = it },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Bottom Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Delete profile (Only in EDIT mode)
                                    if (screenMode == ProfileScreenMode.EDIT && selectedProfileForEdit != null) {
                                        TextButton(
                                            onClick = {
                                                viewModel.deleteProfile(selectedProfileForEdit!!.id)
                                                screenMode = ProfileScreenMode.SELECT
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350)),
                                            modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Eliminar perfil", fontSize = 14.sp)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Cancel button
                                        TextButton(
                                            onClick = { screenMode = ProfileScreenMode.SELECT },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .padding(end = 16.dp)
                                                .tvFocusEffect(shape = RoundedCornerShape(12.dp))
                                        ) {
                                            Text("Cancelar", color = Color.White, fontSize = 16.sp)
                                        }

                                        // Save Button
                                        val buttonColor = Color(tempProfileColor.toColorIntOrFallback(0xFF6200EE.toInt()))
                                        Button(
                                            onClick = {
                                                if (tempName.trim().isNotEmpty()) {
                                                    if (screenMode == ProfileScreenMode.CREATE) {
                                                        viewModel.createProfile(
                                                            name = tempName,
                                                            avatarStyle = tempStyle,
                                                            skinColor = tempSkinColor,
                                                            hairColor = tempHairColor,
                                                            accessory = tempAccessory,
                                                            expression = tempExpression,
                                                            profileColor = tempProfileColor,
                                                            isKids = tempIsKids
                                                        )
                                                    } else if (screenMode == ProfileScreenMode.EDIT && selectedProfileForEdit != null) {
                                                        viewModel.updateProfile(
                                                            id = selectedProfileForEdit!!.id,
                                                            name = tempName,
                                                            avatarStyle = tempStyle,
                                                            skinColor = tempSkinColor,
                                                            hairColor = tempHairColor,
                                                            accessory = tempAccessory,
                                                            expression = tempExpression,
                                                            profileColor = tempProfileColor,
                                                            isKids = tempIsKids
                                                        )
                                                    }
                                                    screenMode = ProfileScreenMode.SELECT
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .height(54.dp)
                                                .widthIn(min = 180.dp)
                                                .tvFocusEffect(shape = RoundedCornerShape(12.dp), scaleAmount = 1.05f)
                                                .semantics { testTag = "save_profile_button" }
                                        ) {
                                            Text("Guardar perfil", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileFormFields(
    tempName: String,
    onNameChange: (String) -> Unit,
    tempProfileColor: String,
    onProfileColorChange: (String) -> Unit,
    tempStyle: String,
    onStyleChange: (String) -> Unit,
    tempHairColor: String,
    onHairColorChange: (String) -> Unit,
    tempIsKids: Boolean,
    onIsKidsChange: (Boolean) -> Unit,
    colorPresets: List<String>,
    stylePresets: List<String>,
    hairPresets: List<String>,
    categoryPresets: List<String>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Character Style Picker (Avatares)
        Column {
            Text("Selecciona un avatar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(stylePresets) { styleName ->
                    val isSelected = tempStyle == styleName
                    val borderColor by animateColorAsState(
                        if (isSelected) Color(tempProfileColor.toColorIntOrFallback(0xFF6200EE.toInt())) else Color.Transparent
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) borderColor else Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .tvFocusEffect(shape = RoundedCornerShape(12.dp))
                            .clickable { onStyleChange(styleName) }
                    ) {
                        CharacterAvatar(
                            style = styleName,
                            skinColorHex = "#FFD1A4",
                            hairColorHex = "#FFCC00",
                            accessory = "none",
                            expression = "cool",
                            profileColorHex = tempProfileColor,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(16.dp)
                                    .background(borderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
        }

        // Profile Theme Highlight Color Row
        Column {
            Text("Selecciona un color", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(colorPresets) { colorHex ->
                    val isSelected = tempProfileColor == colorHex
                    val borderSize by animateDpAsState(if (isSelected) 3.dp else 0.dp)
                    
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex.toColorIntOrFallback(0xFFFFFFFF.toInt())))
                            .border(
                                width = borderSize,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .tvFocusEffect(shape = CircleShape)
                            .clickable { onProfileColorChange(colorHex) }
                    )
                }
            }
        }

        // Category Picker (Capsules)
        Column {
            Text("Selecciona una categoría", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categoryPresets) { category ->
                    val isSelected = selectedCategory == category
                    val bgColor by animateColorAsState(if (isSelected) Color(tempProfileColor.toColorIntOrFallback(0xFF6200EE.toInt())).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                    val borderColor by animateColorAsState(if (isSelected) Color(tempProfileColor.toColorIntOrFallback(0xFF6200EE.toInt())) else Color.Transparent)
                    
                    Surface(
                        onClick = { onCategoryChange(category) },
                        shape = RoundedCornerShape(20.dp),
                        color = bgColor,
                        border = BorderStroke(1.dp, if (isSelected) borderColor else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.tvFocusEffect(shape = RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Options Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Opciones del perfil", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .clickable { onIsKidsChange(!tempIsKids) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.06f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Perfil infantil (recomendado)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("Solo se mostrarán contenidos aptos para niños.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }
                Switch(
                    checked = tempIsKids,
                    onCheckedChange = onIsKidsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(tempProfileColor.toColorIntOrFallback(0xFF6200EE.toInt())),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

// Simple color helper
private fun String.toColorIntOrFallback(fallback: Int): Int {
    return try {
        android.graphics.Color.parseColor(this)
    } catch (e: Exception) {
        fallback
    }
}

// Swap positions helper for profile reordering
private fun reorderProfiles(viewModel: MediaViewModel, list: List<ProfileEntity>, fromIndex: Int, toIndex: Int) {
    if (fromIndex in list.indices && toIndex in list.indices) {
        val mutable = list.toMutableList()
        val item1 = mutable[fromIndex]
        val item2 = mutable[toIndex]
        // Swapping fields can be done by changing IDs or adding order indexes.
        // Let's swap the parameters (name, style, configuration) except ID to physically swap them cleanly on the UI!
        val tempName = item1.name
        val tempStyle = item1.avatarStyle
        val tempSkin = item1.avatarSkinColor
        val tempHair = item1.avatarHairColor
        val tempAcc = item1.avatarAccessory
        val tempExp = item1.avatarExpression
        val tempCol = item1.profileColor
        val tempKids = item1.isKids
        val tempLang = item1.languagePref
        val tempDark = item1.interfacePref

        // Apply item2 data into item1
        viewModel.updateProfile(
            id = item1.id,
            name = item2.name,
            avatarStyle = item2.avatarStyle,
            skinColor = item2.avatarSkinColor,
            hairColor = item2.avatarHairColor,
            accessory = item2.avatarAccessory,
            expression = item2.avatarExpression,
            profileColor = item2.profileColor,
            isKids = item2.isKids
        )

        // Apply item1 data into item2
        viewModel.updateProfile(
            id = item2.id,
            name = tempName,
            avatarStyle = tempStyle,
            skinColor = tempSkin,
            hairColor = tempHair,
            accessory = tempAcc,
            expression = tempExp,
            profileColor = tempCol,
            isKids = tempKids
        )
    }
}

@Composable
private fun ProfileItemView(
    profile: ProfileEntity,
    index: Int,
    profilesList: List<ProfileEntity>,
    screenMode: ProfileScreenMode,
    viewModel: MediaViewModel,
    manageButtonFocusRequester: FocusRequester,
    onEditProfile: (ProfileEntity) -> Unit,
    onSelectProfile: (ProfileEntity) -> Unit
) {
    val isCurrentActive = viewModel.activeProfile?.id == profile.id
    val focusBorderColor = remember(profile.profileColor) {
        try {
            Color(android.graphics.Color.parseColor(profile.profileColor))
        } catch (e: Exception) {
            Color.White
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp.responsive())
            .padding(vertical = 12.dp)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(132.dp.responsive())
                .focusProperties {
                    down = manageButtonFocusRequester
                }
                .clip(RoundedCornerShape(16.dp))
                .tvFocusEffect(
                    shape = RoundedCornerShape(16.dp),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = if (isCurrentActive) focusBorderColor else Color.White.copy(alpha = 0.15f),
                    borderWidth = if (isCurrentActive) 3.dp else 1.5.dp,
                    scaleAmount = 1.15f,
                    liftOnFocus = true,
                    interactionSource = interactionSource
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = {
                        if (screenMode == ProfileScreenMode.SELECT) {
                            onSelectProfile(profile)
                        } else {
                            onEditProfile(profile)
                        }
                    }
                )
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

            if (screenMode == ProfileScreenMode.MANAGE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Perfil",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (screenMode == ProfileScreenMode.MANAGE) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (index > 0) {
                    IconButton(
                        onClick = {
                            reorderProfiles(viewModel, profilesList, index, index - 1)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mover izquierda", tint = Color.LightGray)
                    }
                }
                Text(
                    text = profile.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (index < profilesList.size - 1) {
                    IconButton(
                        onClick = {
                            reorderProfiles(viewModel, profilesList, index, index + 1)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Mover derecha", tint = Color.LightGray)
                    }
                }
            }
        } else {
            Text(
                text = profile.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentActive) focusBorderColor else Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (profile.isKids) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Infantil",
                fontSize = 10.sp,
                color = Color(0xFFFFB300),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(Color(0xFFFFB300).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun AddProfileItemView(
    onAddClick: () -> Unit,
    manageButtonFocusRequester: FocusRequester
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp.responsive())
            .padding(vertical = 12.dp)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(132.dp.responsive())
                .focusProperties {
                    down = manageButtonFocusRequester
                }
                .clip(RoundedCornerShape(16.dp))
                .tvFocusEffect(
                    shape = RoundedCornerShape(16.dp),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    borderWidth = 1.5.dp,
                    scaleAmount = 1.15f,
                    liftOnFocus = true,
                    interactionSource = interactionSource
                )
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onAddClick
                )
                .semantics { testTag = "add_profile_card" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Profile Image",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Añadir Perfil",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
