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
    private val sharedPrefs by lazy { getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE) }
    private val factory by lazy { MediaViewModelFactory(repository, sharedPrefs) }
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
            // Mark as ready once Compose renders its first frame so the native splash is dismissed smoothly
            LaunchedEffect(Unit) {
                isReady = true
            }

            // Force high-comfort dark theme during the splash screen so startup is completely seamless
            MyApplicationTheme(
                darkTheme = true,
                dynamicColor = false
            ) {
                var showSplash by remember { mutableStateOf(true) }
                var splashCompleted by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF020202))) {
                    if (splashCompleted) {
                        // Dynamically resolve user profile preferences only after the splash screen finishes
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
                    }

                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(),
                        exit = fadeOut(animationSpec = tween(700))
                    ) {
                        SplashScreen(
                            onSplashFinished = {
                                showSplash = false
                                splashCompleted = true
                            }
                        )
                    }
                }
            }
        }
    }
}

