import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'utf8');

// Fix repeated @Composable
content = content.replace(/@Composable\s*@Composable/g, '@Composable');

const vars = `
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(SettingCategory.PROFILE) }
    var autoEpgSync by remember { mutableStateOf(true) }
    var downloadLogos by remember { mutableStateOf(true) }
    var bufferLatency by remember { mutableStateOf(true) }
    var hwAudioSync by remember { mutableStateOf(true) }
    var eac3Audio by remember { mutableStateOf(true) }
    var realtimeShadows by remember { mutableStateOf(true) }
    var fluidAnimations by remember { mutableStateOf(true) }
    var ramOptimization by remember { mutableStateOf(true) }
    var forced60fps by remember { mutableStateOf(false) }
    var sendErrorStats by remember { mutableStateOf(false) }
    var keepLocalHistory by remember { mutableStateOf(true) }
    val onOpenSources = { android.widget.Toast.makeText(context, "Abrir fuentes", android.widget.Toast.LENGTH_SHORT).show() }
    val onOpenApiSettings = { android.widget.Toast.makeText(context, "API Settings", android.widget.Toast.LENGTH_SHORT).show() }
    val onOpenDiagnostics = { android.widget.Toast.makeText(context, "Diagnostics", android.widget.Toast.LENGTH_SHORT).show() }
`;

// Replace the SECOND occurrence of activeProfile
let firstIndex = content.indexOf('val activeProfile = viewModel.activeProfile');
let secondIndex = content.indexOf('val activeProfile = viewModel.activeProfile', firstIndex + 1);

if (secondIndex !== -1) {
    content = content.substring(0, secondIndex) + 'val activeProfile = viewModel.activeProfile' + vars + content.substring(secondIndex + 43);
}

// Also fix "Syntax error: Expecting a top level declaration." at line 574.
// Let's check if the braces are balanced for `SettingsScreenMobile`.
// Mobile function end was right before the original `endOfFunc + 1`.
// It's possible I missed a closing brace when constructing `mobileFunc`.
// mobileBlock ends with `}`.
// Let's add an extra `}` just in case, or look for the error. 
// Actually, wait, `split_settings.mjs` did:
// let mobileFunc = `... { ${mobileBlock} } }`;
// I'll just check if it compiles.

fs.writeFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', content);
console.log("Fixed vars and composable");
