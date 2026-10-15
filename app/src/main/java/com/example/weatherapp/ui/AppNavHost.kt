package com.example.weatherapp.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.R
import com.example.weatherapp.extention.rememberShowSnackbar
import com.example.weatherapp.isActiveNetworkConnected
import com.example.weatherapp.ui.screens.home.HomeScreen
import com.example.weatherapp.ui.screens.settings.SettingsScreen
import com.example.weatherapp.ui.theme.WeatherAppTheme

/** Маршруты Compose Navigation (замена id из удалённого nav_graph.xml). */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

/**
 * NavHost приложения на Compose Navigation.
 *
 * Общие эффекты (Snackbar о потере сети) обрабатываются здесь, а не на экранах:
 * host Snackbar живёт над всеми маршрутами, поэтому сообщение видно и на Home,
 * и на Settings. Экраны передают однократные события через callback'и —
 * MVI-эффекты ViewModel остаются чистыми от Android-зависимостей.
 */
@Composable
fun AppNavHost() {
    WeatherAppTheme {
        RootNavHost()
    }
}

@Composable
private fun RootNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val networkLostText = stringResource(R.string.internet_error)
    val showMessage = rememberShowSnackbar(snackbarHostState)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Внутренние padding уже учтены — экранам не нужно повторно
                // резать системные insets (защита от двойных нижних отступов).
                .consumeWindowInsets(innerPadding),
        ) {
            homeGraph(
                navController = navController,
                onNetworkError = {
                    showMessage(networkLostText)
                },
            )
            settingsGraph(
                navController = navController,
                onRestartForLocale = {
                    (context as? ComponentActivity)?.let(MainActivity::restartWithLocale)
                },
            )
        }
    }

    // Разовая проверка соединения при старте навигации: потеря сети — частый кейс,
    // сигнал нужен до первой же ошибки запроса к API.
    LaunchedEffect(Unit) {
        if (!isActiveNetworkConnected(context)) {
            showMessage(networkLostText)
        }
    }
}

private fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    onNetworkError: () -> Unit,
) {
    composable(Routes.HOME) {
        HomeScreen(
            onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            onNetworkError = onNetworkError,
        )
    }
}

private fun NavGraphBuilder.settingsGraph(
    navController: NavHostController,
    onRestartForLocale: () -> Unit,
) {
    composable(Routes.SETTINGS) {
        SettingsScreen(
            onNavigateBack = {
                // popBackStack с fallback: если Home выбит из стека (restore состояния
                // процесса после убийства), возвращаемся явным navigate.
                if (!navController.popBackStack(Routes.HOME, inclusive = false)) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            onExitApp = {
                (navController.context as? Activity)?.finishAndRemoveTask()
            },
            onRestartForLocale = onRestartForLocale,
        )
    }
}
