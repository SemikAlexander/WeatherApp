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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherService: IWeather,
    private val cityService: ICity,
) : MviViewModel<HomeIntent, HomeState, HomeEffect>() {

    private val _cityName = MutableStateFlow(
        runCatching { App.setting.lastCityName }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: DEFAULT_CITY,
    )
    val cityName: StateFlow<String> = _cityName.asStateFlow()

    private val _cityQuery = MutableStateFlow("")

    private var searchJob: Job? = null

    init {
        observeLiveSearch()
        dispatch(HomeIntent.CityInitialized(_cityName.value))
        dispatch(HomeIntent.LoadInitial)
    }

    @OptIn(FlowPreview::class)
    private fun observeLiveSearch() {
        _cityQuery
            .debounce(SEARCH_DEBOUNCE_MS.milliseconds)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length >= MIN_QUERY_LENGTH && query != _lastQueried.value) {
                    _lastQueried.value = query
                    runCitySearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    private val _lastQueried = MutableStateFlow("")

    override fun initialState(): HomeState = HomeState()

    override fun reduce(state: HomeState, intent: HomeIntent): HomeState = when (intent) {
        HomeIntent.LoadInitial,
        HomeIntent.RefreshWeather,
        HomeIntent.RetryClicked -> state.copy(isLoading = true, hasError = false)

        is HomeIntent.CityQueryChanged -> {
            _cityQuery.value = intent.query
            state.copy(
                cityQuery = intent.query,
                isSuggestionsVisible = intent.query.length >= MIN_QUERY_LENGTH,
            )
        }

        is HomeIntent.SearchSubmitted -> state.copy(
            cityQuery = intent.query,
            isSuggestionsVisible = intent.query.length >= MIN_QUERY_LENGTH,
        )

        is HomeIntent.CityPicked -> state.copy(
            cityQuery = intent.city.title,
            suggestions = persistentListOf(),
            isSuggestionsVisible = false,
        )

        is HomeIntent.CityInitialized -> state.copy(cityQuery = intent.city)

        is HomeIntent.CitiesLoaded -> state.copy(suggestions = intent.cities)

        HomeIntent.CitiesLoadFailed -> state

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

        HomeIntent.OpenSettingsClicked -> state
    }

    override suspend fun handle(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadInitial,
            HomeIntent.RefreshWeather,
            HomeIntent.RetryClicked -> loadWeather()

            is HomeIntent.CityQueryChanged -> Unit

            is HomeIntent.SearchSubmitted -> {
                searchJob?.cancel()
                submitSearch(intent.query)
            }

            is HomeIntent.CityPicked -> {
                _cityName.value = intent.city.title
                dispatch(HomeIntent.RefreshWeather)
            }

            HomeIntent.OpenSettingsClicked -> emitEffect(HomeEffect.NavigateToSettings)

            else -> Unit
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            runCatching { weatherService.getWeather(_cityName.value) }
                .onSuccess { weather ->
                    val units = WeatherFormatter.unitSymbol(App.setting.appMetrics.metrics)
                    val windSymbol =
                        WeatherFormatter.windSpeedSymbol(App.setting.appMetrics.metrics)
                    val main = weather.main
                    val sys = weather.sys
                    dispatch(
                        HomeIntent.WeatherLoaded(
                            city = weather.name ?: _cityName.value,
                            country = sys?.country.orEmpty(),
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
                    runCatching { App.setting.lastCityName = _cityName.value }
                }
                .onFailure {
                    dispatch(HomeIntent.WeatherLoadFailed)
                    emitEffect(HomeEffect.ShowNetworkError)
                }
        }
    }

    private fun submitSearch(query: String) {
        if (query.length < MIN_QUERY_LENGTH || query == _lastQueried.value) return
        _lastQueried.value = query
        runCitySearch(query)
    }

    private fun runCitySearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            runCatching { cityService.getCity(query) }
                .onSuccess { city -> dispatch(HomeIntent.CitiesLoaded(persistentListOf(city))) }
                .onFailure { t ->
                    if (t is NoSuchElementException) {
                        dispatch(HomeIntent.CitiesLoaded(persistentListOf()))
                    } else {
                        dispatch(HomeIntent.CitiesLoadFailed)
                    }
                }
        }
    }

    companion object {
        const val DEFAULT_CITY = "London"
        const val MIN_QUERY_LENGTH = 2
        const val SEARCH_DEBOUNCE_MS = 1000L
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
