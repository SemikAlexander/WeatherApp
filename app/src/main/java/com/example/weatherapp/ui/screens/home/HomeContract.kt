package com.example.weatherapp.ui.screens.home

import com.example.weatherapp.mvi.IaEffect
import com.example.weatherapp.mvi.IaIntent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface HomeIntent : IaIntent {
    data object LoadInitial : HomeIntent

    /**
     * Экран снова виден пользователю (первый показ и возврат из настроек).
     * Нужен, чтобы подхватить город, изменённый в настройках.
     */
    data object ScreenShown : HomeIntent
    data object RefreshWeather : HomeIntent
    data object OpenSettingsClicked : HomeIntent
    data object RetryClicked : HomeIntent

    data class WeatherLoaded(
        val city: String,
        val country: String,
        val updatedAt: String,
        val status: String,
        val temperature: String,
        val tempMin: String,
        val tempMax: String,
        val sunrise: String,
        val sunset: String,
        val wind: String,
        val pressure: String,
        val humidity: String,
        val clouds: String,
        val icon: String,
    ) : HomeIntent

    data object WeatherLoadFailed : HomeIntent

    data class ForecastLoaded(val forecast: ImmutableList<ForecastUi>) : HomeIntent
    data object ForecastLoadFailed : HomeIntent
    data object ForecastCleared : HomeIntent
}

data class HomeState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val weather: WeatherUi? = null,
    val forecast: ImmutableList<ForecastUi> = persistentListOf(),
) {
    companion object {
        val EMPTY_WEATHER = WeatherUi(
            city = "London",
            country = "",
            updatedAt = "",
            status = "",
            temperature = "0°",
            tempMin = "",
            tempMax = "",
            sunrise = "--:--",
            sunset = "--:--",
            wind = "0",
            pressure = "0",
            humidity = "0",
            clouds = "100%",
            icon = "",
        )
    }
}

enum class ForecastHorizon {
    IN_3_HOURS,
    IN_6_HOURS,
    IN_12_HOURS,
    IN_24_HOURS,
}

data class ForecastUi(
    val horizon: ForecastHorizon,
    val temperature: String,
    val description: String,
    val icon: String,
)

data class WeatherUi(
    val city: String,
    val country: String,
    val updatedAt: String,
    val status: String,
    val temperature: String,
    val tempMin: String,
    val tempMax: String,
    val sunrise: String,
    val sunset: String,
    val wind: String,
    val pressure: String,
    val humidity: String,
    val clouds: String,
    val icon: String
)

sealed interface HomeEffect : IaEffect {
    data object NavigateToSettings : HomeEffect
    data object ShowNetworkError : HomeEffect
}
