package com.example.weatherapp.extention

import android.content.Context
import androidx.core.content.edit
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

    object Keys {
        const val APPLICATION_LANGUAGE = "APPLICATION_LANGUAGE"
        const val APPLICATION_METRICS = "APPLICATION_METRICS"
        const val DAILY_COUNT = "DAILY_COUNT"
    }
}