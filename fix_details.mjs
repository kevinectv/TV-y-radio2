import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

// 1. Fix the buttons in CatalogItemFullScreenDetails
const brokenButtonsRegex = /\/\/ Essential actions\s*Row\([\s\S]*?Text\(\"TRÁILER\"[\s\S]*?\}\n\s*\}/;

const newButtonsStr = `// Essential actions
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Reproducir
                            Button(
                                onClick = {
                                    val movieChannel = Channel(
                                        id = "catalog_\${item.id}",
                                        name = item.title,
                                        streamUrl = item.streamUrl ?: "",
                                        logoUrl = item.posterUrl,
                                        category = "Cine Premium",
                                        description = item.description,
                                        number = 999
                                    )
                                    viewModel.selectChannel(movieChannel)
                                    viewModel.isFullscreenPlayerActive = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("REPRODUCIR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }

                            // Trailer
                            OutlinedButton(
                                onClick = {
                                    viewModel.activeTrailerItem = item
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TRÁILER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            // Guardar / Mi Lista
                            val isInMyList = item.id in viewModel.favoriteCatalogItems.collectAsState().value
                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleCatalogItemFavorite(item.id)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isInMyList) Color(0xFF00FF87) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isInMyList) Color(0xFF00FF87).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            ) {
                                Icon(if (isInMyList) Icons.Filled.Check else Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isInMyList) "GUARDADO" else "GUARDAR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            
                            // Compartir
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val shareStr = "¡Mira \${item.title} (\${item.year}) en Lumina! Calificación: \${item.rating} estrella."
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, shareStr)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Compartir con"))
                                    } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp).tvFocusEffect(shape = RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("COMPARTIR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }`;

content = content.replace(brokenButtonsRegex, newButtonsStr);

// 2. Reduce padding/sizes so it's more compact on TV
// Replace the top paddings of the scrollable column to be smaller
const colScrollRegex = /\.padding\(start = 24\.dp, end = 24\.dp, bottom = 48\.dp\),\n\s*verticalArrangement = Arrangement\.spacedBy\(24\.dp\)/;
const newColScroll = `.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),\n                verticalArrangement = Arrangement.spacedBy(16.dp)`;
content = content.replace(colScrollRegex, newColScroll);

// Make the backdrop shorter (from 0.16f to 0.10f or so) and change spacing
content = content.replace('padding(top = 16.dp)', 'padding(top = 8.dp)');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Fixed overlay buttons and compact mode.');
