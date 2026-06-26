import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

// The file was duplicated because substring(1) was appended.
// So we just need to find the FIRST "package com.example" and the FIRST "fun DrawCatalogRow".
// Actually, let's find the FIRST occurrence of the delegator "fun HomeHeroBanner("
const delegatorStartTag = '@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)\n@Composable\nfun HomeHeroBanner(';
let part1End = content.indexOf(delegatorStartTag);
if (part1End === -1) {
    // maybe we didn't add it or it's modified.
    part1End = content.indexOf('@Composable\nfun HomeHeroBannerTv(');
}

let part1 = content.substring(0, part1End);

// Now we need the rest of the file. The original file had `fun DrawCatalogRow(` right after HomeHeroBanner.
// Because the file was duplicated with `content.substring(1)`, there is a "ackage com.example.ui.screens" somewhere.
// We should find the LAST occurrence of "fun DrawCatalogRow(" ?
// No, the original `DrawCatalogRow` was inside the duplicated part. The duplicated part is exactly the original file from index 1.
// So the duplicated part starts at "ackage com.example.ui.screens".
const ackageIndex = content.indexOf('ackage com.example.ui.screens');
if (ackageIndex !== -1) {
    // The original file is exactly "p" + content.substring(ackageIndex, ... up to the next duplication if any)
    // Wait, let's just take the duplicated part and reconstruct the original file from it!
    // The duplicated part is `content.substring(ackageIndex)`. If it was duplicated once, then "p" + duplicatedPart IS the original file!
    let originalFile = "p" + content.substring(ackageIndex);
    
    // Now from this originalFile, we extract Part 3: everything from "fun DrawCatalogRow(" to the end.
    // Wait, the original file had `HomeHeroBannerTv` ? No, the original file AT THE TIME of duplication already had `HomeHeroBannerTv` and `HomeHeroBannerMobile` because I had run `script.mjs`.
    let part3StartIndex = originalFile.indexOf('@Composable\nfun DrawCatalogRow(');
    if (part3StartIndex === -1) {
        part3StartIndex = originalFile.indexOf('fun DrawCatalogRow(');
    }
    
    let part3 = originalFile.substring(part3StartIndex);
    
    // Now let's construct the new file.
    const delegator = `@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeHeroBanner(
    currentMovie: CatalogItem,
    activeHeroLoadedDetails: LoadedTmdbDetails?,
    featuredMovies: List<CatalogItem>,
    favoriteCatalogItems: Set<String>,
    bannerHeight: androidx.compose.ui.unit.Dp,
    isWideLayout: Boolean,
    viewModel: MediaViewModel,
    scrollState: LazyListState,
    onTrailerClick: (CatalogItem) -> Unit,
    onDetailsClick: (CatalogItem) -> Unit
) {
    if (isWideLayout) {
        HomeHeroBannerTv(currentMovie, activeHeroLoadedDetails, featuredMovies, favoriteCatalogItems, bannerHeight, viewModel, scrollState, onTrailerClick, onDetailsClick)
    } else {
        HomeHeroBannerMobile(currentMovie, activeHeroLoadedDetails, featuredMovies, favoriteCatalogItems, bannerHeight, viewModel, scrollState, onTrailerClick, onDetailsClick)
    }
}
`;
    const tvCode = fs.readFileSync('./tv_code.txt', 'utf8');
    const mobileCode = fs.readFileSync('./mobile_code.txt', 'utf8');
    
    const finalContent = part1 + delegator + '\n' + tvCode + '\n\n' + mobileCode + '\n\n' + part3;
    
    fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', finalContent);
    console.log("Recovered successfully!");
} else {
    console.log("Could not find 'ackage'. Recovery failed.");
}
