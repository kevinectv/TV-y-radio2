import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target_gradient = """                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.60f),
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Transparent
                                    ),"""

replacement_gradient = """                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.40f),
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),"""

target_tab = """                                            .tvFocusEffect(
                                                shape = RoundedCornerShape(16.dp),
                                                focusedBorderColor = if (isTabFocused) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                borderWidth = 1.dp,
                                                scaleAmount = 1.05f
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp), // Reducido horizontalmente
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = tabIcon,
                                                contentDescription = displayLabel,
                                                tint = contentColor,
                                                modifier = Modifier.size(15.dp.responsive())
                                            )
                                            Spacer(modifier = Modifier.width(4.dp)) // Más compacto
                                            Text(
                                                text = displayLabel,
                                                color = contentColor,
                                                fontSize = 13.sp.responsive(),
                                                fontWeight = if (isSelected || isTabFocused) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }"""

replacement_tab = """                                            .tvFocusEffect(
                                                shape = RoundedCornerShape(16.dp),
                                                focusedBorderColor = if (isTabFocused) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                borderWidth = 1.dp,
                                                scaleAmount = 1.05f
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
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
                                        }"""


if target_gradient in content and target_tab in content:
    content = content.replace(target_gradient, replacement_gradient)
    content = content.replace(target_tab, replacement_tab)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced successfully.")
else:
    print("Target not found.")
    if target_gradient not in content: print("gradient missing")
    if target_tab not in content: print("tab missing")

