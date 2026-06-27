import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

// 1. Remove getMockCast usage
content = content.replace('dynamicCast = getMockCast(item.title, item.genre)', 'dynamicCast = emptyList()');
content = content.replace('var dynamicCast by remember(item) { mutableStateOf<List<ActorInfo>>(getMockCast(item.title, item.genre)) }', 'var dynamicCast by remember(item) { mutableStateOf<List<ActorInfo>>(emptyList()) }');

// 2. Remove getCinematicDetails usage in HomeHeroBanner
content = content.replace(
    /val movieDetails = getCinematicDetails\(currentSafeMovie\)\n                            val backdropUrlToUse = activeHeroLoadedDetails\?\.backdropUrl \?: movieDetails\.backdropUrl/g,
    'val backdropUrlToUse = activeHeroLoadedDetails?.backdropUrl ?: currentSafeMovie.backdropUrl ?: currentSafeMovie.posterUrl'
);

// CatalogItemFullScreenDetails
content = content.replace(
    /val localDetails = remember\(item\) \{ getCinematicDetails\(item\) \}\n\s*val offlineDescription = remember\(item\) \{\n\s*val joins = localDetails\.subtitleLines\.joinToString\("\\n"\)\n\s*if \(joins\.replace\("\\\\[\.\*\?\\\\]"\.toRegex\(\), ""\)\.trim\(\)\.isNotEmpty\(\)\) joins else item\.description\n\s*\}/g,
    'val offlineDescription = item.description'
);
content = content.replace(/item\.streamUrl \?: localDetails\.trailerUrl/g, 'item.streamUrl ?: ""');
content = content.replace(/dynamicBackdrop\.ifEmpty \{ localDetails\.backdropUrl \}/g, 'dynamicBackdrop.ifEmpty { item.backdropUrl ?: item.posterUrl }');

content = content.replace(/item\.streamUrl \?: details\.trailerUrl/g, 'item.streamUrl ?: ""');
content = content.replace(/dynamicBackdrop\.ifEmpty \{ details\.backdropUrl \}/g, 'dynamicBackdrop.ifEmpty { item.backdropUrl ?: item.posterUrl }');
content = content.replace(/val details = remember\(item\) \{ getCinematicDetails\(item\) \}/g, '');

content = content.replace(/val videoUrl = remember\(item\) \{ item\.trailerUrl \?: item\.streamUrl \?: details\.trailerUrl \}/g, 'val videoUrl = remember(item) { item.trailerUrl ?: item.streamUrl ?: "" }');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Fixed usages.');
