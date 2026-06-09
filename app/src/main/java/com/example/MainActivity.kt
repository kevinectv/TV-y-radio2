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
import com.example.ui.screens.ProfileSelectionScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Persistent database configurations
        val database = AppDatabase.getDatabase(this)
        val mediaDao = database.mediaDao()
        val repository = MediaRepository(mediaDao)
        val factory = MediaViewModelFactory(repository)
        
        val viewModel = ViewModelProvider(this, factory)[MediaViewModel::class.java]

        setContent {
            MyApplicationTheme(
                darkTheme = viewModel.isDarkTheme,
                dynamicColor = false // Keep high comfort premium dark colors stable
            ) {
                if (viewModel.showProfileSelector) {
                    ProfileSelectionScreen(viewModel = viewModel)
                } else {
                    LuminaAppShell(viewModel = viewModel)
                }
            }
        }
    }
}

