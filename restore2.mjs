import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

// Fix repeated annotation
content = content.replace(/@Composable\n@Composable/g, '@Composable');

// Fix localDetails
content = content.replace(/localDetails\.trailerUrl/g, '""');
content = content.replace(/localDetails\.backdropUrl/g, 'item.backdropUrl ?: item.posterUrl');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Fixed localDetails and annotations.');
