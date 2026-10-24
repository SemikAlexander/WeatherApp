package com.example.weatherapp.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit,
    onNetworkError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.dispatch(HomeIntent.ScreenShown)
    }

    LaunchedEffectUnit(viewModel.effects) { effect ->
        when (effect) {
            HomeEffect.NavigateToSettings -> onOpenSettings()
            HomeEffect.ShowNetworkError -> onNetworkError()
        }
    }

    HomeUI.Root(
        state = state,
        onRefresh = { viewModel.dispatch(HomeIntent.RefreshWeather) },
        onOpenSettings = { viewModel.dispatch(HomeIntent.OpenSettingsClicked) },
        onRetry = { viewModel.dispatch(HomeIntent.RetryClicked) },
    )
}
