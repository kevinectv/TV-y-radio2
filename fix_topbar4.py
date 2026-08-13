import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target_outer = """                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 1. Sombra muy sutil dibujada detrás sin afectar el layout (sin Spacer ni height extra)
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.40f),
                                        Color.Black.copy(alpha = 0.05f),
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
                        )
                ) {
                    // Left Node (Logo)
                    Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            append("LUMIN")
                            pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF00E5FF)))
                            append("A")
                            pop()
                        },
                        color = Color.White,
                        fontSize = 16.sp.responsive(),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Central Node (Tabs)
                    if (isWideLayout) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // Muy compacto y centrado
                        ) {"""

replacement_outer = """                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 1. Sombra muy sutil dibujada detrás sin afectar el layout (sin Spacer ni height extra)
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = size.height + 120.dp.toPx() // Cae suavemente debajo del menú sin empujar el Hero
                                ),
                                size = Size(size.width, size.height + 120.dp.toPx())
                            )
                        }
                        .padding(
                            start = if (isWideLayout) 32.dp else 12.dp,
                            end = if (isWideLayout) 32.dp else 16.dp,
                            top = if (isWideLayout) 24.dp else 10.dp,
                            bottom = if (isWideLayout) 20.dp else 10.dp // Padding normal, sin espacio extra
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Node (Logo)
                    Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            append("LUMIN")
                            pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF00E5FF)))
                            append("A")
                            pop()
                        },
                        color = Color.White,
                        fontSize = 16.sp.responsive(),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))

                    // Central Node (Tabs)
                    if (isWideLayout) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // Muy compacto y centrado
                        ) {"""

target_tab = """                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.wrapContentSize(Alignment.Center)
                                        ) {
                                            Icon(
                                                imageVector = tabIcon,
                                                contentDescription = displayLabel,
                                                tint = contentColor,
                                                modifier = Modifier.size(16.dp.responsive())
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = displayLabel,
                                                color = contentColor,
                                                fontSize = 13.sp.responsive(),
                                                fontWeight = if (isSelected || isTabFocused) FontWeight.Bold else FontWeight.Medium,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }"""

replacement_tab = """                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = tabIcon,
                                                contentDescription = displayLabel,
                                                tint = contentColor,
                                                modifier = Modifier.size(16.dp.responsive())
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = displayLabel,
                                                color = contentColor,
                                                fontSize = 13.sp.responsive(),
                                                fontWeight = if (isSelected || isTabFocused) FontWeight.Bold else FontWeight.Medium,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }"""

target_tabs_end = """                                        }
                                    }
                                    
                                    // Subtle indicator for selected tab
                                    Spacer(modifier = Modifier.height(4.dp))"""

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
                    
                    Spacer(modifier = Modifier.weight(1f))

                    // Right Node: Live Clock, Search Icon, Profile Avatar, and optional Settings Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,"""

if target_outer in content and target_tab in content and target_right in content:
    content = content.replace(target_outer, replacement_outer)
    content = content.replace(target_tab, replacement_tab)
    content = content.replace(target_right, replacement_right)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced successfully.")
else:
    print("Target not found.")
    if target_outer not in content: print("outer missing")
    if target_tab not in content: print("tab missing")
    if target_right not in content: print("right missing")

