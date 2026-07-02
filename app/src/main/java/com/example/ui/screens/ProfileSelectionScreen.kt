package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
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
    val stylePresets = listOf("ninja", "explorer", "futuristic", "wizard", "pirate", "superhero", "urban", "fantasy", "youth", "elegant")
    val colorPresets = listOf("#00E5FF", "#FF4081", "#E040FB", "#4CAF50", "#FFEB3B", "#FF5722", "#2979FF")
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
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (screenMode == ProfileScreenMode.SELECT) "¿Quién está viendo?" else "Administrar Perfiles",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
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
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val itemsCount = profilesList.size + (if (profilesList.size < 6) 1 else 0)
                                    val rowsCount = (itemsCount + 1) / 2
                                    
                                    for (i in 0 until rowsCount) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                                        .padding(vertical = 16.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(24.dp),
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
                            OutlinedButton(
                                onClick = {
                                    screenMode = if (screenMode == ProfileScreenMode.SELECT) {
                                        ProfileScreenMode.MANAGE
                                    } else {
                                        ProfileScreenMode.SELECT
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .focusRequester(manageButtonFocusRequester)
                                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    .semantics { testTag = "manage_profiles_button" }
                            ) {
                                Text(
                                    text = if (screenMode == ProfileScreenMode.SELECT) "Administrar Perfiles" else "Listo (Guardar)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    ProfileScreenMode.CREATE, ProfileScreenMode.EDIT -> {
                        // --- EDIT / CREATE PROFILE FORM SCREEN ---
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, Color(tempProfileColor.toColorIntOrFallback(0xFFFFFFFF.toInt())).copy(alpha = 0.25f)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF161618).copy(alpha = 0.95f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (screenMode == ProfileScreenMode.CREATE) "Crear Perfil" else "Editar Perfil",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )

                                if (isMobile) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Left: Gorgeous Interactive Avatar Preview + Randomize button
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.width(160.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(110.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .border(3.dp, Color(tempProfileColor.toColorIntOrFallback(0xFF00E5FF.toInt())), RoundedCornerShape(20.dp))
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
                                            }

                                            // Randomizer button
                                            Button(
                                                onClick = { randomizeAvatar() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = "Aleatorio", modifier = Modifier.size(16.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Aleatorio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }

                                        // Right: Fields control form
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
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Left: Gorgeous Interactive Avatar Preview + Randomize button
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.width(160.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(130.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .border(3.dp, Color(tempProfileColor.toColorIntOrFallback(0xFF00E5FF.toInt())), RoundedCornerShape(20.dp))
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
                                            }

                                            // Randomizer button
                                            Button(
                                                onClick = { randomizeAvatar() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = "Aleatorio", modifier = Modifier.size(16.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Aleatorio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }

                                        // Right: Fields control form
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
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Save / Cancel Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Delete profile (Only in EDIT mode)
                                    if (screenMode == ProfileScreenMode.EDIT && selectedProfileForEdit != null) {
                                        Button(
                                            onClick = {
                                                viewModel.deleteProfile(selectedProfileForEdit!!.id)
                                                screenMode = ProfileScreenMode.SELECT
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                                .semantics { testTag = "delete_profile_button" }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Eliminar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Cancel button
                                    OutlinedButton(
                                        onClick = { screenMode = ProfileScreenMode.SELECT },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Text("Cancelar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Save Button
                                    val buttonColor = Color(tempProfileColor.toColorIntOrFallback(0xFF00E5FF.toInt()))
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
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                            .semantics { testTag = "save_profile_button" }
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Guardar", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name Field
        OutlinedTextField(
            value = tempName,
            onValueChange = onNameChange,
            label = { Text("Nombre del Perfil", color = Color.White.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "profile_name_input" },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(tempProfileColor.toColorIntOrFallback(0xFF00E5FF.toInt())),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Profile Theme Highlight Color Row
        Column {
            Text("Color de Perfil", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(colorPresets) { colorHex ->
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex.toColorIntOrFallback(0xFFFFFFFF.toInt())))
                            .border(
                                width = if (tempProfileColor == colorHex) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { onProfileColorChange(colorHex) }
                    )
                }
            }
        }

        // Character Style Picker
        Column {
            Text("Clase de Personaje", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(stylePresets) { styleName ->
                    val isSelected = tempStyle == styleName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(tempProfileColor.toColorIntOrFallback(0xFF00E5FF.toInt())).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(tempProfileColor.toColorIntOrFallback(0xFF00E5FF.toInt())) else Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onStyleChange(styleName) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = styleName.capitalize(Locale.ROOT),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Hair Color Picker
        Column {
            Text("Color de Cabello", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(hairPresets) { hairHex ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(hairHex.toColorIntOrFallback(0xFFFFFFFF.toInt())))
                            .border(
                                width = if (tempHairColor == hairHex) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { onHairColorChange(hairHex) }
                    )
                }
            }
        }

        // Kids Toggles (kids/normal type)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                .clickable { onIsKidsChange(!tempIsKids) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Kids Mode", tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Perfil Infantil (Kids)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Separar programación solo apta para niños", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }
            Switch(
                checked = tempIsKids,
                onCheckedChange = onIsKidsChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFC107),
                    checkedTrackColor = Color(0xFFFFC107).copy(alpha = 0.4f)
                )
            )
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
            .width(128.dp.responsive())
            .padding(vertical = 12.dp)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(112.dp.responsive())
                .tvFocusEffect(
                    shape = RoundedCornerShape(16.dp),
                    focusedBorderColor = Color(0xCCECEFF8),
                    unfocusedBorderColor = if (isCurrentActive) focusBorderColor else Color.White.copy(alpha = 0.15f),
                    borderWidth = if (isCurrentActive) 3.dp else 1.5.dp,
                    scaleAmount = 1.08f,
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
                .clip(RoundedCornerShape(16.dp))
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
                fontSize = 14.sp,
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
            .width(128.dp.responsive())
            .padding(vertical = 12.dp)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(112.dp.responsive())
                .tvFocusEffect(
                    shape = RoundedCornerShape(16.dp),
                    focusedBorderColor = Color(0xCCECEFF8),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    borderWidth = 1.5.dp,
                    scaleAmount = 1.08f,
                    interactionSource = interactionSource
                )
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onAddClick
                )
                .clip(RoundedCornerShape(16.dp))
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
