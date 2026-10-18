package com.example.weatherapp.logic

import com.example.weatherapp.extention.models.ApplicationLanguage
import kotlinx.collections.immutable.ImmutableList
import java.util.Locale

object LanguageLogic {

    fun localeTag(languageKey: String): String =
        if (languageKey.isBlank()) Locale.getDefault().language else languageKey.lowercase(Locale.ROOT)

    fun apiLangParam(languageKey: String): String = localeTag(languageKey)

    fun availableLanguages(): ImmutableList<ApplicationLanguage> = ApplicationLanguage.getAppLanguageList()

    fun findLanguage(languageKey: String): ApplicationLanguage? =
        availableLanguages().firstOrNull {
            it.languageKey.equals(languageKey, ignoreCase = true)
        }
}
