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

/**
 * MVI-ViewModel экрана Settings.
 *
 * Отличия от legacy:
 *  - вместо императивного setContent() после каждого изменения — реактивный
 *    [SettingsState], пересчитываемый редьюсером;
 *  - активный диалог — часть состояния: переживает ротацию (голодный Dialog — нет);
 *  - форматирование подписей делегировано чистым WeatherFormatter/LanguageLogic
 *    и покрыто unit-тестами; Context.getString не используется вовсе (см. ScaleNames).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() :
    MviViewModel<SettingsIntent, SettingsState, SettingsEffect>() {

    override fun initialState(): SettingsState = SettingsState()

    override fun reduce(state: SettingsState, intent: SettingsIntent): SettingsState = when (intent) {
        SettingsIntent.LoadInitial -> {
            val lang = App.setting.appLanguage
            val metrics = App.setting.appMetrics
            state.copy(
                languageLabel = WeatherFormatter.formatLanguageLabel(lang.language, lang.languageKey),
                unitsLabel = unitsLabel(metrics),
                versionLabel = BuildConfig.VERSION_NAME,
                currentLanguage = lang,
                currentMetrics = metrics,
                languages = LanguageLogic.availableLanguages(),
                metricsOptions = ApplicationMetrics.getMetricsList(),
                activeDialog = SettingsDialog.NONE,
            )
        }

        SettingsIntent.OpenLanguageDialogClicked ->
            state.copy(activeDialog = SettingsDialog.LANGUAGE)

        SettingsIntent.OpenUnitsDialogClicked ->
            state.copy(activeDialog = SettingsDialog.UNITS)

        SettingsIntent.DialogDismissed ->
            state.copy(activeDialog = SettingsDialog.NONE)

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

            SettingsIntent.BackClicked -> emitEffect(SettingsEffect.NavigateBack)
            SettingsIntent.ExitClicked -> emitEffect(SettingsEffect.ExitApp)

            else -> Unit
        }
    }

    /**
     * Подписка единиц: "Celsius, °C". Название шкалы берётся из [ScaleNames] —
     * ViewModel принципиально не держит Context (утечка + непроверяемость).
     * Локализованный резолв делает UI-слой через R.string.* (см. SettingsUI).
     */
    private fun unitsLabel(metrics: ApplicationMetrics): String =
        WeatherFormatter.formatUnitLabel(ScaleNames.nameOf(metrics.metrics), metrics.metricsKey)
}

/** Названия температурных шкал; синхронизировано с values/strings.xml (en). */
object ScaleNames {
    fun nameOf(metrics: String): String = when (metrics) {
        WeatherFormatter.METRIC_STANDARD -> "Kelvin"
        WeatherFormatter.METRIC_METRIC -> "Celsius"
        else -> "Fahrenheit"
    }

    /** Resource id для локализованного отображения в Compose-слое. */
    fun resIdOf(metrics: String): Int = when (metrics) {
        WeatherFormatter.METRIC_STANDARD -> R.string.kelvin
        WeatherFormatter.METRIC_METRIC -> R.string.celsius
        else -> R.string.fahrenheit
    }

    fun localized(context: Context, metrics: String): String =
        context.getString(resIdOf(metrics))
}
