import fs from 'fs';

function replaceInFile(file, regex, replacement) {
    let content = fs.readFileSync(file, 'utf-8');
    content = content.replace(regex, replacement);
    fs.writeFileSync(file, content);
}

replaceInFile('app/src/main/java/com/example/ui/LuminaAppShell.kt', /AppTab\.TV -> Icons\.Filled\.Tv/g, 'AppTab.TV -> Icons.Filled.LiveTv');
replaceInFile('app/src/main/java/com/example/ui/screens/TvScreen.kt', /Icons\.Default\.Tv/g, 'Icons.Default.LiveTv');
replaceInFile('app/src/main/java/com/example/ui/screens/FullscreenPlayerScreen.kt', /Icons\.Default\.Tv/g, 'Icons.Default.LiveTv');
replaceInFile('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', /Icons\.Default\.Tv/g, 'Icons.Default.LiveTv');

console.log('Icons replaced successfully.');
