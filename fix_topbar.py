import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target_box = """                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
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

replacement_box = """                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 1. Sombra muy sutil dibujada detrás sin afectar el layout (sin Spacer ni height extra)
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Black.copy(alpha = 0.40f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = size.height + 60.dp.toPx() // Cae suavemente debajo del menú sin empujar el Hero
                                ),
                                size = Size(size.width, size.height + 60.dp.toPx())
                            )
                        }
                        .padding(
                            start = if (isWideLayout) 32.dp else 12.dp,
                            end = if (isWideLayout) 32.dp else 16.dp,
                            top = if (isWideLayout) 24.dp else 10.dp,
                            bottom = if (isWideLayout) 20.dp else 10.dp // Padding normal, sin espacio extra
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LUMINA Logo + Tabs Group
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp) // Reducido y equilibrado
                    ) {"""

target_left = """                        fontSize = 16.sp.responsive(),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Central Node (Tabs)
                    if (isWideLayout) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,"""

replacement_left = """                        fontSize = 16.sp.responsive(),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )

                    // Central Node (Tabs)
                    if (isWideLayout) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,"""

target_right = """                                }
                            }
                        }
                    }

                    // Right Node: Live Clock, Search Icon, Profile Avatar, and optional Settings Button
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,"""

replacement_right = """                                }
                            }
                        }
                    }
                    } // Fin LUMINA Logo + Tabs Group

                    // Right Node: Live Clock, Search Icon, Profile Avatar, and optional Settings Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,"""

if target_box in content and target_left in content and target_right in content:
    content = content.replace(target_box, replacement_box)
    content = content.replace(target_left, replacement_left)
    content = content.replace(target_right, replacement_right)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced successfully.")
else:
    print("Target not found.")
    if target_box not in content: print("target_box missing")
    if target_left not in content: print("target_left missing")
    if target_right not in content: print("target_right missing")

