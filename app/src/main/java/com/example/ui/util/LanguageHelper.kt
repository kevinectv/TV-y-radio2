package com.example.ui.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageHelper {
    /**
     * Applies the selected language to the app.
     * Uses AppCompatDelegate for backward compatibility and LocaleManager for Android 13+.
     */
    fun applyLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Gets the currently selected language code.
     */
    fun getSelectedLanguage(): String {
        return AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
    }

    /**
     * Data class to represent a supported language.
     */
    data class SupportedLanguage(
        val code: String,
        val name: String,
        val nativeName: String,
        val flag: String
    )

    val supportedLanguages = listOf(
        SupportedLanguage("es", "Español", "Español", "🇪🇸"),
        SupportedLanguage("en", "Inglés", "English", "🇺🇸"),
        SupportedLanguage("pt", "Portugués", "Português", "🇧🇷"),
        SupportedLanguage("fr", "Francés", "Français", "🇫🇷"),
        SupportedLanguage("it", "Italiano", "Italiano", "🇮🇹"),
        SupportedLanguage("de", "Alemán", "Deutsch", "🇩🇪")
    )
}
