import fs from 'fs';
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'utf8');

let extraBraceIndex = content.indexOf('}\n\n}\n\n/**\n * Highly symmetric Category Item inside Sidebar.');
if (extraBraceIndex !== -1) {
    content = content.substring(0, extraBraceIndex + 1) + content.substring(extraBraceIndex + 3);
}

// Add the @OptIn for ExperimentalAnimationApi because it complained about it at line 172.
// Wait, the complaint was "This is an experimental animation API" which is a warning, not an error! 
// e: file:///app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt:172:29 This is an experimental animation API.
// Actually, `e:` means ERROR! So it's an error in Kotlin 1.9+.
// Let's add @OptIn(androidx.compose.animation.ExperimentalAnimationApi::class) to SettingsScreenTv and SettingsScreenMobile.

content = content.replace(
    '@Composable\nfun SettingsScreenTv(',
    '@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)\n@Composable\nfun SettingsScreenTv('
);

content = content.replace(
    '@Composable\nfun SettingsScreenMobile(',
    '@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)\n@Composable\nfun SettingsScreenMobile('
);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', content);
console.log("Fixed brace and OptIn");
