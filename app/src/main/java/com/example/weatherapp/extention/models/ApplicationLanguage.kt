package com.example.weatherapp.extention.models

import com.example.weatherapp.App

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

        fun getAppLanguageList(): List<ApplicationLanguage> {
            return listOf(
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
