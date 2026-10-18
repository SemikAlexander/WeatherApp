package com.example.weatherapp.extention

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
