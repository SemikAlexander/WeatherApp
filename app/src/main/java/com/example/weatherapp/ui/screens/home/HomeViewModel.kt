package com.example.weatherapp.ui.screens.home

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.App
import com.example.weatherapp.R
import com.example.weatherapp.api.interfaces.ICity
import com.example.weatherapp.api.interfaces.IWeather
import com.example.weatherapp.logic.WeatherFormatter
import com.example.weatherapp.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI-ViewModel экрана Home.
 *
 * Отличия от legacy HomeViewModel:
 *  - единый immutable [HomeState] вместо нескольких SharedFlow с replay=1;
 *  - сервисы инжектятся через Hilt по интерфейсам (тестируемость, fake в тестах);
 *  - debounce поиска — отменяемый [Job] вместо java.util.Timer (Timer не отменялся
 *    и утекал вместе с эмиссиями в мёртвый экран);
 *  - ошибки сети отделены от успешной загрузки (hasError), а не «emit(null)».
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherService: IWeather,
    private val cityService: ICity,
) : MviViewModel<HomeIntent, HomeState, HomeEffect>() {

    /** Текущий город запроса — вне UI-состояния (не рендерится напрямую). */
    private val _cityName = MutableStateFlow(
        runCatching { App.setting.lastCityName }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: DEFAULT_CITY,
    )
    val cityName: StateFlow<String> = _cityName.asStateFlow()

    private var searchJob: Job? = null

    override fun initialState(): HomeState = HomeState()

    override fun reduce(state: HomeState, intent: HomeIntent): HomeState = when (intent) {
        HomeIntent.LoadInitial,
        HomeIntent.RefreshWeather,
        HomeIntent.RetryClicked -> state.copy(isLoading = true, hasError = false)

        is HomeIntent.CityQueryChanged -> state.copy(
            cityQuery = intent.query,
            isSuggestionsVisible = intent.query.length >= MIN_QUERY_LENGTH,
        )

        is HomeIntent.CityPicked -> state.copy(
            cityQuery = intent.city.title,
            suggestions = emptyList(),
            isSuggestionsVisible = false,
        )

        is HomeIntent.CitiesLoaded -> state.copy(suggestions = intent.cities)

        HomeIntent.CitiesLoadFailed -> state // подсказки некритичны: молча сохраняем старые

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
            ),
        )

        HomeIntent.WeatherLoadFailed -> state.copy(isLoading = false, hasError = true)

        HomeIntent.OpenSettingsClicked -> state // только эффект
    }

    override suspend fun handle(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadInitial,
            HomeIntent.RefreshWeather,
            HomeIntent.RetryClicked -> loadWeather()

            is HomeIntent.CityQueryChanged -> scheduleCitySearch(intent.query)

            is HomeIntent.CityPicked -> {
                _cityName.value = intent.city.title
                dispatch(HomeIntent.RefreshWeather)
            }

            HomeIntent.OpenSettingsClicked -> emitEffect(HomeEffect.NavigateToSettings)

            else -> Unit // внутренние интенты результатов — только reduce
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            runCatching { weatherService.getWeather(_cityName.value) }
                .onSuccess { w ->
                    val units = WeatherFormatter.unitSymbol(App.setting.appMetrics.metrics)
                    val main = w.main
                    val sys = w.sys
                    dispatch(
                        HomeIntent.WeatherLoaded(
                            city = w.name ?: _cityName.value,
                            country = sys?.country.orEmpty(),
                            updatedAt = WeatherFormatter.formatDate(w.dt, DATE_PATTERN_UPDATE),
                            status = WeatherFormatter.capitalizeDescription(
                                w.weather?.firstOrNull()?.description,
                            ),
                            temperature = main?.let {
                                WeatherFormatter.formatTemperature(it.temp, units)
                            }.orEmpty(),
                            tempMin = main?.let {
                                WeatherFormatter.formatTemperatureTruncated(it.temp_min, units)
                            }.orEmpty(),
                            tempMax = main?.let {
                                WeatherFormatter.formatTemperatureTruncated(it.temp_max, units)
                            }.orEmpty(),
                            sunrise = WeatherFormatter.formatDate(sys?.sunrise, TIME_PATTERN),
                            sunset = WeatherFormatter.formatDate(sys?.sunset, TIME_PATTERN),
                            wind = w.wind?.speed?.toInt()?.toString().orEmpty(),
                            pressure = main?.pressure?.toString().orEmpty(),
                            humidity = main?.humidity?.toString().orEmpty(),
                        ),
                    )
                    runCatching { App.setting.lastCityName = _cityName.value }
                }
                .onFailure {
                    dispatch(HomeIntent.WeatherLoadFailed)
                    emitEffect(HomeEffect.ShowNetworkError)
                }
        }
    }

    /** Debounce поиска городов: новый ввод отменяет предыдущий таймер (замена Timer). */
    private fun scheduleCitySearch(query: String) {
        searchJob?.cancel()
        if (query.length < MIN_QUERY_LENGTH) return
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            runCatching { cityService.getCity(query) }
                .onSuccess { cities -> dispatch(HomeIntent.CitiesLoaded(cities)) }
                .onFailure { dispatch(HomeIntent.CitiesLoadFailed) }
        }
    }

    companion object {
        const val DEFAULT_CITY = "London"
        const val MIN_QUERY_LENGTH = 2
        const val SEARCH_DEBOUNCE_MS = 1000L
        const val DATE_PATTERN_UPDATE = "dd/MM/yyyy hh:mm a"
        const val TIME_PATTERN = "hh:mm a"

        /** Резолв локализованных шаблонов («Updated at», «Min t») — делает UI-слой. */
        fun updatedLabel(context: Context, date: String): String =
            context.getString(R.string.update_data, date)

        fun minTempLabel(context: Context, value: String): String =
            context.getString(R.string.min_temp, value)

        fun maxTempLabel(context: Context, value: String): String =
            context.getString(R.string.max_temp, value)
    }
}
