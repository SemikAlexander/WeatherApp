package com.example.weatherapp.extention.models

import com.example.weatherapp.App
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ApplicationLanguage(
    val language: String,
    val languageKey: String
) {
    companion object {
        fun setAppLanguageDefault(): ApplicationLanguage {
            return ApplicationLanguage(
                language = "English",
                languageKey = "EN"
            )
        }

        fun setAppLanguage(
            language: ApplicationLanguage,
            onReloadApp: (ApplicationLanguage) -> Unit = {}
        ) {
            App.setting.appLanguage = language
            onReloadApp(App.setting.appLanguage)
        }

        fun getAppLanguageList(): ImmutableList<ApplicationLanguage> {
            return persistentListOf(
                ApplicationLanguage(
                    language = "English",
                    languageKey = "EN"
                ),
                ApplicationLanguage(
                    language = "Russian",
                    languageKey = "RU"
                ),
                ApplicationLanguage(
                    language = "Spanish",
                    languageKey = "ES"
                ),
                ApplicationLanguage(
                    language = "French",
                    languageKey = "FR"
                )
            )
        }
    }
}
