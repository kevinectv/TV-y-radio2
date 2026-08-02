package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.example.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTvOrWide = configuration.screenWidthDp >= 580

    var startAnimations by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimations = true
        // High-end fast load duration: 650ms to ensure rapid but elegant pre-warming
        delay(650)
        onSplashFinished()
    }

    // Gorgeous organic scale animation from 0.85f to 1.0f
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimations) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 450, easing = EaseOutCubic),
        label = "logo_scale"
    )

    // Fluid fast alpha fade-in
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "logo_fade"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)), // Pure absolute black background
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_lumina_logo_user_v2),
            contentDescription = "Lumina Logo",
            modifier = Modifier
                .size(if (isTvOrWide) 300.dp else 220.dp) // Sized up by 30-40% for maximum presence
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                    alpha = logoAlpha
                },
            contentScale = ContentScale.Fit
        )
    }
}
