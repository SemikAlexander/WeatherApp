package com.example.weatherapp.api

import com.example.weatherapp.BuildConfig

/**
 * Единая точка доступа к конфигурации API.
 *
 * Ключ OpenWeatherMap хранится в local.properties (ключ OPEN_WEATHER_MAP_API_KEY)
 * и попадает в APK через BuildConfig.OWM_API_KEY — файл исключён из VCS (.gitignore).
 * Пустое значение допустимо на машинах без ключа: запросы вернут 401, что обработано
 * как сценарий ошибки на экранах.
 */
object ApiConfig {
    const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val CITY_BASE_URL = "https://www.metaweather.com/api/location/"
    const val CLIENT_TIME_OUT_SECONDS = 10L

    val apiKey: String = BuildConfig.OWM_API_KEY
}
