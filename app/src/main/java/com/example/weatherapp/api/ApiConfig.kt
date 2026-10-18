package com.example.weatherapp.api

import com.example.weatherapp.BuildConfig

object ApiConfig {
    const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val CITY_BASE_URL = "https://www.metaweather.com/api/location/"
    const val CLIENT_TIME_OUT_SECONDS = 10L

    val apiKey: String = BuildConfig.OWM_API_KEY
}
