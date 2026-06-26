import fs from 'fs';
import path from 'path';

function processFile(originalPath, screenName) {
    const dir = path.dirname(originalPath);
    const content = fs.readFileSync(originalPath, 'utf8');

    // Create directories
    const tvDir = path.join(dir, 'tv');
    const mobileDir = path.join(dir, 'mobile');
    if (!fs.existsSync(tvDir)) fs.mkdirSync(tvDir);
    if (!fs.existsSync(mobileDir)) fs.mkdirSync(mobileDir);

    let tvContent = content.replace(/^package com.example.ui.screens/m, 'package com.example.ui.screens.tv');
    let mobileContent = content.replace(/^package com.example.ui.screens/m, 'package com.example.ui.screens.mobile');

    // Extract TV and Mobile names
    tvContent = tvContent.replace(
        new RegExp(`fun ${screenName}\\(`, 'g'),
        `fun ${screenName}Tv(`
    );

    mobileContent = mobileContent.replace(
        new RegExp(`fun ${screenName}\\(`, 'g'),
        `fun ${screenName}Mobile(`
    );

    // Apply layout regexes
    tvContent = tvContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$1');
    tvContent = tvContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$1');
    
    mobileContent = mobileContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$2');
    mobileContent = mobileContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$2');

    tvContent = tvContent.replace(/val isWideLayout = .*/g, 'val isWideLayout = true');
    mobileContent = mobileContent.replace(/val isWideLayout = .*/g, 'val isWideLayout = false');

    fs.writeFileSync(path.join(tvDir, `${screenName}Tv.kt`), tvContent);
    fs.writeFileSync(path.join(mobileDir, `${screenName}Mobile.kt`), mobileContent);

    // Now write the delegator
    const delegator = `package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.MediaViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.tv.${screenName}Tv
import com.example.ui.screens.mobile.${screenName}Mobile

@Composable
fun ${screenName}(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWideLayout = context.resources.configuration.screenWidthDp >= 600
    if (isWideLayout) {
        ${screenName}Tv(viewModel, modifier)
    } else {
        ${screenName}Mobile(viewModel, modifier)
    }
}
`;
    fs.writeFileSync(originalPath, delegator);
    console.log(`Processed ${screenName}`);
}

processFile('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'SettingsScreen');
// processFile('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'HomeScreen');
