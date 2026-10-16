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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
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

    /** Сырой поток текстового поля; live-поиск подписан на него в [init]. */
    private val _cityQuery = MutableStateFlow("")

    private var searchJob: Job? = null

    init {
        observeLiveSearch()
    }

    /**
     * Live-поиск городов: distinctUntilChanged отсекает повторные эмиссии того же текста,
     * debounce схлопывает быстрые последовательные нажатия. Enter идёт через
     * [HomeIntent.SearchSubmitted] (немедленно, с отменю pending-debounce) — дедупликация
     * по [_lastQueried] исключает двойной запрос одного и того же текста.
     */
    @OptIn(FlowPreview::class)
    private fun observeLiveSearch() {
        _cityQuery
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length >= MIN_QUERY_LENGTH && query != _lastQueried.value) {
                    _lastQueried.value = query
                    runCitySearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    /** Последний текст, по которому реально уходил запрос (защита от дублей live/Enter). */
    private val _lastQueried = MutableStateFlow("")

    override fun initialState(): HomeState = HomeState()

    override fun reduce(state: HomeState, intent: HomeIntent): HomeState = when (intent) {
        HomeIntent.LoadInitial,
        HomeIntent.RefreshWeather,
        HomeIntent.RetryClicked -> state.copy(isLoading = true, hasError = false)

        is HomeIntent.CityQueryChanged -> {
            _cityQuery.value = intent.query // питаем live-поиск (debounce+distinct внутри)
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

            is HomeIntent.CityQueryChanged -> Unit // live-путь: reduce уже обновил _cityQuery

            is HomeIntent.SearchSubmitted -> {
                // Enter: немедленный поиск по тексту, отменяем pending-debounce.
                searchJob?.cancel()
                submitSearch(intent.query)
            }

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

    /** Enter/Submit: немедленный поиск; дедуп по _lastQueried отсекает повтор live-запроса. */
    private fun submitSearch(query: String) {
        if (query.length < MIN_QUERY_LENGTH || query == _lastQueried.value) return
        _lastQueried.value = query
        runCitySearch(query)
    }

    /**
     * Выполняет запрос поиска городов и разрешает результат в интенты.
     * Ошибку «город не найден» (NoSuchElementException из [ICity.getCity]) считаем
     * пустым результатом — подсказки схлопнутся, погоду не трогаем.
     */
    private fun runCitySearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            runCatching { cityService.getCity(query) }
                .onSuccess { city -> dispatch(HomeIntent.CitiesLoaded(listOf(city))) }
                .onFailure { t ->
                    if (t is NoSuchElementException) {
                        dispatch(HomeIntent.CitiesLoaded(emptyList()))
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

        /** Резолв локализованных шаблонов («Updated at», «Min t») — делает UI-слой. */
        fun updatedLabel(context: Context, date: String): String =
            context.getString(R.string.update_data, date)

        fun minTempLabel(context: Context, value: String): String =
            context.getString(R.string.min_temp, value)

        fun maxTempLabel(context: Context, value: String): String =
            context.getString(R.string.max_temp, value)
    }
}
