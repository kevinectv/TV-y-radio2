const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

const platformLogoMap = `
fun getPlatformLogoUrl(platform: String): String {
    return when(platform) {
        "Netflix" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Netflix_2015_logo.svg/512px-Netflix_2015_logo.svg.png"
        "Prime Video" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Amazon_Prime_Video_logo.svg/512px-Amazon_Prime_Video_logo.svg.png"
        "Disney+" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Disney%2B_logo.svg/512px-Disney%2B_logo.svg.png"
        "Max" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/Max_logo.svg/512px-Max_logo.svg.png"
        "Apple TV+" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/Apple_TV_Plus_Logo.svg/512px-Apple_TV_Plus_Logo.svg.png"
        else -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Netflix_2015_logo.svg/512px-Netflix_2015_logo.svg.png"
    }
}
`;

// Insert it at the end of the file
if (!content.includes('fun getPlatformLogoUrl')) {
    content += '\n' + platformLogoMap;
}

const tvStart = '                    // 2. Información base agrupada (Año, Género, Duración)';
const tvEnd = '                    }\n                }\n            }\n        }\n    }\n}';
const mobStart = '                        // 2. Información base agrupada';
const mobEnd = '                        }\n                    }\n                }\n            }\n        }\n    }\n}';

const tvReplacement = `                    // 2. Información base agrupada (Año, Género, Duración)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = richMeta.year, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                        Text(text = richMeta.genres, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                        Text(text = richMeta.duration, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Platform & Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Platform Logo
                        coil.compose.AsyncImage(
                            model = getPlatformLogoUrl(richMeta.platform),
                            contentDescription = richMeta.platform,
                            modifier = Modifier.height(16.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )

                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)

                        // IMDb Logo + Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            coil.compose.AsyncImage(
                                model = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/IMDB_Logo_2016.svg/512px-IMDB_Logo_2016.svg.png",
                                contentDescription = "IMDb",
                                modifier = Modifier.height(14.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = richMeta.ratingImdb, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Sinopsis asegurada
                    val extendedDescription = buildString {
                        append(richMeta.description)
                        if (richMeta.premiumBadges.isNotEmpty() || richMeta.techIndicators.isNotEmpty()) {
                            append("\\n\\n")
                            val tags = richMeta.premiumBadges + richMeta.techIndicators
                            append(tags.joinToString(" • "))
                        }
                    }
                    
                    Text(
                        text = extendedDescription,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        maxLines = 3,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}`;

const mobReplacement = `                        // 2. Información base agrupada
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp.responsive())
                        ) {
                            Text(text = richMeta.year, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp.responsive())
                            Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp.responsive())
                            Text(text = richMeta.genres, color = Color.White, fontSize = 11.sp.responsive(), fontWeight = FontWeight.Medium)
                            Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp.responsive())
                            Text(text = richMeta.duration, color = Color.White, fontSize = 11.sp.responsive(), fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp.responsive()))

                        // 3. Platform & Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp.responsive())
                        ) {
                            // Platform Logo
                            coil.compose.AsyncImage(
                                model = getPlatformLogoUrl(richMeta.platform),
                                contentDescription = richMeta.platform,
                                modifier = Modifier.height(12.dp.responsive()),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )

                            Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp.responsive())

                            // IMDb Logo + Rating
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                coil.compose.AsyncImage(
                                    model = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/IMDB_Logo_2016.svg/512px-IMDB_Logo_2016.svg.png",
                                    contentDescription = "IMDb",
                                    modifier = Modifier.height(10.dp.responsive()),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(4.dp.responsive()))
                                Text(text = richMeta.ratingImdb, color = Color.White, fontSize = 11.sp.responsive(), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp.responsive()))

                        // 4. Sinopsis asegurada
                        val extendedDescription = buildString {
                            append(richMeta.description)
                            if (richMeta.premiumBadges.isNotEmpty() || richMeta.techIndicators.isNotEmpty()) {
                                append("\\n\\n")
                                val tags = richMeta.premiumBadges + richMeta.techIndicators
                                append(tags.joinToString(" • "))
                            }
                        }

                        Text(
                            text = extendedDescription,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp.responsive(),
                            maxLines = 4,
                            lineHeight = 16.sp.responsive(),
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}`;

let tvStartIndex = content.indexOf(tvStart);
if (tvStartIndex !== -1) {
    let tvEndIndex = content.indexOf(tvEnd, tvStartIndex) + tvEnd.length;
    content = content.substring(0, tvStartIndex) + tvReplacement + content.substring(tvEndIndex);
} else {
    console.log('TV block not found');
}

let mobStartIndex = content.indexOf(mobStart);
if (mobStartIndex !== -1) {
    let mobEndIndex = content.indexOf(mobEnd, mobStartIndex) + mobEnd.length;
    content = content.substring(0, mobStartIndex) + mobReplacement + content.substring(mobEndIndex);
} else {
    console.log('Mobile block not found');
}

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
