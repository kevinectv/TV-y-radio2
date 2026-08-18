import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Shadow/Gradient
old_gradient = """                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                endX = 1000f
                            )
                        )
                )"""

new_gradient = """                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                endX = 1200f
                            )
                        )
                )"""
if old_gradient in content:
    content = content.replace(old_gradient, new_gradient)

# 2. Logo Size
old_logo_size = """                                modifier = Modifier
                                    .heightIn(max = 86.dp)
                                    .widthIn(max = 400.dp),"""
new_logo_size = """                                modifier = Modifier
                                    .heightIn(max = 110.dp)
                                    .widthIn(max = 480.dp),"""
if old_logo_size in content:
    content = content.replace(old_logo_size, new_logo_size)

# Fallback text size
old_text_size = """                                            fontSize = 38.sp,"""
new_text_size = """                                            fontSize = 44.sp,"""
if old_text_size in content:
    content = content.replace(old_text_size, new_text_size)

old_text_size2 = """                                    fontSize = 38.sp,"""
new_text_size2 = """                                    fontSize = 44.sp,"""
if old_text_size2 in content:
    content = content.replace(old_text_size2, new_text_size2)

# 3. Synopsis down
old_synopsis = """                    // 3. Short Synopsis
                    Text(
                        text = richMeta.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        maxLines = 3,
                        lineHeight = 20.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )"""
new_synopsis = """                    // 3. Short Synopsis
                    Text(
                        text = richMeta.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        maxLines = 3,
                        lineHeight = 20.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    )"""
if old_synopsis in content:
    content = content.replace(old_synopsis, new_synopsis)

# 4. Carousel Indicators slightly down and more centered
old_row = """                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {"""
new_row = """                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(48.dp)
                    ) {"""
if old_row in content:
    content = content.replace(old_row, new_row)

old_indicators = """                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {"""
new_indicators = """                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.CenterVertically).offset(y = 4.dp)
                            ) {"""
if old_indicators in content:
    content = content.replace(old_indicators, new_indicators)

# 5. Play Button
old_play = """                .padding(bottom = 116.dp, end = 80.dp)"""
new_play = """                .padding(bottom = 138.dp, end = 80.dp)"""
if old_play in content:
    content = content.replace(old_play, new_play)


with open(file_path, "w") as f:
    f.write(content)
print("Hero Banner visual changes applied.")

