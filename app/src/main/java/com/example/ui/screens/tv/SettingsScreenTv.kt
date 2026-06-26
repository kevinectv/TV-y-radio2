package com.example.ui.screens.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.MediaViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreenTv(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWideLayout = true
    if (isWideLayout) {
        SettingsScreenTv(viewModel, modifier)
    } else {
        SettingsScreenMobile(viewModel, modifier)
    }
}
