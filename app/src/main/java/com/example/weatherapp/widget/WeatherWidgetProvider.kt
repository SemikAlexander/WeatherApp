package com.example.weatherapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.weatherapp.App
import com.example.weatherapp.R
import com.example.weatherapp.logic.WeatherFormatter
import com.example.weatherapp.logic.isDarkThemeSelected
import com.example.weatherapp.ui.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        if (appWidgetIds.isEmpty()) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // Если сеть недоступна, оставляем последнюю успешную картинку виджета.
                val snapshot = loadSnapshot(appContext) ?: return@launch
                val views = buildRemoteViews(appContext, snapshot)
                appWidgetIds.forEach { appWidgetId ->
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                // Ошибка обновления виджета не должна ронять приложение.
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun loadSnapshot(context: Context): WidgetSnapshot? = runCatching {
        val city = App.setting.selectedCity
        val entryPoint = EntryPointAccessors.fromApplication(
            context = context,
            entryPoint = WeatherWidgetEntryPoint::class.java,
        )

        val weather = entryPoint.weatherService().getWeather(
            latitude = city.lat,
            longitude = city.lon,
        )
        val units = WeatherFormatter.unitSymbol(App.setting.appMetrics.metrics)
        val description = weather.weather?.firstOrNull()?.description

        WidgetSnapshot(
            updatedAt = WeatherFormatter.formatDate(weather.dt, DATE_PATTERN_UPDATE),
            status = WeatherFormatter.capitalizeDescription(description),
            temperature = weather.main?.let {
                WeatherFormatter.formatTemperature(it.temp, units)
            }.orEmpty(),
            icon = loadIcon(weather.getWeatherIcon()),
        )
    }.getOrNull()

    private fun loadIcon(url: String): Bitmap? {
        if (url.isBlank()) return null
        return runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = ICON_TIMEOUT_MS
            connection.readTimeout = ICON_TIMEOUT_MS
            connection.instanceFollowRedirects = true
            try {
                connection.inputStream.use { BitmapFactory.decodeStream(it) }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private fun buildRemoteViews(context: Context, snapshot: WidgetSnapshot): RemoteViews {
        val darkTheme = isDarkThemeSelected(context)
        val primaryColor = ContextCompat.getColor(
            context,
            if (darkTheme) R.color.widget_text_primary_dark else R.color.widget_text_primary_light,
        )
        val secondaryColor = ContextCompat.getColor(
            context,
            if (darkTheme) R.color.widget_text_secondary_dark else R.color.widget_text_secondary_light,
        )

        return RemoteViews(context.packageName, R.layout.widget_weather).apply {
            setInt(
                R.id.widget_root,
                "setBackgroundResource",
                if (darkTheme) R.drawable.widget_background_dark else R.drawable.widget_background_light,
            )
            setTextColor(R.id.widget_updated_at, secondaryColor)
            setTextColor(R.id.widget_status, primaryColor)
            setTextColor(R.id.widget_temperature, primaryColor)

            setTextViewText(
                R.id.widget_updated_at,
                snapshot.updatedAt.ifBlank { context.getString(R.string.widget_no_data) },
            )
            setTextViewText(
                R.id.widget_status,
                snapshot.status.ifBlank { context.getString(R.string.widget_no_data) },
            )
            setTextViewText(
                R.id.widget_temperature,
                snapshot.temperature.ifBlank { EMPTY_TEMPERATURE },
            )

            if (snapshot.icon != null) {
                setImageViewBitmap(R.id.widget_icon, snapshot.icon)
                setViewVisibility(R.id.widget_icon, View.VISIBLE)
            } else {
                setViewVisibility(R.id.widget_icon, View.INVISIBLE)
            }

            setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent(context))
        }
    }

    private fun openAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private data class WidgetSnapshot(
        val updatedAt: String,
        val status: String,
        val temperature: String,
        val icon: Bitmap?,
    )

    companion object {
        private const val DATE_PATTERN_UPDATE = "dd/MM/yyyy hh:mm a"
        private const val ICON_TIMEOUT_MS = 10_000
        private const val EMPTY_TEMPERATURE = "—"

        fun requestUpdate(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, WeatherWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            if (appWidgetIds.isEmpty()) return

            context.sendBroadcast(
                Intent(context, WeatherWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                },
            )
        }
    }
}
