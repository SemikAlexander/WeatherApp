package com.example.weatherapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapp.R
import com.example.weatherapp.api.models.response.geocoding.GeoCity
import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.logic.WeatherFormatter
import com.example.weatherapp.ui.components.CircleIconButton
import com.example.weatherapp.ui.components.SelectableItemRow
import com.example.weatherapp.ui.components.SettingsRow
import com.example.weatherapp.ui.components.SpacerHeight
import com.example.weatherapp.ui.components.SwitchSettingRow
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

object SettingsUI {

    @Composable
    fun Root(
        state: SettingsState,
        onOpenCity: () -> Unit,
        onOpenLanguage: () -> Unit,
        onOpenUnits: () -> Unit,
        onBack: () -> Unit,
        onExit: () -> Unit,
        onCityPicked: (GeoCity) -> Unit,
        onCityQueryChange: (String) -> Unit,
        onCitySearch: () -> Unit,
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
                        title = stringResource(R.string.city),
                        value = state.cityLabel,
                        onClick = onOpenCity,
                    )
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
                SettingsDialog.CITY -> CityDialog(
                    query = state.cityQuery,
                    cities = state.citySuggestions,
                    selectedCity = state.currentCity,
                    isSearching = state.isCitySearching,
                    hasError = state.citySearchFailed,
                    onQueryChange = onCityQueryChange,
                    onSearch = onCitySearch,
                    onPick = onCityPicked,
                    onDismiss = onDismissDialog,
                )

                SettingsDialog.LANGUAGE -> SelectionDialog(
                    title = stringResource(R.string.lang_app),
                    items = state.languages.map {
                        WeatherFormatter.formatLanguageLabel(it.language, it.languageKey)
                    }.toPersistentList(),
                    selectedIndex = state.languages.indexOf(state.currentLanguage),
                    onPick = { index -> onLanguagePicked(state.languages[index]) },
                    onDismiss = onDismissDialog,
                )

                SettingsDialog.UNITS -> SelectionDialog(
                    title = stringResource(R.string.temp_app),
                    items = state.metricsOptions.map {
                        "${ScaleNames.localized(context, it.metrics)}, °${it.metricsKey}"
                    }.toPersistentList(),
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

    /**
     * Диалог выбора города: live-поиск по мере ввода (с debounce в ViewModel)
     * и отправка запроса клавишей Search на клавиатуре.
     */
    @Composable
    fun CityDialog(
        query: String,
        cities: ImmutableList<GeoCity>,
        selectedCity: GeoCity?,
        isSearching: Boolean,
        hasError: Boolean,
        onQueryChange: (String) -> Unit,
        onSearch: () -> Unit,
        onPick: (GeoCity) -> Unit,
        onDismiss: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            },
            title = { Text(stringResource(R.string.city)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.city_search_hint)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        },
                    )
                    SpacerHeight(8)
                    when {
                        hasError -> Text(
                            text = stringResource(R.string.city_search_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )

                        cities.isEmpty() && !isSearching &&
                            query.trim().length >= SettingsViewModel.MIN_CITY_QUERY_LENGTH -> Text(
                            text = stringResource(R.string.city_not_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )

                        else -> Column(
                            modifier = Modifier
                                .heightIn(max = 280.dp)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            cities.forEach { city ->
                                SelectableItemRow(
                                    name = city.displayName,
                                    isSelected = city == selectedCity,
                                    onClick = { onPick(city) },
                                )
                            }
                        }
                    }
                }
            },
        )
    }

    @Composable
    fun SelectionDialog(
        title: String,
        items: ImmutableList<String>,
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

private val PREVIEW_CITIES = persistentListOf(
    GeoCity("London", 51.5073219, -0.1276474, "GB", "England"),
    GeoCity("London", 42.9832406, -81.243372, "CA", "Ontario"),
    GeoCity("Londonderry", 42.865087, -71.37395, "US", "New Hampshire"),
)

private val PREVIEW_SETTINGS_STATE = SettingsState(
    languageLabel = "English, EN",
    unitsLabel = "Celsius, °C",
    versionLabel = "1.0.0",
    cityLabel = "London, England, GB",
    currentCity = GeoCity.DEFAULT,
    currentLanguage = ApplicationLanguage("English", "EN"),
    currentMetrics = ApplicationMetrics("metric", "C"),
    languages = persistentListOf(
        ApplicationLanguage("English", "EN"),
        ApplicationLanguage("Russian", "RU"),
    ),
    metricsOptions = ApplicationMetrics.getMetricsList(),
)

private val PREVIEW_CITY_DIALOG_STATE = PREVIEW_SETTINGS_STATE.copy(
    activeDialog = SettingsDialog.CITY,
    cityQuery = "London",
    citySuggestions = PREVIEW_CITIES,
)

// region LIGHT THEME

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    WeatherAppTheme {
        SettingsUI.Root(
            state = PREVIEW_SETTINGS_STATE,
            onOpenCity = {},
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onCityPicked = {},
            onCityQueryChange = {},
            onCitySearch = {},
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
            onOpenCity = {},
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onCityPicked = {},
            onCityQueryChange = {},
            onCitySearch = {},
            onLanguagePicked = {},
            onUnitsPicked = {},
            onDismissDialog = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityDialogPreview() {
    WeatherAppTheme {
        SettingsUI.Root(
            state = PREVIEW_CITY_DIALOG_STATE,
            onOpenCity = {},
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onCityPicked = {},
            onCityQueryChange = {},
            onCitySearch = {},
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
            onOpenCity = {},
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onCityPicked = {},
            onCityQueryChange = {},
            onCitySearch = {},
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
            onOpenCity = {},
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onCityPicked = {},
            onCityQueryChange = {},
            onCitySearch = {},
            onLanguagePicked = {},
            onUnitsPicked = {},
            onDismissDialog = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun CityDialogDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        SettingsUI.Root(
            state = PREVIEW_CITY_DIALOG_STATE,
            onOpenCity = {},
            onOpenLanguage = {},
            onOpenUnits = {},
            onBack = {},
            onExit = {},
            onCityPicked = {},
            onCityQueryChange = {},
            onCitySearch = {},
            onLanguagePicked = {},
            onUnitsPicked = {},
            onDismissDialog = {},
        )
    }
}

// endregion

// endregion Previews
