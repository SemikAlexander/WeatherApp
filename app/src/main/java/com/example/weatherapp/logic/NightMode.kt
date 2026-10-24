package com.example.weatherapp.logic

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.example.weatherapp.App

fun isDarkThemeSelected(context: Context): Boolean {
    val stored = runCatching { App.setting.darkModeEnabled }.getOrNull()
    return stored ?: (
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        )
}

fun syncApplicationNightMode(context: Context, darkMode: Boolean?) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || darkMode == null) return

    val currentNight = (context.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    if (currentNight == darkMode) return

    context.getSystemService(UiModeManager::class.java)?.setApplicationNightMode(
        if (darkMode) UiModeManager.MODE_NIGHT_YES else UiModeManager.MODE_NIGHT_NO,
    )
}
