package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random



@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    
    // Dynamic screen classification
    val configuration = LocalConfiguration.current
    val isTvOrWide = configuration.screenWidthDp >= 580

    // Lifecycle progress corresponding to Splash Screen timing
    var startAnimations by remember { mutableStateOf(false) }
    var progressVal by remember { mutableStateOf(0f) }

    // Start delay to trigger enter animations
    LaunchedEffect(Unit) {
        delay(100)
        startAnimations = true

        // Faster virtual progress tracking (total splash duration: ~1.0 second)
        val steps = 50
        val stepDuration = 16L // ~0.8 seconds to fill 100%
        for (i in 1..steps) {
            delay(stepDuration)
            progressVal = i / 50f
        }
        
        // Brief final delay to let user see full progress, then transition
        delay(100)
        onSplashFinished()
    }

    // --- ANIMATIONS ---
    
    // Smooth Scale Animator for the Brand Logo
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimations) 1.0f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    // Breathing pulse for ambient visual feedback
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_loop")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    // Fade Inn for Logo & Branding details
    val brandingAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1.0f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseInOutQuad),
        label = "branding_alpha"
    )

    // Glow Backlight pulsing intensity
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_intensity"
    )



    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020202)), // Deep Cinematic Obsidian Base
        contentAlignment = Alignment.Center
    ) {
        
        // 1. BACKLIGHT GLOW CANVAS: Subtle background radial gradient glow in deep cyan/purple tones to match the logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Core Radial Underlight Background Glow (Atmospheric Center Backlight in deep blue/cyan/purple tones)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A).copy(alpha = 0.35f * glowIntensity), // Subtle premium deep slate/blue backlight glow
                        Color(0xFF080C14).copy(alpha = 0.6f),
                        Color(0xFF020202)
                    ),
                    center = Offset(width * 0.5f, height * 0.45f),
                    radius = if (isTvOrWide) width * 0.6f else width * 1.0f
                )
            )
        }

        // 2. MAIN LOGO & BRAND TEXT WRAPPER
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .scale(logoScale * pulseScale)
                .alpha(brandingAlpha)
        ) {
            
            // 2a. LOGO SYMBOL: Our custom generated premium luxury glass play-logo
            Box(
                modifier = Modifier
                    .size(if (isTvOrWide) 180.dp else 130.dp)
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Backlight ambient glowing shadow behind our custom image logo
                Box(
                    modifier = Modifier
                        .size(if (isTvOrWide) 150.dp else 105.dp)
                        .scale(1.05f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00D2FF).copy(alpha = 0.18f * glowIntensity), // Cyan neon aura
                                    Color(0xFF9D00FF).copy(alpha = 0.10f * glowIntensity), // Purple neon aura
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.img_lumina_logo),
                    contentDescription = "Lumina Logo",
                    modifier = Modifier
                        .size(if (isTvOrWide) 140.dp else 100.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2b. THE NAME: High contrast elegant typography with a slight horizontal tracking
            Text(
                text = "L U M I N A",
                color = Color.White,
                fontSize = if (isTvOrWide) 34.sp else 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 10.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2c. THE SLOGAN: Cinema grade subtitle with wide spacing
            Text(
                text = "ENTERTAINMENT WITHOUT LIMITS",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = if (isTvOrWide) 11.sp else 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = if (isTvOrWide) 4.sp else 2.5.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(34.dp))

            // 2d. PREMIUM LAUNCH LOADING INDICATOR: Beautiful, ultra-slim laser loading line
            Box(
                modifier = Modifier
                    .width(if (isTvOrWide) 220.dp else 160.dp)
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.CenterStart
            ) {
                // Moving progress bar styled with a dual core light neon glow gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressVal)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.4f),
                                    Color.White,
                                    Color.White.copy(alpha = 0.8f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
        
        // 3. UNDER FOOTER SIGNATURE: Soft tech branding label at the absolute bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = if (isTvOrWide) 32.dp else 24.dp)
                .alpha(brandingAlpha * 0.5f)
        ) {
            Text(
                text = "POWERED BY SYSTEM CORE v3.5",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
