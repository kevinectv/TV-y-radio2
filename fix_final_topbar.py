import sys
import re

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Sombra oscura y suave que se funde hacia abajo
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF030406).copy(alpha = 0.95f),
                                    Color(0xFF030406).copy(alpha = 0.85f),
                                    Color(0xFF030406).copy(alpha = 0.50f),
                                    Color(0xFF030406).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(
                            start = if (isWideLayout) 32.dp else 12.dp,
                            end = if (isWideLayout) 32.dp else 16.dp,
                            top = if (isWideLayout) 24.dp else 10.dp,
                            bottom = if (isWideLayout) 56.dp else 24.dp // Espacio extendido para que el degradado actúe como sombra sobre el hero
                        )
                ) {"""

replacement = """                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 1. Sombra muy pequeña y discreta dibujada detrás sin afectar el layout (sin Spacer ni height extra)
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.40f),
                                        Color.Black.copy(alpha = 0.10f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = size.height + 24.dp.toPx() // Cae suavemente debajo del menú sin empujar el Hero
                                ),
                                size = Size(size.width, size.height + 24.dp.toPx())
                            )
                        }
                        .padding(
                            start = if (isWideLayout) 32.dp else 12.dp,
                            end = if (isWideLayout) 32.dp else 16.dp,
                            top = if (isWideLayout) 24.dp else 10.dp,
                            bottom = if (isWideLayout) 20.dp else 10.dp // Padding normal, sin espacio extra
                        )
                ) {"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced top bar container successfully.")
else:
    print("Target not found.")

