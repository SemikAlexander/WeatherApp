package com.example.weatherapp.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.weatherapp.App
import com.example.weatherapp.R
import com.example.weatherapp.logic.LanguageLogic
import com.example.weatherapp.logic.isDarkThemeSelected
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageKey = runCatching { App.setting.appLanguage.languageKey }
            .getOrNull()
            ?.let(LanguageLogic::localeTag)
            ?: Locale.getDefault().language
        super.attachBaseContext(newBase.wrapLocale(languageKey))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val darkTheme = isDarkThemeSelected(this)
        setTheme(if (darkTheme) R.style.Theme_WeatherApp_Dark else R.style.Theme_WeatherApp)

        val navLightScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val navDarkScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { darkTheme },
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = navLightScrim,
                darkScrim = navDarkScrim,
                detectDarkMode = { darkTheme },
            ),
        )
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(
            ColorDrawable(
                getColor(
                    if (darkTheme) R.color.start_background_dark
                    else R.color.start_background_light
                ),
            ),
        )

        setContent { AppNavHost() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    companion object {
        @Deprecated("Перезапуск через Intent не нужен; язык применяется recreate()-ом")
        const val EXTRA_RESTART_FOR_LOCALE = "extra_restart_for_locale"

        fun restartWithLocale(activity: ComponentActivity) {
            activity.recreate()
        }
    }
}

fun Context.wrapLocale(languageKey: String): Context {
    val locale = Locale(languageKey)
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration).apply { setLocale(locale) }
    return createConfigurationContext(config)
}
