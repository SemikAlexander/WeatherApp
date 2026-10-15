package com.example.weatherapp.logic

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Чистая (не зависящая от Android-ресурсов) логика форматирования погодных данных.
 *
 * Зачем отдельный объект:
 *  1) `when` по единицам измерения был продублирован в HomeFragment и SettingsViewModel —
 *     здесь единственная реализация, переиспользуемая обоими экранами;
 *  2) нет зависимостей от Context/R — покрывается unit-тестами без Robolectric/эмулятора.
 */
object WeatherFormatter {

    const val METRIC_STANDARD = "standard"
    const val METRIC_METRIC = "metric"
    const val METRIC_IMPERIAL = "imperial"

    /** Символ градуса (в res не храним из-за различий кодировок). */
    const val DEGREE = "°"
    const val EMPTY = ""

    /** Шкалы для подписи в настройках — индексы строковых ресурсов (маппит UI-слой). */
    const val SCALE_KELVIN = 0
    const val SCALE_CELSIUS = 1
    const val SCALE_FAHRENHEIT = 2

    /**
     * Символ единицы по ключу метрики API (OWM: standard/metric/imperial).
     * Неизвестное значение -> F (сохраняет legacy-поведение `else -> "F"`).
     */
    fun unitSymbol(metrics: String): String = when (metrics) {
        METRIC_STANDARD -> "K"
        METRIC_METRIC -> "C"
        else -> "F"
    }

    /** Индекс названия шкалы (UI резолвит в R.string.kelvin/celsius/fahrenheit). */
    fun scaleIndex(metrics: String): Int = when (metrics) {
        METRIC_STANDARD -> SCALE_KELVIN
        METRIC_METRIC -> SCALE_CELSIUS
        else -> SCALE_FAHRENHEIT
    }

    /** Основная температура: округление к ближайшему целому (legacy: roundToInt). */
    fun formatTemperature(value: Double, unit: String): String =
        "${value.roundToInt()}$DEGREE$unit"

    /** Min/Max температура: legacy использовал усечение (toInt()) — семантику сохраняем. */
    fun formatTemperatureTruncated(value: Double, unit: String): String =
        "${value.toInt()}$DEGREE$unit"

    /** Дата из unix-time (секунды) по шаблону; null -> пустая строка (краш недопустим). */
    fun formatDate(epochSeconds: Long?, pattern: String): String {
        if (epochSeconds == null) return EMPTY
        return SimpleDateFormat(pattern, Locale.ENGLISH).format(Date(epochSeconds * 1000L))
    }

    /** Описание погоды с заглавной буквы ("broken clouds" -> "Broken clouds"). */
    fun capitalizeDescription(description: String?): String =
        description?.replaceFirstChar { it.uppercase(Locale.ROOT) } ?: EMPTY

    /** Подписка единиц в настройках: "<шкала>, °<key>" (например "Celsius, °C"). */
    fun formatUnitLabel(scaleName: String, metricsKey: String): String =
        "$scaleName, $DEGREE$metricsKey"

    /** Язык + его ключ для настроек: "English, EN". Пустой язык -> EMPTY (без "null"). */
    fun formatLanguageLabel(language: String, languageKey: String): String {
        if (language.isBlank()) return languageKey
        return "$language, $languageKey"
    }
}
