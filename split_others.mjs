import fs from 'fs';

const screens = [
    { file: 'app/src/main/java/com/example/ui/screens/TvScreen.kt', name: 'TvScreen' },
    { file: 'app/src/main/java/com/example/ui/screens/RadioScreen.kt', name: 'RadioScreen' },
    { file: 'app/src/main/java/com/example/ui/screens/SearchScreen.kt', name: 'SearchScreen' },
    { file: 'app/src/main/java/com/example/ui/screens/WatchlistScreen.kt', name: 'WatchlistScreen' },
];

for (const screen of screens) {
    let content = fs.readFileSync(screen.file, 'utf8');

    // Find the main Composable
    const regex = new RegExp(`@Composable\\s*fun ${screen.name}\\([\\s\\S]*?\\) \\{[\\s\\S]*?\\n\\}`, 'm');
    const match = content.match(regex);
    if (!match) {
        console.log("Could not find " + screen.name);
        continue;
    }
    
    // Actually, finding the end of the function is tricky with regex if there are nested braces.
    // Let's use the brace counting method.
    const startRegex = new RegExp(`fun ${screen.name}\\(`);
    const startMatch = content.match(startRegex);
    let startIndex = startMatch.index;
    
    // Find the `@Composable` before it.
    let composableIndex = content.lastIndexOf('@Composable', startIndex);
    
    // Find the opening brace of the function body
    let braceIndex = content.indexOf('{', startIndex);
    let openBraces = 1;
    let i = braceIndex + 1;
    for (; i < content.length; i++) {
        if (content[i] === '{') openBraces++;
        if (content[i] === '}') openBraces--;
        if (openBraces === 0) break;
    }
    
    let originalFunc = content.substring(composableIndex, i + 1);
    
    // Create Tv and Mobile variants
    let tvFunc = originalFunc.replace(`fun ${screen.name}(`, `fun ${screen.name}Tv(`);
    let mobileFunc = originalFunc.replace(`fun ${screen.name}(`, `fun ${screen.name}Mobile(`);
    
    // Create Delegator
    let delegator = `@Composable
fun ${screen.name}(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWideLayout = context.resources.configuration.screenWidthDp >= 600
    if (isWideLayout) {
        ${screen.name}Tv(viewModel, modifier)
    } else {
        ${screen.name}Mobile(viewModel, modifier)
    }
}`;
    
    // If the original function has more parameters, we might need to adjust the delegator.
    // Looking at LuminaAppShell, they are all called with `(viewModel = viewModel)`.
    // So the delegator is safe.
    
    // Replace original function with the 3 new ones
    let newContent = content.substring(0, composableIndex) + delegator + '\\n\\n' + tvFunc + '\\n\\n' + mobileFunc + content.substring(i + 1);
    
    // Add LocalContext import if missing
    if (!newContent.includes('import androidx.compose.ui.platform.LocalContext')) {
        newContent = newContent.replace('import androidx.compose.runtime.Composable', 'import androidx.compose.runtime.Composable\\nimport androidx.compose.ui.platform.LocalContext');
    }
    
    fs.writeFileSync(screen.file, newContent);
    console.log("Processed " + screen.name);
}
