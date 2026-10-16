package com.example.weatherapp.ui.screens.settings

import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.mvi.IaEffect
import com.example.weatherapp.mvi.IaIntent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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

    data object BackClicked : SettingsIntent
    data object ExitClicked : SettingsIntent
}

data class SettingsState(
    val languageLabel: String = "",
    val unitsLabel: String = "",
    val versionLabel: String = "",
    val currentLanguage: ApplicationLanguage? = null,
    val currentMetrics: ApplicationMetrics? = null,
    /** Активный диалог выбора — часть состояния: восстанавливается после ротации. */
    val activeDialog: SettingsDialog = SettingsDialog.NONE,
    val languages: ImmutableList<ApplicationLanguage> = persistentListOf(),
    val metricsOptions: ImmutableList<ApplicationMetrics> = persistentListOf(),
)

enum class SettingsDialog { NONE, LANGUAGE, UNITS }

sealed interface SettingsEffect : IaEffect {
    data object NavigateBack : SettingsEffect
    data object ExitApp : SettingsEffect

    /** Язык сохранён — нужен перезапуск Activity с новой локалью. */
    data object RestartForLocale : SettingsEffect
}
