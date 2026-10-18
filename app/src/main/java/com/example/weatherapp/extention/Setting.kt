package com.example.weatherapp.extention

import android.content.Context
import androidx.core.content.edit
import com.example.weatherapp.extention.Setting.Keys.MODE_OFF
import com.example.weatherapp.extention.Setting.Keys.MODE_ON
import com.example.weatherapp.extention.Setting.Keys.MODE_SYSTEM
import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.google.gson.Gson

class Setting(context: Context) {
    private val pref by lazy { context.getSharedPreferences("setting", Context.MODE_PRIVATE) }

    var appLanguage: ApplicationLanguage
        get() = Gson().fromJson(
            pref.getString(
                Keys.APPLICATION_LANGUAGE,
                Gson().toJson(ApplicationLanguage.setAppLanguageDefault())
            ),
            ApplicationLanguage::class.java
        )
        set(value) = pref.edit { putString(Keys.APPLICATION_LANGUAGE, Gson().toJson(value)) }

    var appMetrics: ApplicationMetrics
        get() = Gson().fromJson(
            pref.getString(
                Keys.APPLICATION_METRICS,
                Gson().toJson(ApplicationMetrics.setDefaultAppMetrics())
            ),
            ApplicationMetrics::class.java
        )
        set(value) = pref.edit { putString(Keys.APPLICATION_METRICS, Gson().toJson(value)) }

    var dailyCount: Int
        get() = pref.getInt(Keys.DAILY_COUNT, 16)
        set(value) = pref.edit { putInt(Keys.DAILY_COUNT, value) }

    var lastCityName: String?
        get() = pref.getString(Keys.LAST_CITY_NAME, null)
        set(value) = pref.edit { putString(Keys.LAST_CITY_NAME, value) }

    var darkModeEnabled: Boolean?
        get() = when (pref.getInt(Keys.DARK_MODE, MODE_SYSTEM)) {
            MODE_ON -> true
            MODE_OFF -> false
            else -> null
        }
        set(value) = pref.edit {
            putInt(
                Keys.DARK_MODE,
                when (value) {
                    true -> MODE_ON
                    false -> MODE_OFF
                    null -> MODE_SYSTEM
                },
            )
        }

    object Keys {
        const val APPLICATION_LANGUAGE = "APPLICATION_LANGUAGE"
        const val APPLICATION_METRICS = "APPLICATION_METRICS"
        const val DAILY_COUNT = "DAILY_COUNT"
        const val LAST_CITY_NAME = "LAST_CITY_NAME"
        const val DARK_MODE = "DARK_MODE"

        const val MODE_OFF = 0
        const val MODE_ON = 1
        const val MODE_SYSTEM = 2
    }
}