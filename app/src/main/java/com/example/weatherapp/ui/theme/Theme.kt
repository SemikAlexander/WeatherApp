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

val InfoTabBackground = Color(0x3CF1EBF1)

val c212121 = Color(0xFF212121)
val c323233 = Color(0xFF323233)
val cFAFAFA = Color(0xFFFAFAFA)
val c0099CC = Color(0xFF0099CC)
val c006485 = Color(0xFF006485)

private val LightColors = lightColorScheme(
    primary = c0099CC,
    secondary = c006485,
    background = c0099CC,
    surface = c006485,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = c0099CC,
    secondary = c006485,
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
