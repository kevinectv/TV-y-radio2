import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'utf8');

const delegatorStart = `
@Composable
fun SettingsScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    if (isWideLayout) {
        SettingsScreenTv(viewModel, modifier)
    } else {
        SettingsScreenMobile(viewModel, modifier)
    }
}
`;

// Extract the two branches of SettingsScreen
const mainFuncRegex = /fun SettingsScreen\([\s\S]*?BoxWithConstraints[\s\S]*?val isWideLayout = maxWidth >= 580\.dp\s*if \(isWideLayout\) \{/m;

const match = content.match(mainFuncRegex);
if (!match) {
    console.log("Could not find the start of SettingsScreen");
    process.exit(1);
}

let startIndex = match.index;
// Find the end of `if (isWideLayout) {`
let openBrackets = 1;
let i = startIndex + match[0].length;
for (; i < content.length; i++) {
    if (content[i] === '{') openBrackets++;
    if (content[i] === '}') openBrackets--;
    if (openBrackets === 0) break;
}
let tvBlock = content.substring(startIndex + match[0].length, i);

// Now for mobile block
let mobileStartIndex = content.indexOf('} else {', i);
if (mobileStartIndex === -1) {
    console.log("Could not find else block");
    process.exit(1);
}

openBrackets = 1;
let j = mobileStartIndex + 8; // skip '} else {'
for (; j < content.length; j++) {
    if (content[j] === '{') openBrackets++;
    if (content[j] === '}') openBrackets--;
    if (openBrackets === 0) break;
}
let mobileBlock = content.substring(mobileStartIndex + 8, j);

// The end of the BoxWithConstraints is right after j.
let endOfFunc = content.indexOf('}', j + 1);

// Construct the new functions
let tvFunc = `
@Composable
fun SettingsScreenTv(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    var pushAlerts by remember { mutableStateOf(true) }
    var updateAlerts by remember { mutableStateOf(true) }

    val profilesList by viewModel.profiles.collectAsState(initial = emptyList())
    val activeProfile = viewModel.activeProfile

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
${tvBlock}
    }
}
`;

let mobileFunc = `
@Composable
fun SettingsScreenMobile(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    var pushAlerts by remember { mutableStateOf(true) }
    var updateAlerts by remember { mutableStateOf(true) }

    val profilesList by viewModel.profiles.collectAsState(initial = emptyList())
    val activeProfile = viewModel.activeProfile

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
${mobileBlock}
    }
}
`;

// Replace the old function with the delegator + tvFunc + mobileFunc
let newContent = content.substring(0, startIndex) + delegatorStart + tvFunc + mobileFunc + content.substring(endOfFunc + 1);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', newContent);
console.log("SettingsScreen split successfully!");
