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

                // High fidelity TV vector graphic representing the premium LUMINA Entertainment Hub
                Canvas(
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Sleek Futuristic TV Antennas with glowing endpoints
                    val leftAntennaPath = Path().apply {
                        moveTo(w * 0.42f, h * 0.12f)
                        lineTo(w * 0.25f, -h * 0.1f)
                    }
                    val rightAntennaPath = Path().apply {
                        moveTo(w * 0.58f, h * 0.12f)
                        lineTo(w * 0.75f, -h * 0.1f)
                    }

                    drawPath(
                        path = leftAntennaPath,
                        color = Color.White.copy(alpha = 0.6f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = rightAntennaPath,
                        color = Color.White.copy(alpha = 0.6f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Small glowing neon beads on top of the antennas
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(w * 0.25f, -h * 0.1f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(w * 0.75f, -h * 0.1f)
                    )

                    // 2. Beautiful Streaming Radar / Broadcast Waves around the TV
                    val waveGlowAlpha = 0.35f
                    drawArc(
                        color = Color.White.copy(alpha = waveGlowAlpha * 0.5f),
                        startAngle = 140f,
                        sweepAngle = 80f,
                        useCenter = false,
                        topLeft = Offset(-w * 0.18f, h * 0.05f),
                        size = Size(w * 1.36f, h * 0.8f),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = waveGlowAlpha * 0.8f),
                        startAngle = 150f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(-w * 0.08f, h * 0.1f),
                        size = Size(w * 1.16f, h * 0.7f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. Curved TV Cabinet Stand / Footing Base
                    val standPath = Path().apply {
                        moveTo(w * 0.44f, h * 0.78f)
                        quadraticTo(w * 0.45f, h * 0.9f, w * 0.30f, h * 0.94f)
                        lineTo(w * 0.70f, h * 0.94f)
                        quadraticTo(w * 0.55f, h * 0.9f, w * 0.56f, h * 0.78f)
                        close()
                    }
                    drawPath(
                        path = standPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.12f)
                            )
                        )
                    )
                    drawPath(
                        path = standPath,
                        color = Color.White.copy(alpha = 0.5f),
                        style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round)
                    )

                    // 4. Outer Beveled TV Screen Frame with premium rounded edges
                    val outerTvRect = androidx.compose.ui.geometry.RoundRect(
                        left = w * 0.08f,
                        top = h * 0.12f,
                        right = w * 0.92f,
                        bottom = h * 0.78f,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                    )
                    val tvPath = Path().apply {
                        addRoundRect(outerTvRect)
                    }

                    // Draw solid backlight inner screen wash
                    drawPath(
                        path = tvPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.45f),
                            radius = w * 0.5f
                        )
                    )

                    // Stroke for outer TV casing
                    drawPath(
                        path = tvPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.8f)
                            ),
                            start = Offset(w * 0.1f, h * 0.15f),
                            end = Offset(w * 0.9f, h * 0.75f)
                        ),
                        style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round)
                    )

                    // 5. Inner screen bezel boundary
                    val innerTvRect = androidx.compose.ui.geometry.RoundRect(
                        left = w * 0.13f,
                        top = h * 0.17f,
                        right = w * 0.87f,
                        bottom = h * 0.73f,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(11.dp.toPx())
                    )
                    val innerTvPath = Path().apply {
                        addRoundRect(innerTvRect)
                    }
                    drawPath(
                        path = innerTvPath,
                        color = Color.White.copy(alpha = 0.18f),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // 6. Styled sleek laser letter mark "L" inside the widescreen TV display safely centered
                    val brandLPath = Path().apply {
                        moveTo(w * 0.40f, h * 0.32f)
                        lineTo(w * 0.40f, h * 0.58f)
                        lineTo(w * 0.62f, h * 0.58f)
                    }

                    // 6a. Giant fuzzy neon glow underlying the "L"
                    drawPath(
                        path = brandLPath,
                        color = Color.White.copy(alpha = 0.28f),
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    
                    // 6b. Solid sharp high-intensity white core laser "L"
                    drawPath(
                        path = brandLPath,
                        color = Color.White,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // 6c. Central luminous focal highlight node at the turn point
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = Offset(w * 0.40f, h * 0.58f)
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
