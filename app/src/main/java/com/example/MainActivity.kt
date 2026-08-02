package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.data.MediaRepository
import com.example.data.database.AppDatabase
import com.example.ui.LuminaAppShell
import com.example.ui.MediaViewModel
import com.example.ui.MediaViewModelFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import com.example.ui.screens.ProfileSelectionScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var isReady = false

    // Lazy initializations to completely offload heavy constructor tasks from onCreate()
    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { MediaRepository(database.mediaDao()) }
    private val settingsManager by lazy { com.example.data.SettingsManager(applicationContext) }
    private val sharedPrefs by lazy { getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE) }
    private val factory by lazy { MediaViewModelFactory(repository, settingsManager, sharedPrefs) }
    private val viewModel by lazy {
        val vm = ViewModelProvider(this, factory)[MediaViewModel::class.java]
        vm.updateManager = com.example.data.util.UpdateManager(applicationContext)
        vm.catalogRepository = com.example.data.CatalogRepository(applicationContext)
        vm.mdbListSearchService = com.example.data.MdbListSearchService(applicationContext)
        vm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Sincronizar el splash nativo de AndroidX con Compose
        splashScreen.setKeepOnScreenCondition { !isReady }
        
        enableEdgeToEdge()

        setContent {
            // Pre-warm the lazy viewModel and its catalog caches during the splash screen so home is warm instantly
            LaunchedEffect(Unit) {
                val warmViewModel = viewModel
                warmViewModel.refreshCatalogs()
                isReady = true
            }

            var showSplash by remember { mutableStateOf(true) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000)) // Pure pitch-black to eliminate light/dark flashes
            ) {
                // 1. Render the main UI underneath the splash screen so it is pre-composed and fully loaded
                MyApplicationTheme(
                    darkTheme = viewModel.isDarkTheme,
                    dynamicColor = false
                ) {
                    if (viewModel.showProfileSelector) {
                        ProfileSelectionScreen(viewModel = viewModel)
                    } else {
                        LuminaAppShell(viewModel = viewModel)
                    }
                }

                // 2. Overlay the beautiful Splash Screen on top. When it finishes, it fades out cleanly to reveal the fully-loaded UI underneath
                AnimatedVisibility(
                    visible = showSplash,
                    enter = fadeIn(),
                    exit = fadeOut(animationSpec = tween(350))
                ) {
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                }
            }
        }
    }
}

