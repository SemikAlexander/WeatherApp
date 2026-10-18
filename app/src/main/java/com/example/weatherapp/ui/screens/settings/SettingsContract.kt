package com.example.weatherapp.ui.screens.settings

import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.mvi.IaEffect
import com.example.weatherapp.mvi.IaIntent

/** MVI-контракт экрана Settings. */

sealed interface SettingsIntent : IaIntent {
    /** Первый вход: прочитать текущие язык/единицы/версию из настроек. */
    data object LoadInitial : SettingsIntent

    data object OpenLanguageDialogClicked : SettingsIntent
    data object OpenUnitsDialogClicked : SettingsIntent
    data class LanguagePicked(val language: ApplicationLanguage) : SettingsIntent
    data class UnitsPicked(val metrics: ApplicationMetrics) : SettingsIntent

    /** Диалог закрыт кнопкой «крестик»/back. */
    data object DialogDismissed : SettingsIntent

    /** Переключатель тёмной темы (вкл/выкл). */
    data class DarkModeToggled(val enabled: Boolean) : SettingsIntent

    data object BackClicked : SettingsIntent
    data object ExitClicked : SettingsIntent
}

data class SettingsState(
    val languageLabel: String = "",
    val unitsLabel: String = "",
    val versionLabel: String = "",
    val currentLanguage: ApplicationLanguage? = null,
    val currentMetrics: ApplicationMetrics? = null,
    val activeDialog: SettingsDialog = SettingsDialog.NONE,
    val languages: List<ApplicationLanguage> = emptyList(),
    val metricsOptions: List<ApplicationMetrics> = emptyList(),
    val isDarkMode: Boolean = false,
)

enum class SettingsDialog { NONE, LANGUAGE, UNITS }

sealed interface SettingsEffect : IaEffect {
    data object NavigateBack : SettingsEffect
    data object ExitApp : SettingsEffect
    data object RestartForLocale : SettingsEffect
    data object RestartForTheme : SettingsEffect
}
