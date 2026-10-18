package com.example.weatherapp.ui.screens.settings

import android.content.Context
import com.example.weatherapp.App
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.logic.LanguageLogic
import com.example.weatherapp.logic.WeatherFormatter
import com.example.weatherapp.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() :
    MviViewModel<SettingsIntent, SettingsState, SettingsEffect>() {

    init {
        dispatch(SettingsIntent.LoadInitial)
    }

    override fun initialState(): SettingsState = SettingsState()

    override fun reduce(state: SettingsState, intent: SettingsIntent): SettingsState =
        when (intent) {
            SettingsIntent.LoadInitial -> {
                val lang = App.setting.appLanguage
                val metrics = App.setting.appMetrics
                state.copy(
                    languageLabel = WeatherFormatter.formatLanguageLabel(
                        lang.language,
                        lang.languageKey
                    ),
                    unitsLabel = unitsLabel(metrics),
                    versionLabel = BuildConfig.VERSION_NAME,
                    currentLanguage = lang,
                    currentMetrics = metrics,
                    languages = LanguageLogic.availableLanguages(),
                    metricsOptions = ApplicationMetrics.getMetricsList(),
                    isDarkMode = App.setting.darkModeEnabled ?: false,
                    activeDialog = SettingsDialog.NONE,
                )
            }

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
            SettingsIntent.ExitClicked,
                -> state // только эффекты
        }

    override suspend fun handle(intent: SettingsIntent) {
        when (intent) {
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
