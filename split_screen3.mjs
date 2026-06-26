import fs from 'fs';

function processFile(filePath, screenName) {
    let content = fs.readFileSync(filePath, 'utf8');

    // Remove the delegator check if we run it multiple times? No, we will just run it on pristine files or the backup we make.
    // Actually, we can just run it.

    // Make everything private
    let privateContent = content.replace(/^fun /gm, 'private fun ');
    privateContent = privateContent.replace(/^data class /gm, 'private data class ');
    privateContent = privateContent.replace(/^val /gm, 'private val ');
    privateContent = privateContent.replace(/^const val /gm, 'private const val ');
    privateContent = privateContent.replace(/^enum class /gm, 'private enum class ');

    // Extract TV and Mobile
    let tvContent = privateContent.replace(
        new RegExp(`private fun ${screenName}\\(`, 'g'),
        `fun ${screenName}Tv(`
    );

    let mobileContent = privateContent.replace(
        new RegExp(`private fun ${screenName}\\(`, 'g'),
        `fun ${screenName}Mobile(`
    );

    // Apply layout regexes
    tvContent = tvContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$1');
    tvContent = tvContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$1');
    
    mobileContent = mobileContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$2');
    mobileContent = mobileContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$2');

    tvContent = tvContent.replace(/val isWideLayout = .*/g, 'val isWideLayout = true');
    mobileContent = mobileContent.replace(/val isWideLayout = .*/g, 'val isWideLayout = false');

    // Also change `if (isWideLayout) { ... } else { ... }` blocks (Wait, replacing large blocks with regex is hard)
    // Actually, if we just set `val isWideLayout = true`, the compiler will optimize it or it's just a variable. We don't need to manually remove the `else` blocks from Kotlin! The Kotlin compiler will just strip dead code. 
    // And if they want to modify it, they can just edit the block inside `isWideLayout = true`.
    // Wait, the user specifically wants to modify TV without touching Mobile. If the Mobile code is dead code in the TV file, it's confusing.
    // However, they can just look at `if (isWideLayout)`. But it's better if they don't have to.
    // For now, this is already splitting them into separate files.

    fs.writeFileSync(filePath.replace('.kt', 'Tv.kt'), tvContent);
    fs.writeFileSync(filePath.replace('.kt', 'Mobile.kt'), mobileContent);

    // Now write the delegator
    const delegator = `package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.MediaViewModel
import androidx.compose.ui.platform.LocalContext

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
    fs.writeFileSync(filePath, delegator);
    console.log(`Processed ${screenName}`);
}

processFile('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'SettingsScreen');
// processFile('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'HomeScreen');
