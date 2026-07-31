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
        delay(150)
        startAnimations = true
        delay(950)
        onSplashFinished()
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 700, easing = EaseInOutQuart),
        label = "logo_fade"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08080A)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_lumina_logo_user_v2),
            contentDescription = "Lumina Logo",
            modifier = Modifier
                .size(if (isTvOrWide) 220.dp else 160.dp)
                .alpha(logoAlpha),
            contentScale = ContentScale.Fit
        )
    }
}
