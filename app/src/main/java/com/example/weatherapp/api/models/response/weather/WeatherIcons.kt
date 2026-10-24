package com.example.weatherapp.api.models.response.weather

private const val WEATHER_ICON_BASE_URL = "https://openweathermap.org/payload/api/media/file/"

/** URL иконки погоды OpenWeatherMap (одна точка правды для current и forecast). */
fun weatherIconUrl(icon: String?): String = "$WEATHER_ICON_BASE_URL${icon.orEmpty()}.png"
