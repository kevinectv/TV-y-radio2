import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

// Even smaller banner for more room
content = content.replace('val bannerHeight = if (isWideLayout) 235.dp else 195.dp.responsive()', 'val bannerHeight = if (isWideLayout) 215.dp else 195.dp.responsive()');

// Further reduce sizes inside TV banner
content = content.replace('heightIn(max = 65.dp)', 'heightIn(max = 55.dp)');
content = content.replace('widthIn(max = 300.dp)', 'widthIn(max = 240.dp)');
content = content.replace('fontSize = 24.sp', 'fontSize = 22.sp');
content = content.replace('lineHeight = 28.sp', 'lineHeight = 26.sp');

content = content.replace('fontSize = 15.sp,\n                        maxLines = 3,\n                        lineHeight = 22.sp,', 'fontSize = 13.sp,\n                        maxLines = 2,\n                        lineHeight = 18.sp,');

// paddings
content = content.replace('padding(start = 48.dp, end = 48.dp, bottom = 12.dp, top = 12.dp)', 'padding(start = 48.dp, end = 48.dp, bottom = 8.dp, top = 8.dp)');
content = content.replace('Spacer(modifier = Modifier.height(10.dp))', 'Spacer(modifier = Modifier.height(6.dp))');
content = content.replace('Spacer(modifier = Modifier.height(8.dp))', 'Spacer(modifier = Modifier.height(4.dp))');
content = content.replace('Spacer(modifier = Modifier.height(12.dp))', 'Spacer(modifier = Modifier.height(8.dp))');


fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Made TV banner even smaller.');
