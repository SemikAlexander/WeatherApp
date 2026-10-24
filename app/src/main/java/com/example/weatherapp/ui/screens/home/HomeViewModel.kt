package com.example.weatherapp.ui.screens.home

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.App
import com.example.weatherapp.R
import com.example.weatherapp.api.interfaces.IWeather
import com.example.weatherapp.api.models.response.geocoding.GeoCity
import com.example.weatherapp.logic.WeatherFormatter
import com.example.weatherapp.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Погода для города, выбранного в настройках.
 *
 * Выбора города на главном экране нет: экран только отображает данные и умеет
 * подхватить новую настройку при возврате из Settings (см. [HomeIntent.ScreenShown]).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherService: IWeather,
) : MviViewModel<HomeIntent, HomeState, HomeEffect>() {

    /** Город, для которого сейчас загружены (или загружаются) данные. */
    private var loadedCity: GeoCity = App.setting.selectedCity

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
                loadedCity = App.setting.selectedCity
                loadWeather(loadedCity)
            }

            HomeIntent.ScreenShown -> {
                // Город задаётся только в настройках: при возврате на главный экран
                // сравниваем выбор с уже загруженным городом и перезапрашиваем погоду.
                if (App.setting.selectedCity != loadedCity) {
                    dispatch(HomeIntent.RefreshWeather)
                }
            }

            HomeIntent.OpenSettingsClicked -> emitEffect(HomeEffect.NavigateToSettings)

            else -> Unit
        }
    }

    private fun loadWeather(city: GeoCity) {
        viewModelScope.launch {
            runCatching { weatherService.getWeather(latitude = city.lat, longitude = city.lon) }
                .onSuccess { weather ->
                    if (city != loadedCity) return@onSuccess
                    val units = WeatherFormatter.unitSymbol(App.setting.appMetrics.metrics)
                    val windSymbol =
                        WeatherFormatter.windSpeedSymbol(App.setting.appMetrics.metrics)
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
                .onFailure {
                    if (city != loadedCity) return@onFailure
                    dispatch(HomeIntent.WeatherLoadFailed)
                    emitEffect(HomeEffect.ShowNetworkError)
                }
        }
    }

    companion object {
        const val DATE_PATTERN_UPDATE = "dd/MM/yyyy hh:mm a"
        const val TIME_PATTERN = "hh:mm a"

        fun updatedLabel(context: Context, date: String): String =
            context.getString(R.string.update_data, date)

        fun minTempLabel(context: Context, value: String): String =
            context.getString(R.string.min_temp, value)

        fun maxTempLabel(context: Context, value: String): String =
            context.getString(R.string.max_temp, value)
    }
}
