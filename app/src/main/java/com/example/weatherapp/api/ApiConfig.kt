package com.example.weatherapp.api

import com.example.weatherapp.BuildConfig

object ApiConfig {
    const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val GEOCODING_BASE_URL = "https://api.openweathermap.org/geo/1.0/"
    const val CLIENT_TIME_OUT_SECONDS = 10L

    val apiKey: String = BuildConfig.OWM_API_KEY
}
