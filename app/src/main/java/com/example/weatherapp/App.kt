package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.extention.Setting
import com.example.weatherapp.logic.syncApplicationNightMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    companion object {
        lateinit var setting: Setting
    }

    override fun onCreate() {
        super.onCreate()
        setting = Setting(applicationContext)
        syncApplicationNightMode(this, setting.darkModeEnabled)
    }
}