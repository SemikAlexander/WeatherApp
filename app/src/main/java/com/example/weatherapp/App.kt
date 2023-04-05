package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.extention.Setting

class App : Application() {
    companion object {
        lateinit var setting: Setting
    }

    override fun onCreate() {
        super.onCreate()
        setting = Setting(applicationContext)
    }
}