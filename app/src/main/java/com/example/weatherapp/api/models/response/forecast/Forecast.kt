package com.example.weatherapp.api.models.response.forecast

import com.example.weatherapp.api.models.response.weather.Main
import com.example.weatherapp.api.models.response.weather.WeatherX

/**
 * Ответ OpenWeatherMap `GET /data/2.5/forecast` — прогноз на 5 дней с шагом 3 часа.
 *
 * Используется бесплатный тариф: из [list] берутся ближайшие 3-часовые слоты
 * (+3, +6, +12 и +24 часа). Минутные прогнозы требуют One Call API 3.0
 * с отдельной подпиской и здесь сознательно не используются.
 */
data class Forecast(
    val cod: String? = null,
    val message: Any? = null,
    val cnt: Int? = null,
    val list: List<ForecastItem>? = null,
)

data class ForecastItem(
    val dt: Long? = null,
    val main: Main? = null,
    val weather: List<WeatherX>? = null,
    val pop: Double? = null,
)
