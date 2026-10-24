package com.example.weatherapp.ui.screens.home

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.App
import com.example.weatherapp.R
import com.example.weatherapp.api.interfaces.IWeather
import com.example.weatherapp.api.models.response.forecast.ForecastItem
import com.example.weatherapp.api.models.response.geocoding.GeoCity
import com.example.weatherapp.api.models.response.weather.Weather
import com.example.weatherapp.api.models.response.weather.weatherIconUrl
import com.example.weatherapp.logic.WeatherFormatter
import com.example.weatherapp.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherService: IWeather,
) : MviViewModel<HomeIntent, HomeState, HomeEffect>() {

    private var loadedCity: GeoCity = App.setting.selectedCity

    private var loadJob: Job? = null

    init {
        dispatch(HomeIntent.LoadInitial)
    }

    override fun initialState(): HomeState = HomeState()

    override fun reduce(state: HomeState, intent: HomeIntent): HomeState = when (intent) {
        HomeIntent.LoadInitial,
        HomeIntent.RefreshWeather,
        HomeIntent.RetryClicked -> state.copy(isLoading = true, hasError = false)

        HomeIntent.ScreenShown,
        HomeIntent.OpenSettingsClicked -> state

        HomeIntent.ForecastCleared,
        HomeIntent.ForecastLoadFailed -> state.copy(forecast = persistentListOf())

        is HomeIntent.ForecastLoaded -> state.copy(forecast = intent.forecast)

        is HomeIntent.WeatherLoaded -> state.copy(
            isLoading = false,
            hasError = false,
            weather = WeatherUi(
                city = intent.city,
                country = intent.country,
                updatedAt = intent.updatedAt,
                status = intent.status,
                temperature = intent.temperature,
                tempMin = intent.tempMin,
                tempMax = intent.tempMax,
                sunrise = intent.sunrise,
                sunset = intent.sunset,
                wind = intent.wind,
                pressure = intent.pressure,
                humidity = intent.humidity,
                clouds = intent.clouds,
                icon = intent.icon
            ),
        )

        HomeIntent.WeatherLoadFailed -> state.copy(isLoading = false, hasError = true)
    }

    override suspend fun handle(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadInitial,
            HomeIntent.RefreshWeather,
            HomeIntent.RetryClicked -> {
                val city = App.setting.selectedCity
                if (city != loadedCity) dispatch(HomeIntent.ForecastCleared)
                loadedCity = city
                loadWeather(city)
            }

            HomeIntent.ScreenShown -> {
                if (App.setting.selectedCity != loadedCity) {
                    dispatch(HomeIntent.RefreshWeather)
                }
            }

            HomeIntent.OpenSettingsClicked -> emitEffect(HomeEffect.NavigateToSettings)

            else -> Unit
        }
    }

    private fun loadWeather(city: GeoCity) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val currentDeferred = async {
                runCatching { weatherService.getWeather(latitude = city.lat, longitude = city.lon) }
            }
            val forecastDeferred = async {
                runCatching { weatherService.getForecast(latitude = city.lat, longitude = city.lon) }
            }

            currentDeferred.await()
                .onSuccess { weather ->
                    if (city == loadedCity) dispatchWeatherLoaded(weather, city)
                }
                .onFailure {
                    if (city == loadedCity) {
                        dispatch(HomeIntent.WeatherLoadFailed)
                        emitEffect(HomeEffect.ShowNetworkError)
                    }
                }

            forecastDeferred.await()
                .onSuccess { forecast ->
                    if (city == loadedCity) {
                        dispatch(HomeIntent.ForecastLoaded(buildForecast(forecast.list)))
                    }
                }
                .onFailure {
                    if (city == loadedCity) dispatch(HomeIntent.ForecastLoadFailed)
                }
        }
    }

    private fun dispatchWeatherLoaded(weather: Weather, city: GeoCity) {
        val units = WeatherFormatter.unitSymbol(App.setting.appMetrics.metrics)
        val windSymbol = WeatherFormatter.windSpeedSymbol(App.setting.appMetrics.metrics)
        val main = weather.main
        val sys = weather.sys
        dispatch(
            HomeIntent.WeatherLoaded(
                city = weather.name ?: city.name,
                country = sys?.country ?: city.country.orEmpty(),
                updatedAt = WeatherFormatter.formatDate(
                    epochSeconds = weather.dt,
                    pattern = DATE_PATTERN_UPDATE
                ),
                status = WeatherFormatter.capitalizeDescription(
                    weather.weather?.firstOrNull()?.description,
                ),
                temperature = main?.let {
                    WeatherFormatter.formatTemperature(it.temp, units)
                }.orEmpty(),
                tempMin = main?.let {
                    WeatherFormatter.formatTemperatureTruncated(it.tempMin, units)
                }.orEmpty(),
                tempMax = main?.let {
                    WeatherFormatter.formatTemperatureTruncated(it.tempMax, units)
                }.orEmpty(),
                sunrise = WeatherFormatter.formatDate(sys?.sunrise, TIME_PATTERN),
                sunset = WeatherFormatter.formatDate(sys?.sunset, TIME_PATTERN),
                wind = "${weather.wind?.speed?.toInt() ?: 0} $windSymbol",
                pressure = "${main?.pressure?.toString().orEmpty()} hPa",
                humidity = main?.humidity?.toString().orEmpty(),
                clouds = "${weather.clouds?.all ?: 100}%",
                icon = weather.getWeatherIcon()
            ),
        )
    }

    /**
     * Собирает 4 карточки прогноза из 3-часовых слотов.
     * Первый слот ответа — ближайший будущий 3-часовой рубеж, поэтому:
     * 0 → +3 ч, 1 → +6 ч, 3 → +12 ч, 7 → +24 ч.
     */
    private fun buildForecast(items: List<ForecastItem>?): ImmutableList<ForecastUi> {
        if (items.isNullOrEmpty()) return persistentListOf()
        val units = WeatherFormatter.unitSymbol(App.setting.appMetrics.metrics)

        return FORECAST_SLOTS.mapNotNull { (horizon, index) ->
            val item = items.getOrNull(index) ?: return@mapNotNull null
            val weather = item.weather?.firstOrNull()
            ForecastUi(
                horizon = horizon,
                temperature = item.main?.let {
                    WeatherFormatter.formatTemperature(it.temp, units)
                }.orEmpty(),
                description = WeatherFormatter.capitalizeDescription(weather?.description),
                icon = weatherIconUrl(weather?.icon),
            )
        }.toPersistentList()
    }

    companion object {
        const val DATE_PATTERN_UPDATE = "dd/MM/yyyy hh:mm a"
        const val TIME_PATTERN = "hh:mm a"

        private val FORECAST_SLOTS = listOf(
            ForecastHorizon.IN_3_HOURS to 0,
            ForecastHorizon.IN_6_HOURS to 1,
            ForecastHorizon.IN_12_HOURS to 3,
            ForecastHorizon.IN_24_HOURS to 7,
        )

        fun updatedLabel(context: Context, date: String): String =
            context.getString(R.string.update_data, date)

        fun minTempLabel(context: Context, value: String): String =
            context.getString(R.string.min_temp, value)

        fun maxTempLabel(context: Context, value: String): String =
            context.getString(R.string.max_temp, value)
    }
}
