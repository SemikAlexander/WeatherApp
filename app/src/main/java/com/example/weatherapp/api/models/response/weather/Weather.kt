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
        if (cod is Int)
            cod
        else
            (cod as String).toInt()
    }
}