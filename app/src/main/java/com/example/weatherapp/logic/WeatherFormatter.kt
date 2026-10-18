package com.example.weatherapp.logic

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object WeatherFormatter {

    const val METRIC_STANDARD = "standard"
    const val METRIC_METRIC = "metric"
    const val METRIC_IMPERIAL = "imperial"

    const val DEGREE = "°"
    const val EMPTY = ""

    const val SCALE_KELVIN = 0
    const val SCALE_CELSIUS = 1
    const val SCALE_FAHRENHEIT = 2

    fun unitSymbol(metrics: String): String = when (metrics) {
        METRIC_STANDARD -> "K"
        METRIC_METRIC -> "C"
        else -> "F"
    }

    fun windSpeedSymbol(metrics: String): String = when (metrics) {
        METRIC_STANDARD -> "miles/hour"
        METRIC_METRIC -> "met/sec"
        else -> "met/sec"
    }

    fun scaleIndex(metrics: String): Int = when (metrics) {
        METRIC_STANDARD -> SCALE_KELVIN
        METRIC_METRIC -> SCALE_CELSIUS
        else -> SCALE_FAHRENHEIT
    }

    fun formatTemperature(value: Double, unit: String): String =
        "${value.roundToInt()}$DEGREE$unit"

    fun formatTemperatureTruncated(value: Double, unit: String): String =
        "${value.toInt()}$DEGREE$unit"

    fun formatDate(epochSeconds: Long?, pattern: String): String {
        if (epochSeconds == null) return EMPTY
        return SimpleDateFormat(pattern, Locale.ENGLISH).format(Date(epochSeconds * 1000L))
    }

    fun capitalizeDescription(description: String?): String =
        description?.replaceFirstChar { it.uppercase(Locale.ROOT) } ?: EMPTY

    fun formatUnitLabel(scaleName: String, metricsKey: String): String =
        "$scaleName, $DEGREE$metricsKey"

    fun formatLanguageLabel(language: String, languageKey: String): String {
        if (language.isBlank()) return languageKey
        return "$language, $languageKey"
    }
}
