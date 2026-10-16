package com.example.weatherapp.ui.screens.home

import com.example.weatherapp.api.models.response.cities.CitiesItem
import com.example.weatherapp.mvi.IaEffect
import com.example.weatherapp.mvi.IaIntent

/**
 * MVI-контракт экрана Home: Intent (вход), State (единственный источник истины UI),
 * Effect (однократные побочные события: тосты/навигация).
 * Держим всё в одном файле — контракт читается целиком и не «протечёт» в другие слои.
 */

sealed interface HomeIntent : IaIntent {
    /** Первый вход на экран: подтянуть погода + сохранить последний город. */
    data object LoadInitial : HomeIntent

    /** Загрузка погоды по текущему cityName (Pull-to-refresh / выбор из списка / retry). */
    data object RefreshWeather : HomeIntent

    /** Ввод текста в поле города (live-путь; debounce/distinct — во ViewModel). */
    data class CityQueryChanged(val query: String) : HomeIntent

    /** Enter/imeAction Search: немедленный поиск по текущему тексту, минуя debounce. */
    data class SearchSubmitted(val query: String) : HomeIntent

    /** Выбор города из выпадающего списка подсказок. */
    data class CityPicked(val city: CitiesItem) : HomeIntent

    /** Клик по иконке настроек. */
    data object OpenSettingsClicked : HomeIntent

    /** Повтор загрузки после ошибки. */
    data object RetryClicked : HomeIntent

    /** Внутренний: результат поиска городов пришёл. */
    data class CitiesLoaded(val cities: List<CitiesItem>) : HomeIntent

    /** Внутренний: поиск городов завершился ошибкой. */
    data object CitiesLoadFailed : HomeIntent

    /** Внутренний: погода загружена успешно. */
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
    ) : HomeIntent

    /** Внутренний: загрузка погоды завершилась ошибкой. */
    data object WeatherLoadFailed : HomeIntent
}

data class HomeState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val cityQuery: String = "",
    val suggestions: List<CitiesItem> = emptyList(),
    val isSuggestionsVisible: Boolean = false,
    /** null -> показываем плейсхолдеры вместо данных. */
    val weather: WeatherUi? = null,
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
        )
    }
}

/** Готовые к отрисовке строки — Composable не знает про API-модели и форматирование. */
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
)

sealed interface HomeEffect : IaEffect {
    data object NavigateToSettings : HomeEffect
    data object ShowNetworkError : HomeEffect
}
