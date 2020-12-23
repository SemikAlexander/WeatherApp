package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)

        if (pref.getString("mode", null) == "dark")
            setTheme(R.style.Theme_WeatherAppNight)
        else
            setTheme(R.style.Theme_WeatherApp)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val lan = pref.getString("language", null)
        if (lan != null)
            setLocale(this, lan)
        GlobalScope.launch {
            delay(2000)
            startActivity<MainActivity>()
            finish()
        }
    }

    private fun setLocale(context: Context, locale: String) {
        context.resources.configuration.locale = Locale(locale)
        context.resources.updateConfiguration(
                context.resources.configuration,
                context.resources.displayMetrics
        )
    }
}