package com.example.weatherapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weatherapp.R
import com.example.weatherapp.api.models.response.cities.CitiesItem
import com.example.weatherapp.ui.components.CircleIconButton
import com.example.weatherapp.ui.components.InfoTile
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.ui.theme.WeatherBlue

/**
 * Экран Home (Compose). Все элементы UI — в объекте-неймспейсе [MainUI]
 * (правило проекта): переиспользуемые кирпичи живут в ui/components, а
 * композиция конкретного экрана инкапсулирована здесь.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit,
    onNetworkError: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Эффекты: навигация/тост — однократные, не состояние.
    LaunchedEffectUnit(viewModel.effects) { effect ->
        when (effect) {
            HomeEffect.NavigateToSettings -> onOpenSettings()
            HomeEffect.ShowNetworkError -> onNetworkError()
        }
    }

    MainUI.Root(
        state = state,
        onQueryChange = { viewModel.dispatch(HomeIntent.CityQueryChanged(it)) },
        onSearchSubmit = { viewModel.dispatch(HomeIntent.SearchSubmitted(it)) },
        onCityPicked = { viewModel.dispatch(HomeIntent.CityPicked(it)) },
        onRefresh = { viewModel.dispatch(HomeIntent.RefreshWeather) },
        onOpenSettings = { viewModel.dispatch(HomeIntent.OpenSettingsClicked) },
        onRetry = { viewModel.dispatch(HomeIntent.RetryClicked) },
    )
}

object MainUI {

    /** Корневая раскладка экрана. */
    @Composable
    fun Root(
        state: HomeState,
        onQueryChange: (String) -> Unit,
        onSearchSubmit: (String) -> Unit,
        onCityPicked: (CitiesItem) -> Unit,
        onRefresh: () -> Unit,
        onOpenSettings: () -> Unit,
        onRetry: () -> Unit,
    ) {
        Scaffold(
            containerColor = WeatherBlue,
            topBar = { TopBar(onOpenSettings = onOpenSettings) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    CitySearchField(
                        query = state.cityQuery,
                        suggestions = state.suggestions,
                        isSuggestionsVisible = state.isSuggestionsVisible,
                        onQueryChange = onQueryChange,
                        onSearchSubmit = onSearchSubmit,
                        onCityPicked = onCityPicked,
                    )

                    val weather = state.weather ?: HomeState.EMPTY_WEATHER
                    WeatherBlock(weather = weather)

                    Spacer(Modifier.height(10.dp))

                    DetailsGrid(weather = weather)
                }

                if (state.isLoading) {
                    LoadingOverlay()
                } else if (state.hasError) {
                    ErrorOverlay(onRetry = onRetry)
                }

                // Кнопка ручного обновления поверх контента (аналог SwipeRefreshLayout)
                RefreshButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                )
            }
        }
    }

    @Composable
    fun TopBar(onOpenSettings: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            CircleIconButton(
                iconResId = R.drawable.settings,
                contentDescription = stringResource(R.string.settings),
                onClick = onOpenSettings,
            )
        }
    }

    /** Поле поиска города с выпадающими подсказками (замена AutoCompleteTextView). */
    @Composable
    fun CitySearchField(
        query: String,
        suggestions: List<CitiesItem>,
        isSuggestionsVisible: Boolean,
        onQueryChange: (String) -> Unit,
        onSearchSubmit: (String) -> Unit,
        onCityPicked: (CitiesItem) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    onQueryChange(it)
                    expanded = it.length >= HomeViewModel.MIN_QUERY_LENGTH && suggestions.isNotEmpty()
                },
                label = { Text(stringResource(R.string.location)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        expanded = false
                        focusManager.clearFocus() // закрывает клавиатуру; Enter -> немедленный поиск
                        onSearchSubmit(query)
                    },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            DropdownMenu(
                expanded = expanded && isSuggestionsVisible && suggestions.isNotEmpty(),
                onDismissRequest = { expanded = false },
            ) {
                suggestions.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city.title) },
                        onClick = {
                            expanded = false
                            onCityPicked(city)
                        },
                    )
                }
            }
        }
    }

    /** Блок текущей погоды: город/страна, дата, статус, температура, min/max. */
    @Composable
    fun WeatherBlock(weather: WeatherUi) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = weather.city,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.size(8.dp))
                if (weather.country.isNotBlank()) {
                    Text(
                        text = weather.country,
                        fontSize = 24.sp,
                        color = Color.White,
                    )
                }
            }
            Text(
                text = weather.updatedAt,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = weather.temperature,
                fontSize = 60.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
            )
            Text(
                text = weather.status,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
                Text(
                    text = stringResource(R.string.min_temp, weather.tempMin),
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.max_temp, weather.tempMax),
                    color = Color.White,
                )
            }
        }
    }

    /** Сетка инфо-плиток: два ряда по три элемента (переиспользует InfoTile). */
    @Composable
    fun DetailsGrid(weather: WeatherUi) {
        val tiles = listOf(
            Triple(R.drawable.sunrise, stringResource(R.string.sunrise), weather.sunrise),
            Triple(R.drawable.sunset, stringResource(R.string.sunset), weather.sunset),
            Triple(R.drawable.wind, stringResource(R.string.wind), weather.wind),
            Triple(R.drawable.pressure, stringResource(R.string.pressure), weather.pressure),
            Triple(R.drawable.humidity, stringResource(R.string.humidity), weather.humidity),
            Triple(R.drawable.info, stringResource(R.string.created_by), stringResource(R.string.about_dev)),
        )
        Column(
            modifier = Modifier.padding(horizontal = 25.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            tiles.chunked(3).forEach { rowTiles ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowTiles.forEach { (iconRes, label, value) ->
                        InfoTile(
                            iconResId = iconRes,
                            label = label,
                            value = value,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun LoadingOverlay() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WeatherBlue.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    @Composable
    fun ErrorOverlay(onRetry: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WeatherBlue.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.error), color = Color.White)
                Spacer(Modifier.height(12.dp))
                CircleIconButton(
                    iconResId = R.drawable.synchronize,
                    contentDescription = stringResource(R.string.save_button),
                    onClick = onRetry,
                )
            }
        }
    }

    @Composable
    fun RefreshButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        CircleIconButton(
            iconResId = R.drawable.synchronize,
            contentDescription = stringResource(R.string.change_data),
            onClick = onClick,
            modifier = modifier,
            size = 56,
        )
    }
}

// ---------- Previews ----------

private val PREVIEW_STATE = HomeState(
    cityQuery = "London",
    weather = WeatherUi(
        city = "London",
        country = "GB",
        updatedAt = "15/10/2026 10:30 AM",
        status = "Broken clouds",
        temperature = "18°C",
        tempMin = "15°C",
        tempMax = "21°C",
        sunrise = "06:40 AM",
        sunset = "07:35 PM",
        wind = "4",
        pressure = "1012",
        humidity = "72",
    ),
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    WeatherAppTheme {
        MainUI.Root(
            state = PREVIEW_STATE,
            onQueryChange = {},
            onSearchSubmit = {},
            onCityPicked = {},
            onRefresh = {},
            onOpenSettings = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeLoadingPreview() {
    WeatherAppTheme {
        MainUI.Root(
            state = PREVIEW_STATE.copy(isLoading = true),
            onQueryChange = {},
            onSearchSubmit = {},
            onCityPicked = {},
            onRefresh = {},
            onOpenSettings = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeErrorPreview() {
    WeatherAppTheme {
        MainUI.Root(
            state = HomeState(hasError = true),
            onQueryChange = {},
            onSearchSubmit = {},
            onCityPicked = {},
            onRefresh = {},
            onOpenSettings = {},
            onRetry = {},
        )
    }
}
