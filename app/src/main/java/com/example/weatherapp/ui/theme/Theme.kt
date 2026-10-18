package com.example.weatherapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.weatherapp.App

// Палитра перенесена из values/colors.xml — единый источник для Compose-экранов.
// Legacy-палитра: точечные цвета, ещё не переведённые на MaterialTheme-роли
// (HomeUI/SettingsScreen). Убираются по мере миграции экранов на colorScheme.
val c212121 = Color(0xFF212121)
val c323233 = Color(0xFF323233)
val cFAFAFA = Color(0xFFFAFAFA)
val c006485 = Color(0xFF006485)
val c757575 = Color(0xFF757575)
val c9e9e9e = Color(0xFF9E9E9E)
val cf9f9fa = Color(0xFFF9F9FA)

private val LightColors = lightColorScheme(
    primary = c757575,
    secondary = c9e9e9e,
    background = Color.White,
    surface = cf9f9fa,
)

private val DarkColors = darkColorScheme(
    primary = Color.White,
    secondary = cFAFAFA,
    background = c212121,
    surface = c323233,
)

/**
 * Типографика экранов: размеры перенесены из XML-вёрстки (32sp заголовки,
 * 22sp пункты настроек, 12/14sp подписи инфо-плиток).
 */
private val WeatherTypography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal),
    bodyLarge = TextStyle(fontSize = 17.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelSmall = TextStyle(fontSize = 12.sp),
)

/**
 * Единая точка чтения настройки темы: null -> системная тема,
 * true/false -> принудительная. Используется и в рантайме, и в @Preview.
 */
@Composable
fun resolveDarkTheme(): Boolean {
    val stored = runCatching { App.setting.darkModeEnabled }.getOrNull()
    return stored ?: isSystemInDarkTheme()
}

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WeatherTypography,
        content = content,
    )
}
