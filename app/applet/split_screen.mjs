import fs from 'fs';

function processFile(filePath, screenName) {
    let content = fs.readFileSync(filePath, 'utf8');

    // Make everything private
    let privateContent = content.replace(/^fun /gm, 'private fun ');
    privateContent = privateContent.replace(/^data class /gm, 'private data class ');
    privateContent = privateContent.replace(/^val /gm, 'private val ');
    privateContent = privateContent.replace(/^const val /gm, 'private const val ');

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
    // For TV: `if (isWideLayout) A else B` -> `A`
    tvContent = tvContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$1');
    tvContent = tvContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$1');
    
    // For Mobile: `if (isWideLayout) A else B` -> `B`
    mobileContent = mobileContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$2');
    mobileContent = mobileContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$2');

    // We also need to hardcode `isWideLayout` to true/false inside the functions so other conditionals work natively.
    tvContent = tvContent.replace(/val isWideLayout = .*/g, 'val isWideLayout = true');
    mobileContent = mobileContent.replace(/val isWideLayout = .*/g, 'val isWideLayout = false');

    fs.writeFileSync(filePath.replace('.kt', 'Tv.kt'), tvContent);
    fs.writeFileSync(filePath.replace('.kt', 'Mobile.kt'), mobileContent);

    // Now write the delegator
    const delegator = `package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.viewmodels.MediaViewModel
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

processFile('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'HomeScreen');
