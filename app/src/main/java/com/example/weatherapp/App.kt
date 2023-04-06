package com.example.weatherapp

import android.app.Application
import android.app.LocaleManager
import android.content.res.Configuration
import com.example.weatherapp.extention.Setting
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    companion object {
        lateinit var setting: Setting
    }

    override fun onCreate() {
        super.onCreate()
        setting = Setting(applicationContext)
    }
}