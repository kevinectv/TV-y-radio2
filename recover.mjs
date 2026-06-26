import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

const delegatorStartTag = '@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)\n@Composable\nfun HomeHeroBanner(';
let part1End = content.indexOf(delegatorStartTag);
if (part1End === -1) {
    part1End = content.indexOf('@Composable\nfun HomeHeroBannerTv(');
}

let part1 = content.substring(0, part1End);

const ackageIndex = content.indexOf('ackage com.example.ui.screens');
if (ackageIndex !== -1) {
    let originalFile = "p" + content.substring(ackageIndex);
    
    let part3StartIndex = originalFile.indexOf('@Composable\nfun DrawCatalogRow(');
    if (part3StartIndex === -1) {
        part3StartIndex = originalFile.indexOf('fun DrawCatalogRow(');
    }
    
    let part3 = originalFile.substring(part3StartIndex);
    
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
