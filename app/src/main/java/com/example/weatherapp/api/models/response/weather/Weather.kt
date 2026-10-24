package com.example.weatherapp.api.models.response.weather

data class Weather(
    val base: String?,
    val clouds: Clouds?,
    val cod: Any,
    val coord: Coord?,
    val dt: Long?,
    val id: Int?,
    val main: Main?,
    val name: String?,
    val sys: Sys?,
    val weather: List<WeatherX>?,
    val wind: Wind?
) {
    val answerCode by lazy {
        cod as? Int ?: (cod as String).toInt()
    }

    fun getWeatherIcon(): String = weatherIconUrl(weather?.firstOrNull()?.icon)
}