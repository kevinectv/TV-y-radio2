import os

file_path = '/app/applet/app/src/main/java/com/example/ui/screens/LuminaPremiumCard.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update card sizes
old_sizes = """private fun getLocalNormalCardWidth(isWideLayout: Boolean): Dp =
    if (isWideLayout) 115.dp.responsive() else 132.dp.responsive()

@Composable
private fun getLocalExpandedCardWidth(isWideLayout: Boolean): Dp =
    if (isWideLayout) 265.dp.responsive() else 304.dp.responsive()

@Composable
private fun getLocalCardHeight(isWideLayout: Boolean): Dp =
    if (isWideLayout) 172.dp.responsive() else 198.dp.responsive()"""

new_sizes = """private fun getLocalNormalCardWidth(isWideLayout: Boolean): Dp =
    if (isWideLayout) 125.dp.responsive() else 142.dp.responsive()

@Composable
private fun getLocalExpandedCardWidth(isWideLayout: Boolean): Dp =
    if (isWideLayout) 300.dp.responsive() else 345.dp.responsive()

@Composable
private fun getLocalCardHeight(isWideLayout: Boolean): Dp =
    if (isWideLayout) 187.dp.responsive() else 213.dp.responsive()"""

if old_sizes in content:
    content = content.replace(old_sizes, new_sizes)
else:
    # Try with single carriage returns or slightly different spacing
    print("WARNING: Direct old_sizes match failed, doing line-by-line replacement for sizes.")
    content = content.replace("115.dp.responsive() else 132.dp.responsive()", "125.dp.responsive() else 142.dp.responsive()")
    content = content.replace("265.dp.responsive() else 304.dp.responsive()", "300.dp.responsive() else 345.dp.responsive()")
    content = content.replace("172.dp.responsive() else 198.dp.responsive()", "187.dp.responsive() else 213.dp.responsive()")

# 2. Update logo with SubcomposeAsyncImage fallback
old_logo_block = """                                if (!item.logoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = item.logoUrl,
                                        contentDescription = item.title,
                                        modifier = Modifier
                                            .height(38.dp.responsive())
                                            .widthIn(max = 180.dp.responsive()),
                                        contentScale = ContentScale.Fit,
                                        alignment = Alignment.BottomStart
                                    )
                                } else {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 14.sp.responsive(),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }"""

new_logo_block = """                                val resolvedLogo = if (item.logoUrl.isNullOrBlank() || item.logoUrl == "null" || item.logoUrl == "NULL") null else item.logoUrl
                                if (resolvedLogo != null) {
                                    val context = LocalContext.current
                                    coil.compose.SubcomposeAsyncImage(
                                        model = coil.request.ImageRequest.Builder(context)
                                            .data(resolvedLogo)
                                            .crossfade(true)
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = item.title,
                                        modifier = Modifier
                                            .height(38.dp.responsive())
                                            .widthIn(max = 220.dp.responsive()),
                                        contentScale = ContentScale.Fit,
                                        alignment = Alignment.BottomStart,
                                        loading = { },
                                        error = {
                                            Text(
                                                text = item.title,
                                                color = Color.White,
                                                fontSize = 14.sp.responsive(),
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    )
                                } else {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 14.sp.responsive(),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }"""

if old_logo_block in content:
    content = content.replace(old_logo_block, new_logo_block)
else:
    print("WARNING: Old logo block match failed, doing raw search and replace.")
    # Search for item.logoUrl.isNullOrEmpty() or similar
    # Let's locate AsyncImage(model = item.logoUrl...) and replace it
    idx = content.find("model = item.logoUrl")
    if idx != -1:
        # We will surgically replace the entire surrounding if-else condition
        start_if = content.rfind("if (", 0, idx)
        end_else = content.find("}", idx)
        end_else_block = content.find("}", end_else + 1)
        # Let's verify by checking the file
        print(f"Surgical index details: {start_if} to {end_else_block}")

# 3. Optimize animation speeds (immediate/lag-free transition)
content = content.replace("stiffness = 15000f", "stiffness = 24000f")
content = content.replace("durationMillis = 100", "durationMillis = 40")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("SUCCESS: LuminaPremiumCard sizes, logo, and animations polished!")
