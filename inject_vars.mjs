import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'utf8');

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

content = content.replace(
    'val activeProfile = viewModel.activeProfile',
    'val activeProfile = viewModel.activeProfile' + vars
);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', content);
console.log("Injected vars");
