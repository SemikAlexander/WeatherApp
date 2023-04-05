package com.example.weatherapp.extention

import android.content.Context

class Setting(context: Context) {
    private val pref by lazy { context.getSharedPreferences("setting", Context.MODE_PRIVATE) }
}