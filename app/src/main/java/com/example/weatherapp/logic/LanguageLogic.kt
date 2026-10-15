package com.example.weatherapp.logic

import com.example.weatherapp.extention.models.ApplicationLanguage
import java.util.Locale

/**
 * Чистая логика выбора локали приложения.
 *
 * Историческая неточность: languageKey хранился в верхнем регистре ("EN"),
 * а Locale и OpenWeatherMap ожидают ISO-639 код ("en"). Здесь — единственное
 * место нормализации; покрыто unit-тестами.
 */
object LanguageLogic {

    /** Нормализованный ISO-код языка для Locale/attachBaseContext ("EN" -> "en"). */
    fun localeTag(languageKey: String): String =
        if (languageKey.isBlank()) Locale.getDefault().language else languageKey.lowercase(Locale.ROOT)

    /** Значение параметра `lang` для API (OWM ожидает lowercase ISO-код). */
    fun apiLangParam(languageKey: String): String = localeTag(languageKey)

    /** Доступные языки приложения (список — единый источник для экрана настроек). */
    fun availableLanguages(): List<ApplicationLanguage> = ApplicationLanguage.getAppLanguageList()

    /** Поиск языка по ключу (case-insensitive); null, если не найден. */
    fun findLanguage(languageKey: String): ApplicationLanguage? =
        availableLanguages().firstOrNull {
            it.languageKey.equals(languageKey, ignoreCase = true)
        }
}
