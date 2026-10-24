package com.example.weatherapp.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherapp.ui.screens.home.LaunchedEffectUnit

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onExitApp: () -> Unit,
    onRestartForLocale: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffectUnit(viewModel.effects) { effect ->
        when (effect) {
            SettingsEffect.NavigateBack -> onNavigateBack()
            SettingsEffect.ExitApp -> onExitApp()
            SettingsEffect.RestartForLocale,
            SettingsEffect.RestartForTheme -> onRestartForLocale()
        }
    }

    SettingsUI.Root(
        state = state,
        onOpenCity = { viewModel.dispatch(SettingsIntent.OpenCityDialogClicked) },
        onOpenLanguage = { viewModel.dispatch(SettingsIntent.OpenLanguageDialogClicked) },
        onOpenUnits = { viewModel.dispatch(SettingsIntent.OpenUnitsDialogClicked) },
        onBack = { viewModel.dispatch(SettingsIntent.BackClicked) },
        onExit = { viewModel.dispatch(SettingsIntent.ExitClicked) },
        onCityPicked = { viewModel.dispatch(SettingsIntent.CityPicked(it)) },
        onCityQueryChange = { viewModel.dispatch(SettingsIntent.CityQueryChanged(it)) },
        onCitySearch = { viewModel.dispatch(SettingsIntent.CitySearchSubmitted) },
        onLanguagePicked = { viewModel.dispatch(SettingsIntent.LanguagePicked(it)) },
        onUnitsPicked = { viewModel.dispatch(SettingsIntent.UnitsPicked(it)) },
        onDismissDialog = { viewModel.dispatch(SettingsIntent.DialogDismissed) },
        onDarkModeToggled = { viewModel.dispatch(SettingsIntent.DarkModeToggled(it)) },
    )
}
