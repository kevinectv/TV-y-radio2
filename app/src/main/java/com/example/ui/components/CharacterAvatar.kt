package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath

@Composable
fun CharacterAvatar(
    style: String,
    skinColorHex: String,
    hairColorHex: String,
    accessory: String,
    expression: String,
    profileColorHex: String,
    modifier: Modifier = Modifier
) {
    val skinColor = safeColor(skinColorHex, Color(0xFFFCD0A1))
    val hairColor = safeColor(hairColorHex, Color(0xFF3F2B1E))
    val profileColor = safeColor(profileColorHex, Color(0xFF00E5FF))

    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        profileColor.copy(alpha = 0.9f),
                        profileColor.copy(alpha = 0.4f),
                        Color(0xFF121212)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h * 0.48f)
            val headRadius = w * 0.28f

            // 1. Draw Body/Shoulders
            drawShoulders(w, h, skinColor, style, hairColor)

            // 2. Draw Head Base
            drawHeadBase(center, headRadius, skinColor, style)

            // 3. Draw Style Specifics (Ninja hood, Wizard hair, etc.)
            drawHairAndHeadwear(center, headRadius, hairColor, style, accessory)

            // 4. Draw Face Expressions (Eyes, Mouth, Eyebrows)
            drawExpression(center, headRadius, expression, accessory, style)

            // 5. Draw Eyepatch, Visor or Headband Accessories
            drawAccessories(center, headRadius, accessory, style)

            // 6. Volumetric Overlay / Lighting Glow
            drawGlassGlossEffect(w, h)
        }
    }
}

private fun safeColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

private fun DrawScope.drawShoulders(
    w: Float,
    h: Float,
    skinColor: Color,
    style: String,
    hairColor: Color
) {
    val shoulderPath = Path().apply {
        moveTo(w * 0.15f, h)
        quadraticTo(w * 0.25f, h * 0.72f, w * 0.32f, h * 0.74f)
        lineTo(w * 0.68f, h * 0.74f)
        quadraticTo(w * 0.75f, h * 0.72f, w * 0.85f, h)
        close()
    }

    val jacketBrush = when (style) {
        "ninja" -> Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF0B0B0B)))
        "wizard" -> Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF1E003D)))
        "explorer" -> Brush.verticalGradient(listOf(Color(0xFF33691E), Color(0xFF1B5E20)))
        "futuristic" -> Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFF00ACC1)))
        "pirate" -> Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFF5D0000)))
        "superhero" -> Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFFB71C1C)))
        "urban" -> Brush.verticalGradient(listOf(Color(0xFFFB8C00), Color(0xFFE65100)))
        "fantasy" -> Brush.verticalGradient(listOf(Color(0xFF003D33), Color(0xFF00796B)))
        "youth" -> Brush.verticalGradient(listOf(Color(0xFFD81B60), Color(0xFF3F51B5)))
        else -> Brush.verticalGradient(listOf(Color(0xFF263238), Color(0xFF0F171A)))
    }

    drawPath(path = shoulderPath, brush = jacketBrush)

    // Neck
    val neckPath = Path().apply {
        moveTo(w * 0.42f, h * 0.76f)
        lineTo(w * 0.42f, h * 0.64f)
        lineTo(w * 0.58f, h * 0.64f)
        lineTo(w * 0.58f, h * 0.76f)
        close()
    }
    drawPath(
        path = neckPath,
        brush = Brush.verticalGradient(listOf(skinColor.copy(alpha = 0.85f), skinColor.copy(alpha = 0.5f)))
    )
}

private fun DrawScope.drawHeadBase(
    center: Offset,
    radius: Float,
    skinColor: Color,
    style: String
) {
    if (style == "ninja") {
        // Ninja wears a full hood masking the face except eyes
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF2E2E2E), Color(0xFF111111)), center = center, radius = radius),
            radius = radius,
            center = center
        )
        // Draw face reveal slot for the eyes
        val eyeBoxPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = center.x - radius * 0.7f,
                    top = center.y - radius * 0.25f,
                    right = center.x + radius * 0.7f,
                    bottom = center.y + radius * 0.22f,
                    cornerRadius = CornerRadius(radius * 0.15f)
                )
            )
        }
        drawPath(path = eyeBoxPath, color = skinColor)
    } else {
        // Standard skin head with 3D shadow gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(skinColor, skinColor.copy(alpha = 0.9f), skinColor.copy(alpha = 0.65f)),
                center = center + Offset(0f, -radius * 0.2f),
                radius = radius * 1.1f
            ),
            radius = radius,
            center = center
        )
    }
}

private fun DrawScope.drawHairAndHeadwear(
    center: Offset,
    radius: Float,
    hairColor: Color,
    style: String,
    accessory: String
) {
    when (style) {
        "wizard" -> {
            // Wizards have mystical white/silver long hair on sides
            val hairPath = Path().apply {
                moveTo(center.x - radius * 0.9f, center.y - radius * 0.2f)
                cubicTo(center.x - radius * 1.3f, center.y + radius * 0.5f, center.x - radius * 1.1f, center.y + radius * 1.1f, center.x - radius * 0.6f, center.y + radius * 0.9f)
                lineTo(center.x - radius * 0.5f, center.y + radius * 0.2f)
                close()
            }
            val hairPathRight = Path().apply {
                moveTo(center.x + radius * 0.9f, center.y - radius * 0.2f)
                cubicTo(center.x + radius * 1.3f, center.y + radius * 0.5f, center.x + radius * 1.1f, center.y + radius * 1.1f, center.x + radius * 0.6f, center.y + radius * 0.9f)
                lineTo(center.x + radius * 0.5f, center.y + radius * 0.2f)
                close()
            }
            drawPath(path = hairPath, color = Color(0xFFECEFF1))
            drawPath(path = hairPathRight, color = Color(0xFFECEFF1))

            // Pointed wizard hat
            val hatPath = Path().apply {
                moveTo(center.x - radius * 1.2f, center.y - radius * 0.6f)
                quadraticTo(center.x, center.y - radius * 0.8f, center.x + radius * 1.2f, center.y - radius * 0.6f)
                quadraticTo(center.x + radius * 0.5f, center.y - radius * 1.8f, center.x - radius * 0.1f, center.y - radius * 2.3f)
                quadraticTo(center.x - radius * 0.6f, center.y - radius * 1.6f, center.x - radius * 1.2f, center.y - radius * 0.6f)
                close()
            }
            drawPath(
                path = hatPath,
                brush = Brush.verticalGradient(listOf(Color(0xFF5E35B1), Color(0xFF311B92)))
            )
            // Hat star decoration
            drawCircle(color = Color(0xFFFFEB3B), radius = radius * 0.12f, center = Offset(center.x - radius * 0.1f, center.y - radius * 1.3f))
        }
        "explorer" -> {
            // Explorer hat
            val hatBrim = Path().apply {
                addOval(Rect(center.x - radius * 1.3f, center.y - radius * 0.85f, center.x + radius * 1.3f, center.y - radius * 0.45f))
            }
            val hatDome = Path().apply {
                moveTo(center.x - radius * 0.8f, center.y - radius * 0.65f)
                quadraticTo(center.x - radius * 0.7f, center.y - radius * 1.5f, center.x, center.y - radius * 1.55f)
                quadraticTo(center.x + radius * 0.7f, center.y - radius * 1.5f, center.x + radius * 0.8f, center.y - radius * 0.65f)
                close()
            }
            drawPath(path = hatDome, color = Color(0xFF8D6E63))
            drawPath(path = hatBrim, color = Color(0xFFA1887F))
            // Hat band
            val hatBand = Path().apply {
                moveTo(center.x - radius * 0.82f, center.y - radius * 0.7f)
                quadraticTo(center.x, center.y - radius * 0.8f, center.x + radius * 0.82f, center.y - radius * 0.7f)
                lineTo(center.x + radius * 0.8f, center.y - radius * 0.82f)
                quadraticTo(center.x, center.y - radius * 0.92f, center.x - radius * 0.8f, center.y - radius * 0.82f)
                close()
            }
            drawPath(path = hatBand, color = Color(0xFF2E7D32))
        }
        "pirate" -> {
            // Pirate Bandana
            val bandMain = Path().apply {
                moveTo(center.x - radius * 1.05f, center.y - radius * 0.4f)
                quadraticTo(center.x, center.y - radius * 1.2f, center.x + radius * 1.05f, center.y - radius * 0.4f)
                quadraticTo(center.x + radius * 0.9f, center.y - radius * 1.3f, center.x, center.y - radius * 1.25f)
                quadraticTo(center.x - radius * 0.9f, center.y - radius * 1.3f, center.x - radius * 1.05f, center.y - radius * 0.4f)
                close()
            }
            drawPath(path = bandMain, color = Color(0xFFC62828))
            // Bandana knot
            drawCircle(Color(0xFFC62828), radius * 0.15f, Offset(center.x - radius * 0.95f, center.y - radius * 0.3f))
        }
        "urban" -> {
            // Stylish Spiky Hair
            val spikyHair = Path().apply {
                moveTo(center.x - radius * 1.02f, center.y - radius * 0.3f)
                lineTo(center.x - radius * 0.7f, center.y - radius * 0.9f)
                lineTo(center.x - radius * 0.4f, center.y - radius * 0.75f)
                lineTo(center.x - radius * 0.1f, center.y - radius * 1.05f)
                lineTo(center.x + radius * 0.2f, center.y - radius * 0.8f)
                lineTo(center.x + radius * 0.6f, center.y - radius * 1.02f)
                lineTo(center.x + radius * 0.75f, center.y - radius * 0.65f)
                lineTo(center.x + radius * 1.02f, center.y - radius * 0.3f)
                quadraticTo(center.x, center.y - radius * 0.6f, center.x - radius * 1.02f, center.y - radius * 0.3f)
                close()
            }
            drawPath(path = spikyHair, color = hairColor)
        }
        "youth" -> {
            // backwards baseball cap
            val capDome = Path().apply {
                moveTo(center.x - radius * 0.9f, center.y - radius * 0.35f)
                quadraticTo(center.x, center.y - radius * 1.35f, center.x + radius * 0.9f, center.y - radius * 0.35f)
                quadraticTo(center.x, center.y - radius * 0.55f, center.x - radius * 0.9f, center.y - radius * 0.35f)
                close()
            }
            val capVisor = Path().apply {
                moveTo(center.x - radius * 0.2f, center.y - radius * 0.35f)
                quadraticTo(center.x - radius * 0.5f, center.y - radius * 0.2f, center.x - radius * 0.9f, center.y - radius * 0.15f)
                quadraticTo(center.x - radius * 0.6f, center.y - radius * 0.38f, center.x - radius * 0.2f, center.y - radius * 0.35f)
                close()
            }
            drawPath(path = capDome, color = Color(0xFF0277BD))
            drawPath(path = capVisor, color = Color(0xFFD32F2F))
        }
        "elegant" -> {
            // Sleek combed back hair
            val sleekHair = Path().apply {
                moveTo(center.x - radius * 0.95f, center.y - radius * 0.4f)
                quadraticTo(center.x, center.y - radius * 1.4f, center.x + radius * 0.95f, center.y - radius * 0.4f)
                quadraticTo(center.x, center.y - radius * 0.8f, center.x - radius * 0.95f, center.y - radius * 0.4f)
                close()
            }
            drawPath(path = sleekHair, color = hairColor)
        }
        "fantasy" -> {
            // Magical glowing crown and long hair
            val fanHair = Path().apply {
                moveTo(center.x - radius * 0.92f, center.y - radius * 0.2f)
                cubicTo(center.x - radius * 1.25f, center.y + radius * 0.3f, center.x - radius * 1.2f, center.y + radius * 1.1f, center.x - radius * 0.85f, center.y + radius * 1.0f)
                lineTo(center.x - radius * 0.6f, center.y + radius * 0.1f)
                close()
            }
            val fanHairR = Path().apply {
                moveTo(center.x + radius * 0.92f, center.y - radius * 0.2f)
                cubicTo(center.x + radius * 1.25f, center.y + radius * 0.3f, center.x + radius * 1.2f, center.y + radius * 1.1f, center.x + radius * 0.85f, center.y + radius * 1.0f)
                lineTo(center.x + radius * 0.6f, center.y + radius * 0.1f)
                close()
            }
            drawPath(path = fanHair, color = Color(0xFF00E676))
            drawPath(path = fanHairR, color = Color(0xFF00E676))

            // Crown tiara
            val tiaraPath = Path().apply {
                moveTo(center.x - radius * 0.7f, center.y - radius * 0.65f)
                lineTo(center.x, center.y - radius * 0.98f)
                lineTo(center.x + radius * 0.7f, center.y - radius * 0.65f)
                lineTo(center.x, center.y - radius * 0.75f)
                close()
            }
            drawPath(path = tiaraPath, color = Color(0xFFFFD54F))
        }
        else -> {
            // Default spiky/classic hair for others
            val hairPath = Path().apply {
                moveTo(center.x - radius * 0.9f, center.y - radius * 0.2f)
                quadraticTo(center.x - radius * 0.8f, center.y - radius * 1.1f, center.x, center.y - radius * 1.05f)
                quadraticTo(center.x + radius * 0.8f, center.y - radius * 1.1f, center.x + radius * 0.9f, center.y - radius * 0.2f)
                quadraticTo(center.x, center.y - radius * 0.6f, center.x - radius * 0.9f, center.y - radius * 0.2f)
                close()
            }
            drawPath(path = hairPath, color = hairColor)
        }
    }
}

private fun DrawScope.drawExpression(
    center: Offset,
    radius: Float,
    expression: String,
    accessory: String,
    style: String
) {
    if (style == "ninja" && accessory != "visor") {
        // Ninja only shows focused eyes, no mouth since it is hooded
        drawFocusedEyes(center, radius)
        return
    }

    // 1. Draw Eyes
    val eyeY = center.y - radius * 0.12f
    val eyeOffsetX = radius * 0.32f
    val eyeRadius = radius * 0.08f

    if (expression == "wink") {
        // Left eye closed (wink), right eye open
        // Left Wink line
        drawLine(
            color = Color(0xFF212121),
            start = Offset(center.x - eyeOffsetX - eyeRadius, eyeY),
            end = Offset(center.x - eyeOffsetX + eyeRadius, eyeY),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
        // Right eye open
        drawEye(Offset(center.x + eyeOffsetX, eyeY), eyeRadius)
    } else {
        // Both eyes open
        drawEye(Offset(center.x - eyeOffsetX, eyeY), eyeRadius)
        drawEye(Offset(center.x + eyeOffsetX, eyeY), eyeRadius)
    }

    // 2. Draw Mouth
    val mouthY = center.y + radius * 0.42f
    val mouthWidth = radius * 0.44f

    when (expression) {
        "smile" -> {
            // Big curved smile
            val smilePath = Path().apply {
                moveTo(center.x - mouthWidth * 0.6f, mouthY - radius * 0.08f)
                quadraticTo(center.x, mouthY + radius * 0.18f, center.x + mouthWidth * 0.6f, mouthY - radius * 0.08f)
            }
            drawPath(path = smilePath, color = Color(0xFF212121), style = Stroke(width = 7f, cap = StrokeCap.Round))
        }
        "cool", "determined" -> {
            // Calm straight line or smirk
            drawLine(
                color = Color(0xFF212121),
                start = Offset(center.x - mouthWidth * 0.4f, mouthY),
                end = Offset(center.x + mouthWidth * 0.4f, mouthY),
                strokeWidth = 7f,
                cap = StrokeCap.Round
            )
        }
        "smirk" -> {
            // Mischievous side smirk
            val smirkPath = Path().apply {
                moveTo(center.x - mouthWidth * 0.5f, mouthY)
                quadraticTo(center.x + mouthWidth * 0.2f, mouthY - radius * 0.04f, center.x + mouthWidth * 0.5f, mouthY - radius * 0.15f)
            }
            drawPath(path = smirkPath, color = Color(0xFF212121), style = Stroke(width = 7f, cap = StrokeCap.Round))
        }
        else -> {
            // Classic open smile
            val normalSmile = Path().apply {
                moveTo(center.x - mouthWidth * 0.5f, mouthY)
                quadraticTo(center.x, mouthY + radius * 0.1f, center.x + mouthWidth * 0.5f, mouthY)
            }
            drawPath(path = normalSmile, color = Color(0xFF212121), style = Stroke(width = 6f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawEye(center: Offset, radius: Float) {
    // Pupil
    drawCircle(color = Color(0xFF151515), radius = radius, center = center)
    // Eye Glow/Highlight (for modern anime/3D character feel)
    drawCircle(color = Color.White, radius = radius * 0.35f, center = center + Offset(-radius * 0.3f, -radius * 0.3f))
}

private fun DrawScope.drawFocusedEyes(center: Offset, radius: Float) {
    val eyeY = center.y - radius * 0.02f
    val eyeOffsetX = radius * 0.32f
    val eyeRadius = radius * 0.07f

    // Mysterious, determined slanted ninja eyes
    val leftEyePath = Path().apply {
        moveTo(center.x - eyeOffsetX - eyeRadius * 1.5f, eyeY + eyeRadius * 0.5f)
        lineTo(center.x - eyeOffsetX + eyeRadius * 1.1f, eyeY - eyeRadius * 0.6f)
        lineTo(center.x - eyeOffsetX - eyeRadius * 0.2f, eyeY + eyeRadius * 0.8f)
        close()
    }
    val rightEyePath = Path().apply {
        moveTo(center.x + eyeOffsetX + eyeRadius * 1.5f, eyeY + eyeRadius * 0.5f)
        lineTo(center.x + eyeOffsetX - eyeRadius * 1.1f, eyeY - eyeRadius * 0.6f)
        lineTo(center.x + eyeOffsetX + eyeRadius * 0.2f, eyeY + eyeRadius * 0.8f)
        close()
    }
    drawPath(path = leftEyePath, color = Color(0xFF0A0A0A))
    drawPath(path = rightEyePath, color = Color(0xFF0A0A0A))

    drawCircle(color = Color.White, radius = eyeRadius * 0.35f, center = Offset(center.x - eyeOffsetX, eyeY))
    drawCircle(color = Color.White, radius = eyeRadius * 0.35f, center = Offset(center.x + eyeOffsetX, eyeY))
}

private fun DrawScope.drawAccessories(
    center: Offset,
    radius: Float,
    accessory: String,
    style: String
) {
    when (accessory) {
        "visor" -> {
            // Futuristic transparent glowing cybernetic neon visor
            val visorPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = center.x - radius * 0.88f,
                        top = center.y - radius * 0.25f,
                        right = center.x + radius * 0.88f,
                        bottom = center.y + radius * 0.12f,
                        cornerRadius = CornerRadius(radius * 0.1f)
                    )
                )
            }
            drawPath(
                path = visorPath,
                brush = Brush.linearGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.85f), Color(0xFF2979FF).copy(alpha = 0.55f)))
            )
            // Visor reflection line
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(center.x - radius * 0.7f, center.y - radius * 0.18f),
                end = Offset(center.x + radius * 0.5f, center.y + radius * 0.05f),
                strokeWidth = 4f
            )
        }
        "glasses" -> {
            // Modern round eyeglasses
            val leftCenter = Offset(center.x - radius * 0.32f, center.y - radius * 0.12f)
            val rightCenter = Offset(center.x + radius * 0.32f, center.y - radius * 0.12f)
            val glassRadius = radius * 0.22f

            drawCircle(color = Color(0xFF37474F), radius = glassRadius, center = leftCenter, style = Stroke(width = 5f))
            drawCircle(color = Color(0xFF37474F), radius = glassRadius, center = rightCenter, style = Stroke(width = 5f))
            // Bridge
            drawLine(color = Color(0xFF37474F), start = leftCenter + Offset(glassRadius, 0f), end = rightCenter + Offset(-glassRadius, 0f), strokeWidth = 5f)
        }
        "eyepatch" -> {
            // Captain pirate eyepatch (left eye)
            val eyeOffsetX = radius * 0.32f
            val eyeY = center.y - radius * 0.12f
            val patchCenter = Offset(center.x - eyeOffsetX, eyeY)

            // Strap
            drawLine(
                color = Color(0xFF212121),
                start = Offset(center.x - radius, eyeY - radius * 0.4f),
                end = Offset(center.x + radius, eyeY + radius * 0.2f),
                strokeWidth = 6f
            )
            // Patch
            drawCircle(color = Color(0xFF111111), radius = radius * 0.16f, center = patchCenter)
        }
        "headphones" -> {
            // Urban headphones around head
            val arcRect = Rect(center.x - radius * 1.1f, center.y - radius * 0.5f, center.x + radius * 1.1f, center.y + radius * 0.3f)
            drawArc(
                color = Color(0xFFD32F2F),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 12f)
            )
            // Ear muffs
            drawCircle(color = Color(0xFFD32F2F), radius = radius * 0.25f, center = Offset(center.x - radius * 1.05f, center.y - radius * 0.1f))
            drawCircle(color = Color(0xFFD32F2F), radius = radius * 0.25f, center = Offset(center.x + radius * 1.05f, center.y - radius * 0.1f))
        }
        "headband" -> {
            // Crimson headband for Ninjas/Fighters
            val headbandPath = Path().apply {
                moveTo(center.x - radius * 0.88f, center.y - radius * 0.35f)
                quadraticTo(center.x, center.y - radius * 0.5f, center.x + radius * 0.88f, center.y - radius * 0.35f)
                lineTo(center.x + radius * 0.84f, center.y - radius * 0.5f)
                quadraticTo(center.x, center.y - radius * 0.65f, center.x - radius * 0.84f, center.y - radius * 0.5f)
                close()
            }
            drawPath(path = headbandPath, color = Color(0xFFD32F2F))
        }
    }
}

private fun DrawScope.drawGlassGlossEffect(w: Float, h: Float) {
    // Dynamic overlay shine giving a Netflix premium card volume reflection
    val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(w, 0f)
        lineTo(w * 0.6f, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f), Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    )
}
