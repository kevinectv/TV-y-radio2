import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

// 1. bannerHeight
const bannerRegex = /val bannerHeight = if \(isWideLayout\) 315\.dp else 195\.dp\.responsive\(\)/;
content = content.replace(bannerRegex, 'val bannerHeight = if (isWideLayout) 235.dp else 195.dp.responsive()');

// 2. max width and title
content = content.replace('widthIn(max = 660.dp) // Ancho extendido para evitar saltos prematuros', 'widthIn(max = 520.dp) // Reducido para modo TV');
content = content.replace('heightIn(max = 95.dp) // Ligeramente reducido', 'heightIn(max = 65.dp)');
content = content.replace('fontSize = 32.sp', 'fontSize = 24.sp');
content = content.replace('lineHeight = 36.sp', 'lineHeight = 28.sp');

// 3. paddings and spacings in HomeHeroBannerTv
const tvPaddingRegex = /padding\(\s*start = 48\.dp,\s*end = 48\.dp,\s*bottom = 20\.dp,\s*top = 20\.dp\s*\)/;
content = content.replace(tvPaddingRegex, 'padding(start = 48.dp, end = 48.dp, bottom = 12.dp, top = 12.dp)');

content = content.replace('Spacer(modifier = Modifier.height(12.dp))', 'Spacer(modifier = Modifier.height(8.dp))');
content = content.replace('Spacer(modifier = Modifier.height(14.dp))', 'Spacer(modifier = Modifier.height(10.dp))');
content = content.replace('Spacer(modifier = Modifier.height(16.dp))', 'Spacer(modifier = Modifier.height(12.dp))');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Modified TV hero banner size.');
