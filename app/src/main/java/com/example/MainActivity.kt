package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import com.example.ui.screens.ProfileSelectionScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initializing Lumina Play IPTV - Stable Signed Release v2.0.2
        val database = AppDatabase.getDatabase(this)
        val mediaDao = database.mediaDao()
        val repository = MediaRepository(mediaDao)
        val sharedPrefs = getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE)
        val factory = MediaViewModelFactory(repository, sharedPrefs)
        
        val viewModel = ViewModelProvider(this, factory)[MediaViewModel::class.java]

        setContent {
            MyApplicationTheme(
                darkTheme = viewModel.isDarkTheme,
                dynamicColor = false // Keep high comfort premium dark colors stable
            ) {
                var showSplash by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (viewModel.showProfileSelector) {
                        ProfileSelectionScreen(viewModel = viewModel)
                    } else {
                        LuminaAppShell(viewModel = viewModel)
                    }

                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(),
                        exit = fadeOut(animationSpec = tween(700))
                    ) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    }
                }
            }
        }
    }
}

