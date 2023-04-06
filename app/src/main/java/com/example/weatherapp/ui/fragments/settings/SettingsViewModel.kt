package com.example.weatherapp.ui.fragments.settings

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import com.example.weatherapp.App
import com.example.weatherapp.R
import java.util.Locale
import javax.inject.Inject

class SettingsViewModel @Inject constructor() : ViewModel() {
    fun changeLocale(context: Context, locale: Locale) {
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        context.createConfigurationContext(config)
    }

    fun getUnit(context: Context): String {
        return when (App.setting.appMetrics.metrics) {
            "standard" -> "${context.getString(R.string.kelvin)}, °${App.setting.appMetrics.metricsKey}"
            "metric" -> "${context.getString(R.string.celsius)}, °${App.setting.appMetrics.metricsKey}"
            else -> "${context.getString(R.string.fahrenheit)}, °${App.setting.appMetrics.metricsKey}"
        }
    }
}