import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

const mockStart = content.indexOf('fun getMockCast(');
const mockEnd = content.indexOf('fun CatalogItemFullScreenDetails(');
if (mockStart !== -1 && mockEnd !== -1) {
    content = content.substring(0, mockStart) + content.substring(mockEnd);
}

const cineStart = content.indexOf('data class CinematicInfo(');
const cineEnd = content.indexOf('fun TrailerYoutubePlayerDialog(');
if (cineStart !== -1 && cineEnd !== -1) {
    content = content.substring(0, cineStart) + content.substring(cineEnd);
}

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Removed demo functions.');
