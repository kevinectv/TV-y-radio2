package com.example.ui.screens.mobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.MediaViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreenMobile(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWideLayout = false
    if (isWideLayout) {
        SettingsScreenTv(viewModel, modifier)
    } else {
        SettingsScreenMobile(viewModel, modifier)
    }
}
