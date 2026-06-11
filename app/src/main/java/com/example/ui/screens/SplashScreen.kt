package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
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

// Represents a tiny light particle drifting in the background for a cinematic bokeh effect
private data class LightParticle(
    val id: Int,
    val initialX: Float,
    val initialY: Float,
    val speedY: Float,
    val amplitude: Float,
    val frequency: Float,
    val maxAlpha: Float,
    val radius: Float
)

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

        // Smooth virtual progress tracking (total splash duration: ~2.8 seconds)
        val steps = 100
        val stepDuration = 24L // ~2.4 seconds to fill 100%
        for (i in 1..steps) {
            delay(stepDuration)
            progressVal = i / 100f
        }
        
        // Brief final delay to let user see full progress, then transition
        delay(200)
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

    // Cinematic particle clock for drifting movement
    val particleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )

    // Stable client-side particle models
    val particles = remember {
        List(25) { index ->
            LightParticle(
                id = index,
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                speedY = 0.05f + Random.nextFloat() * 0.08f,
                amplitude = 0.02f + Random.nextFloat() * 0.04f,
                frequency = 0.5f + Random.nextFloat() * 1.5f,
                maxAlpha = 0.15f + Random.nextFloat() * 0.35f,
                radius = 3.dp.value + Random.nextFloat() * 5.dp.value
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020202)), // Deep Cinematic Obsidian Base
        contentAlignment = Alignment.Center
    ) {
        
        // 1. DUST CHRONICLE CANVAS: Premium moving background lights and soft drift particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1a. Core Radial Underlight Background Glow (Atmospheric Center Backlight)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.08f * glowIntensity),
                        Color(0xFF040406).copy(alpha = 0.4f),
                        Color(0xFF020202)
                    ),
                    center = Offset(width * 0.5f, height * 0.45f),
                    radius = if (isTvOrWide) width * 0.6f else width * 1.0f
                )
            )

            // 1b. Drifting soft-glow cinematic light particles
            particles.forEach { p ->
                val elapsedSeconds = particleTime
                // Drift upwards vertically
                var yPercentage = p.initialY - (p.speedY * elapsedSeconds * 0.05f)
                yPercentage = (yPercentage % 1.0f + 1.0f) % 1.0f // Keep bounds safe [0..1]
                
                // Drift slightly horizontally in a wave pattern
                val xOffset = p.amplitude * kotlin.math.sin(elapsedSeconds * p.frequency * 0.1f)
                var xPercentage = p.initialX + xOffset
                xPercentage = (xPercentage % 1.0f + 1.0f) % 1.0f

                val drawX = xPercentage * width
                val drawY = yPercentage * height

                // Fade out near vertical edges for natural bokeh
                val edgeFade = if (yPercentage < 0.15f) {
                    yPercentage / 0.15f
                } else if (yPercentage > 0.85f) {
                    (1.0f - yPercentage) / 0.15f
                } else {
                    1.0f
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = p.maxAlpha * edgeFade * brandingAlpha),
                            Color.White.copy(alpha = 0.0f)
                        ),
                        center = Offset(drawX, drawY),
                        radius = p.radius * 3f
                    ),
                    center = Offset(drawX, drawY),
                    radius = p.radius * 3f
                )
            }
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
            
            // 2a. LOGO SYMBOL: Procedural glow diamond / prism with custom ambient aura
            Box(
                modifier = Modifier
                    .size(if (isTvOrWide) 160.dp else 110.dp)
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Backlight Halo glow effect behind the logo symbol
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f * glowIntensity),
                                Color.White.copy(alpha = 0.04f * glowIntensity),
                                Color.Transparent
                            ),
                            center = center,
                            radius = r * 1.5f
                        ),
                        radius = r * 1.5f
                    )
                }

                // High fidelity vector prism lines
                Canvas(
                    modifier = Modifier
                        .fillMaxSize(0.72f)
                ) {
                    val w = size.width
                    val h = size.height
                    
                    // Path for beautiful outer diamond shield representing Lumina Aperture
                    val outerPath = Path().apply {
                        moveTo(w * 0.5f, 0f)
                        lineTo(w, h * 0.5f)
                        lineTo(w * 0.5f, h)
                        lineTo(0f, h * 0.5f)
                        close()
                    }

                    // Stroke glow using subtle dual gradient
                    drawPath(
                        path = outerPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.7f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        ),
                        style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round)
                    )

                    // Path for internal floating visual laser line (prism facet)
                    val innerPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.2f)
                        lineTo(w * 0.8f, h * 0.5f)
                        lineTo(w * 0.5f, h * 0.8f)
                        lineTo(w * 0.2f, h * 0.5f)
                        close()
                    }

                    drawPath(
                        path = innerPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.15f)
                            ),
                            start = Offset(0f, h),
                            end = Offset(w, 0f)
                        ),
                        style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round)
                    )

                    // Draw stylized sleek letter mark representation "L" (laser solid bar) and central focal node
                    val linePath = Path().apply {
                        moveTo(w * 0.42f, h * 0.35f)
                        lineTo(w * 0.42f, h * 0.65f)
                        lineTo(w * 0.62f, h * 0.65f)
                    }

                    drawPath(
                        path = linePath,
                        color = Color.White,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Subtle central core beacon (a small filled glowing focal point circle)
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(w * 0.42f, h * 0.65f)
                    )
                }
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
