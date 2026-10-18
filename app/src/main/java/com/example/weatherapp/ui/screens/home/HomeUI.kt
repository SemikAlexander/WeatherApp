package com.example.weatherapp.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.R
import com.example.weatherapp.ui.components.CircleIconButton
import com.example.weatherapp.ui.components.SpacerHeight
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.ui.theme.c212121
import com.example.weatherapp.ui.theme.c323233
import com.example.weatherapp.ui.theme.cFAFAFA
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

object HomeUI {
    @Composable
    fun Root(
        state: HomeState,
        onRefresh: () -> Unit,
        onOpenSettings: () -> Unit,
        onRetry: () -> Unit,
    ) {
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isLoading)

        // Синхронизация «state — источник истины» <-> «локальный drag-статус widget'а».
        // 1) isLoading стал true (например, рефреш запущен не свайпом: выбор города,
        //    retry, стартовая загрузка) — показываем спиннер принудительно;
        // 2) isLoading стал false — гарантированно убираем спиннер, даже если
        //    widget сам его не сбросил.
        LaunchedEffect(swipeRefreshState, state.isLoading) {
            snapshotFlow { state.isLoading }
                .distinctUntilChanged()
                .collect { refreshing ->
                    if (refreshing && !swipeRefreshState.isRefreshing) {
                        swipeRefreshState.isRefreshing = true
                    } else if (!refreshing && swipeRefreshState.isRefreshing) {
                        swipeRefreshState.isRefreshing = false
                    }
                }
        }

        SwipeRefresh(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            state = swipeRefreshState,
            onRefresh = onRefresh
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(onOpenSettings = onOpenSettings)
                SpacerHeight(36)
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(size = 30.dp)
                        )
                        .padding(horizontal = 25.dp, vertical = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.weather?.updatedAt.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        SpacerHeight(4)
                        Text(
                            text = state.weather?.status.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        SpacerHeight(8)
                        Text(
                            text = state.weather?.temperature.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    AsyncImage(
                        modifier = Modifier
                            .height(66.dp)
                            .weight(1f),
                        model = state.weather?.icon.orEmpty(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                }
                SpacerHeight(20)
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(size = 30.dp)
                        )
                        .padding(horizontal = 25.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(0.18f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sunrise),
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        SpacerHeight(4)
                        Text(
                            text = state.weather?.sunrise.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Icon(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(0.64f),
                        painter = painterResource(R.drawable.sunset_to_sunrise),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.weight(0.18f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sunset),
                            modifier = Modifier.size(24.dp),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        SpacerHeight(4)
                        Text(
                            text = state.weather?.sunset.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                SpacerHeight(20)
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(size = 30.dp)
                        )
                        .padding(horizontal = 25.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_wind),
                            modifier = Modifier.size(16.dp),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        SpacerHeight(14)
                        Text(
                            text = state.weather?.wind.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        SpacerHeight(4)
                        Text(
                            text = stringResource(R.string.wind),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_humidity),
                            modifier = Modifier.size(16.dp),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        SpacerHeight(14)
                        Text(
                            text = "${state.weather?.humidity.orEmpty()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        SpacerHeight(4)
                        Text(
                            text = stringResource(R.string.humidity),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.pressure),
                            modifier = Modifier.size(16.dp),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        SpacerHeight(14)
                        Text(
                            text = state.weather?.pressure.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        SpacerHeight(4)
                        Text(
                            text = stringResource(R.string.pressure),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            if (state.isLoading) {
                LoadingOverlay()
            } else if (state.hasError) {
                ErrorOverlay(onRetry = onRetry)
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
            Spacer(Modifier.weight(1f))
            Icon(
                modifier = Modifier
                    .size(width = 28.dp, height = 28.dp)
                    .clickable(onClick = onOpenSettings),
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }

    @Composable
    fun LoadingOverlay() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }

    @Composable
    fun ErrorOverlay(onRetry: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.error), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                CircleIconButton(
                    iconResId = R.drawable.synchronize,
                    contentDescription = stringResource(R.string.save_button),
                    containerColor = MaterialTheme.colorScheme.surface,
                    onClick = onRetry,
                )
            }
        }
    }
}

// region Previews

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
        wind = "4 met/s",
        pressure = "1012 hPa",
        humidity = "72",
        icon = ""
    ),
)

// region LIGHT THEME

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    WeatherAppTheme {
        HomeUI.Root(
            state = PREVIEW_STATE,
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
        HomeUI.Root(
            state = PREVIEW_STATE.copy(isLoading = true),
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
        HomeUI.Root(
            state = HomeState(hasError = true),
            onRefresh = {},
            onOpenSettings = {},
            onRetry = {},
        )
    }
}

// endregion

// region DARK THEME

@Preview(name = "Dark", showBackground = true)
@Composable
private fun HomeScreenDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        HomeUI.Root(
            state = PREVIEW_STATE,
            onRefresh = {},
            onOpenSettings = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun HomeLoadingDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        HomeUI.Root(
            state = PREVIEW_STATE.copy(isLoading = true),
            onRefresh = {},
            onOpenSettings = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun HomeErrorDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        HomeUI.Root(
            state = HomeState(hasError = true),
            onRefresh = {},
            onOpenSettings = {},
            onRetry = {},
        )
    }
}

// endregion

// endregion Previews