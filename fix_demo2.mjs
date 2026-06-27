import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

content = content.replace(/val localDetails = remember\(item\) \{ getCinematicDetails\(item\) \}/g, '');
content = content.replace(/val offlineDescription = remember\(item\) \{\n\s*val joins = localDetails\.subtitleLines\.joinToString\("\\n"\)\n\s*if \(joins\.replace\("\\\\[\.\*\?\\\\]"\.toRegex\(\), ""\)\.trim\(\)\.isNotEmpty\(\)\) joins else item\.description\n\s*\}/g, 'val offlineDescription = item.description');
content = content.replace(/item\.streamUrl \?: localDetails\.trailerUrl/g, 'item.streamUrl ?: ""');
content = content.replace(/dynamicBackdrop\.ifEmpty \{ localDetails\.backdropUrl \}/g, 'dynamicBackdrop.ifEmpty { item.backdropUrl ?: item.posterUrl }');


fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Fixed usages step 2.');
