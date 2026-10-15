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

// Палитра перенесена из values/colors.xml — единый источник для Compose-экранов.
val WeatherBlue = Color(0xFF0099CC)      // mainBackground
val WeatherBlueDark = Color(0xFF006485)  // backBG / accent
val InfoTabBackground = Color(0x3CF1EBF1) // mainBackgroundInfoTab (полупрозрачный)
val ForecastStroke = Color(0xFFEEEEEE)

private val LightColors = lightColorScheme(
    primary = WeatherBlue,
    secondary = WeatherBlueDark,
    background = WeatherBlue,
    surface = WeatherBlueDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = WeatherBlue,
    secondary = WeatherBlueDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
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

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WeatherTypography,
        content = content,
    )
}
