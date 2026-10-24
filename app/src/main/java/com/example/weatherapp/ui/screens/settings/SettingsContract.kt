package com.example.weatherapp.ui.screens.settings

import com.example.weatherapp.api.models.response.geocoding.GeoCity
import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.mvi.IaEffect
import com.example.weatherapp.mvi.IaIntent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface SettingsIntent : IaIntent {
    data object LoadInitial : SettingsIntent

    data object OpenCityDialogClicked : SettingsIntent
    data class CityQueryChanged(val query: String) : SettingsIntent
    data object CitySearchSubmitted : SettingsIntent
    data object CitySearchStarted : SettingsIntent
    data class CitiesLoaded(val cities: ImmutableList<GeoCity>) : SettingsIntent
    data object CitiesLoadFailed : SettingsIntent
    data class CityPicked(val city: GeoCity) : SettingsIntent

    data object OpenLanguageDialogClicked : SettingsIntent
    data object OpenUnitsDialogClicked : SettingsIntent
    data class LanguagePicked(val language: ApplicationLanguage) : SettingsIntent
    data class UnitsPicked(val metrics: ApplicationMetrics) : SettingsIntent

    data object DialogDismissed : SettingsIntent

    data class DarkModeToggled(val enabled: Boolean) : SettingsIntent

    data object BackClicked : SettingsIntent
    data object ExitClicked : SettingsIntent
}

data class SettingsState(
    val languageLabel: String = "",
    val unitsLabel: String = "",
    val versionLabel: String = "",
    val cityLabel: String = "",
    val currentCity: GeoCity? = null,
    val currentLanguage: ApplicationLanguage? = null,
    val currentMetrics: ApplicationMetrics? = null,
    val activeDialog: SettingsDialog = SettingsDialog.NONE,
    val languages: ImmutableList<ApplicationLanguage> = persistentListOf(),
    val metricsOptions: ImmutableList<ApplicationMetrics> = persistentListOf(),
    val isDarkMode: Boolean = false,
    val cityQuery: String = "",
    val citySuggestions: ImmutableList<GeoCity> = persistentListOf(),
    val isCitySearching: Boolean = false,
    val citySearchFailed: Boolean = false,
)

enum class SettingsDialog { NONE, CITY, LANGUAGE, UNITS }

sealed interface SettingsEffect : IaEffect {
    data object NavigateBack : SettingsEffect
    data object ExitApp : SettingsEffect
    data object RestartForLocale : SettingsEffect
    data object RestartForTheme : SettingsEffect
}
