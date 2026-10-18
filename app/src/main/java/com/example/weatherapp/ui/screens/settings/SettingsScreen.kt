package com.example.weatherapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherapp.R
import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.logic.WeatherFormatter
import com.example.weatherapp.ui.components.CircleIconButton
import com.example.weatherapp.ui.components.SelectableItemRow
import com.example.weatherapp.ui.components.SettingsRow
import com.example.weatherapp.ui.components.SpacerHeight
import com.example.weatherapp.ui.components.SwitchSettingRow
import com.example.weatherapp.ui.screens.home.LaunchedEffectUnit
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.collections.immutable.persistentListOf

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
        onOpenLanguage = { viewModel.dispatch(SettingsIntent.OpenLanguageDialogClicked) },
        onOpenUnits = { viewModel.dispatch(SettingsIntent.OpenUnitsDialogClicked) },
        onBack = { viewModel.dispatch(SettingsIntent.BackClicked) },
        onExit = { viewModel.dispatch(SettingsIntent.ExitClicked) },
        onLanguagePicked = { viewModel.dispatch(SettingsIntent.LanguagePicked(it)) },
        onUnitsPicked = { viewModel.dispatch(SettingsIntent.UnitsPicked(it)) },
        onDismissDialog = { viewModel.dispatch(SettingsIntent.DialogDismissed) },
        onDarkModeToggled = { viewModel.dispatch(SettingsIntent.DarkModeToggled(it)) },
    )
}

object SettingsUI {

    @Composable
    fun Root(
        state: SettingsState,
        onOpenLanguage: () -> Unit,
        onOpenUnits: () -> Unit,
        onBack: () -> Unit,
        onExit: () -> Unit,
        onLanguagePicked: (ApplicationLanguage) -> Unit,
        onUnitsPicked: (ApplicationMetrics) -> Unit,
        onDismissDialog: () -> Unit,
        onDarkModeToggled: (Boolean) -> Unit = {},
    ) {
        val context = LocalContext.current
        val localizedUnitsLabel = state.currentMetrics?.let {
            WeatherFormatter.formatUnitLabel(
                ScaleNames.localized(context, it.metrics),
                it.metricsKey,
            )
        } ?: state.unitsLabel

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderRow(onBack = onBack)


                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(size = 30.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SettingsRow(
                        title = stringResource(R.string.lang_app),
                        value = state.languageLabel,
                        onClick = onOpenLanguage,
                    )
                    SettingsRow(
                        title = stringResource(R.string.temp_app),
                        value = localizedUnitsLabel,
                        onClick = onOpenUnits,
                    )
                    SwitchSettingRow(
                        title = stringResource(R.string.dark_mode),
                        checked = state.isDarkMode,
                        onCheckedChange = onDarkModeToggled,
                    )
                    SettingsRow(
                        title = stringResource(R.string.exit),
                        value = null,
                        onClick = onExit,
                    )
                    VersionFooter(state.versionLabel)
                    SpacerHeight(10)
                }
            }

            when (state.activeDialog) {
                SettingsDialog.LANGUAGE -> SelectionDialog(
                    title = stringResource(R.string.lang_app),
                    items = state.languages.map {
                        WeatherFormatter.formatLanguageLabel(it.language, it.languageKey)
                    },
                    selectedIndex = state.languages.indexOf(state.currentLanguage),
                    onPick = { index -> onLanguagePicked(state.languages[index]) },
                    onDismiss = onDismissDialog,
                )

                SettingsDialog.UNITS -> SelectionDialog(
                    title = stringResource(R.string.temp_app),
                    items = state.metricsOptions.map {
                        "${ScaleNames.localized(context, it.metrics)}, °${it.metricsKey}"
                    },
                    selectedIndex = state.metricsOptions.indexOf(state.currentMetrics),
                    onPick = { index -> onUnitsPicked(state.metricsOptions[index]) },
                    onDismiss = onDismissDialog,
                )

                SettingsDialog.NONE -> Unit
            }
        }
    }

    @Composable
    fun HeaderRow(onBack: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            CircleIconButton(
                iconResId = R.drawable.ic_back,
                contentDescription = stringResource(R.string.back),
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }

    /** Универсальный диалог выбора (язык/единицы) — замена голодного Dialog+RecyclerView. */
    @Composable
    fun SelectionDialog(
        title: String,
        items: List<String>,
        selectedIndex: Int,
        onPick: (Int) -> Unit,
        onDismiss: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.save_button))
                }
            },
            title = { Text(title) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    items.forEachIndexed { index, name ->
                        SelectableItemRow(
                            name = name,
                            isSelected = index == selectedIndex,
                            onClick = { onPick(index) },
                        )
                    }
                }
            },
        )
    }

    @Composable
    fun VersionFooter(version: String) {
        Text(
            text = stringResource(R.string.version_name, version),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

// region Previews

private val PREVIEW_SETTINGS_STATE = SettingsState(
    languageLabel = "English, EN",
    unitsLabel = "Celsius, °C",
    versionLabel = "1.0.0",
    currentLanguage = ApplicationLanguage("English", "EN"),
    currentMetrics = ApplicationMetrics("metric", "C"),
    languages = persistentListOf(
        ApplicationLanguage("English", "EN"),
        ApplicationLanguage("Russian", "RU"),
    ),
    metricsOptions = ApplicationMetrics.getMetricsList(),
)

// region LIGHT THEME

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    WeatherAppTheme {
        SettingsUI.Root(
            state = PREVIEW_SETTINGS_STATE,
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onLanguagePicked = {},
            onUnitsPicked = {},
            onDismissDialog = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDialogPreview() {
    WeatherAppTheme {
        SettingsUI.Root(
            state = PREVIEW_SETTINGS_STATE.copy(activeDialog = SettingsDialog.LANGUAGE),
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onLanguagePicked = {},
            onUnitsPicked = {},
            onDismissDialog = {},
        )
    }
}

// endregion

// region DARK THEME

@Preview(name = "Dark", showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        SettingsUI.Root(
            state = PREVIEW_SETTINGS_STATE,
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onLanguagePicked = {},
            onUnitsPicked = {},
            onDismissDialog = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun SettingsDialogDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        SettingsUI.Root(
            state = PREVIEW_SETTINGS_STATE.copy(activeDialog = SettingsDialog.LANGUAGE),
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onLanguagePicked = {},
            onUnitsPicked = {},
            onDismissDialog = {},
        )
    }
}

// endregion

// endregion Previews