package com.example.weatherapp.ui.fragments.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.weatherapp.App
import com.example.weatherapp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    /**
     * Событие «язык изменён, нужен перезапуск с новой локалью».
     *
     * Раньше для этого использовался статический MutableLiveData в MainActivity —
     * антипаттерн: состояние жизненного цикла UI хранилось в глобальной переменной.
     * Теперь это однократное событие (SharedFlow), на которое подписан фрагмент
     * в своём lifecycleScope — подписка умирает вместе со view.
     */
    private val _restartEvent = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val restartEvent: SharedFlow<Unit> = _restartEvent.asSharedFlow()

    /**
     * Язык уже сохранён в App.setting во фрагменте; локаль применяется
     * в MainActivity.attachBaseContext при следующем старте Activity,
     * сюда остаётся только послать сигнал на перезапуск.
     */
    fun changeLocale() {
        _restartEvent.tryEmit(Unit)
    }

    fun getUnit(context: Context): String {
        return when (App.setting.appMetrics.metrics) {
            "standard" -> "${context.getString(R.string.kelvin)}, °${App.setting.appMetrics.metricsKey}"
            "metric" -> "${context.getString(R.string.celsius)}, °${App.setting.appMetrics.metricsKey}"
            else -> "${context.getString(R.string.fahrenheit)}, °${App.setting.appMetrics.metricsKey}"
        }
    }
}
