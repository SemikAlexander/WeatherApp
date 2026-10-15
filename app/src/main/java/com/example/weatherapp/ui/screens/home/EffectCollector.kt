package com.example.weatherapp.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

/**
 * Однократная обработка потока эффектов (MVI Effect).
 * key = Unit: подписка создаётся один раз на композицию; SharedFlow(replay=0)
 * не отдаёт старые значения при рекомпозиции.
 */
@Composable
fun <T> LaunchedEffectUnit(flow: Flow<T>, handler: (T) -> Unit) {
    LaunchedEffect(Unit) {
        flow.collect(handler)
    }
}
