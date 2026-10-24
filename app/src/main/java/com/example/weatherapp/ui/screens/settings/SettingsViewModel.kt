package com.example.weatherapp.ui.screens.settings

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.App
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.api.interfaces.IGeocoding
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.logic.LanguageLogic
import com.example.weatherapp.logic.WeatherFormatter
import com.example.weatherapp.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val geocodingService: IGeocoding,
) : MviViewModel<SettingsIntent, SettingsState, SettingsEffect>() {
    companion object {
        const val MIN_CITY_QUERY_LENGTH = 2
        const val CITY_SEARCH_LIMIT = 5
        const val CITY_SEARCH_DEBOUNCE_MS = 500L
    }

    private val cityQueryFlow = MutableStateFlow("")
    private var citySearchJob: Job? = null
    private var lastSearchedQuery: String? = null

    init {
        dispatch(SettingsIntent.LoadInitial)
        observeCitySearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeCitySearch() {
        cityQueryFlow
            .debounce(CITY_SEARCH_DEBOUNCE_MS.milliseconds)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.trim().length >= MIN_CITY_QUERY_LENGTH) searchCities(query)
            }
            .launchIn(viewModelScope)
    }

    override fun initialState(): SettingsState = SettingsState()

    override fun reduce(state: SettingsState, intent: SettingsIntent): SettingsState =
        when (intent) {
            SettingsIntent.LoadInitial -> {
                val lang = App.setting.appLanguage
                val metrics = App.setting.appMetrics
                val city = App.setting.selectedCity
                state.copy(
                    languageLabel = WeatherFormatter.formatLanguageLabel(
                        lang.language,
                        lang.languageKey
                    ),
                    unitsLabel = unitsLabel(metrics),
                    versionLabel = BuildConfig.VERSION_NAME,
                    cityLabel = city.displayName,
                    currentCity = city,
                    currentLanguage = lang,
                    currentMetrics = metrics,
                    languages = LanguageLogic.availableLanguages(),
                    metricsOptions = ApplicationMetrics.getMetricsList(),
                    isDarkMode = App.setting.darkModeEnabled ?: false,
                    activeDialog = SettingsDialog.NONE,
                    cityQuery = "",
                    citySuggestions = persistentListOf(),
                    isCitySearching = false,
                    citySearchFailed = false,
                )
            }

            SettingsIntent.OpenCityDialogClicked -> state.copy(
                activeDialog = SettingsDialog.CITY,
                cityQuery = "",
                citySuggestions = persistentListOf(),
                isCitySearching = false,
                citySearchFailed = false,
            )

            is SettingsIntent.CityQueryChanged -> state.copy(
                cityQuery = intent.query,
                citySuggestions = persistentListOf(),
                isCitySearching = intent.query.trim().length >= MIN_CITY_QUERY_LENGTH,
                citySearchFailed = false,
            )

            SettingsIntent.CitySearchSubmitted -> state

            SettingsIntent.CitySearchStarted -> state.copy(
                isCitySearching = true,
                citySearchFailed = false,
            )

            is SettingsIntent.CitiesLoaded -> state.copy(
                citySuggestions = intent.cities,
                isCitySearching = false,
                citySearchFailed = false,
            )

            SettingsIntent.CitiesLoadFailed -> state.copy(
                citySuggestions = persistentListOf(),
                isCitySearching = false,
                citySearchFailed = true,
            )

            is SettingsIntent.CityPicked -> state.copy(
                activeDialog = SettingsDialog.NONE,
                currentCity = intent.city,
                cityLabel = intent.city.displayName,
                cityQuery = "",
                citySuggestions = persistentListOf(),
                isCitySearching = false,
                citySearchFailed = false,
            )

            SettingsIntent.OpenLanguageDialogClicked ->
                state.copy(activeDialog = SettingsDialog.LANGUAGE)

            SettingsIntent.OpenUnitsDialogClicked ->
                state.copy(activeDialog = SettingsDialog.UNITS)

            SettingsIntent.DialogDismissed ->
                state.copy(activeDialog = SettingsDialog.NONE)

            is SettingsIntent.DarkModeToggled ->
                state.copy(isDarkMode = intent.enabled)

            is SettingsIntent.LanguagePicked -> state.copy(
                activeDialog = SettingsDialog.NONE,
                currentLanguage = intent.language,
                languageLabel = WeatherFormatter.formatLanguageLabel(
                    intent.language.language, intent.language.languageKey,
                ),
            )

            is SettingsIntent.UnitsPicked -> state.copy(
                activeDialog = SettingsDialog.NONE,
                currentMetrics = intent.metrics,
                unitsLabel = unitsLabel(intent.metrics),
            )

            SettingsIntent.BackClicked,
            SettingsIntent.ExitClicked, -> state
        }

    override suspend fun handle(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.OpenCityDialogClicked -> {
                citySearchJob?.cancel()
                lastSearchedQuery = null
                cityQueryFlow.value = ""
            }

            is SettingsIntent.CityQueryChanged -> {
                citySearchJob?.cancel()
                if (intent.query.trim() != lastSearchedQuery) lastSearchedQuery = null
                cityQueryFlow.value = intent.query
            }

            SettingsIntent.CitySearchSubmitted -> {
                val query = state.value.cityQuery.trim()
                if (query.length >= MIN_CITY_QUERY_LENGTH) searchCities(query)
            }

            is SettingsIntent.CityPicked -> App.setting.selectedCity = intent.city

            SettingsIntent.DialogDismissed -> {
                citySearchJob?.cancel()
                lastSearchedQuery = null
                cityQueryFlow.value = ""
            }

            is SettingsIntent.LanguagePicked -> {
                if (App.setting.appLanguage != intent.language) {
                    App.setting.appLanguage = intent.language
                    emitEffect(SettingsEffect.RestartForLocale)
                }
            }

            is SettingsIntent.UnitsPicked ->
                App.setting.appMetrics = intent.metrics

            is SettingsIntent.DarkModeToggled -> {
                if (App.setting.darkModeEnabled != intent.enabled) {
                    App.setting.darkModeEnabled = intent.enabled
                    emitEffect(SettingsEffect.RestartForTheme)
                }
            }

            SettingsIntent.BackClicked -> emitEffect(SettingsEffect.NavigateBack)
            SettingsIntent.ExitClicked -> emitEffect(SettingsEffect.ExitApp)

            else -> Unit
        }
    }

    private fun searchCities(query: String) {
        val normalized = query.trim()
        if (normalized == lastSearchedQuery) return
        lastSearchedQuery = normalized

        citySearchJob?.cancel()
        dispatch(SettingsIntent.CitySearchStarted)
        citySearchJob = viewModelScope.launch {
            try {
                val cities = geocodingService.searchCities(normalized, CITY_SEARCH_LIMIT)
                dispatch(SettingsIntent.CitiesLoaded(cities.toPersistentList()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastSearchedQuery = null // ошибка не блокирует повторный запрос
                dispatch(SettingsIntent.CitiesLoadFailed)
            }
        }
    }

    private fun unitsLabel(metrics: ApplicationMetrics): String =
        WeatherFormatter.formatUnitLabel(ScaleNames.nameOf(metrics.metrics), metrics.metricsKey)
}

object ScaleNames {
    fun nameOf(metrics: String): String = when (metrics) {
        WeatherFormatter.METRIC_STANDARD -> "Kelvin"
        WeatherFormatter.METRIC_METRIC -> "Celsius"
        else -> "Fahrenheit"
    }

    fun resIdOf(metrics: String): Int = when (metrics) {
        WeatherFormatter.METRIC_STANDARD -> R.string.kelvin
        WeatherFormatter.METRIC_METRIC -> R.string.celsius
        else -> R.string.fahrenheit
    }

    fun localized(context: Context, metrics: String): String =
        context.getString(resIdOf(metrics))
}
