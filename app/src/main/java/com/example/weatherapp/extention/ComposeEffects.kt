package com.example.weatherapp.extention

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Показ Snackbar из синхронного колбэка (обработчик MVI-эффектов — не суспендируемый).
 *
 * showSnackbar() суспендируется до dismiss, поэтому запуск идёт в composition-scoped
 * корутине: отменяется вместе с композицией, утечек нет. Предыдущий показ явно
 * сбрасывается — сообщения не перекрывают друг друга.
 */
@Composable
fun rememberShowSnackbar(snackbarHostState: SnackbarHostState): (String) -> Unit {
    val scope = rememberCoroutineScope()
    return { message ->
        if (message.isNotBlank()) {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
        }
    }
}
