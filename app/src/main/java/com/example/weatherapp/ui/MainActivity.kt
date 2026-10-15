package com.example.weatherapp.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.weatherapp.App
import com.example.weatherapp.logic.LanguageLogic
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

/**
 * Единственная Activity приложения (SingleActivity + Jetpack Compose).
 *
 * Зоны ответственности (и только они):
 *  - применение сохранённой локали (attachBaseContext + wrapLocale);
 *  - размещение Compose-NavHost ([AppNavHost]);
 *  - обработка перезапуска после смены языка.
 *
 * Бизнес-логика экранов — в MVI-ViewModel, здесь её быть не должно.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // App.setting инициализируется в Application.onCreate() до создания Activity,
        // но runCatching — страховка от первого запуска/повреждённых prefs.
        val languageKey = runCatching { App.setting.appLanguage.languageKey }
            .getOrNull()
            ?.let(LanguageLogic::localeTag)
            ?: Locale.getDefault().language
        super.attachBaseContext(newBase.wrapLocale(languageKey))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent { AppNavHost() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // setIntent обязателен: без него getIntent() вернёт старый intent
        // и повторный перезапуск не увидит новый extra.
        setIntent(intent)
    }

    companion object {
        /**
         * Флаг перезапуска ради локали больше НЕ используется: смена языка теперь
         * делает recreate() без пересоздания задачи (см. SettingsScreen). Константа
         * оставлена как deprecated-маркер для внешних интентов.
         */
        @Deprecated("Перезапуск через Intent не нужен; язык применяется recreate()-ом")
        const val EXTRA_RESTART_FOR_LOCALE = "extra_restart_for_locale"

        /**
         * Пересоздать Activity с новой локалью: attachBaseContext выполнится заново
         * и заберёт обновлённый App.setting.appLanguage. Стек Compose Navigation при
         * этом сохраняется (navController помнит текущий маршрут), поэтому пользователя
         * не выбрасывает на Home — в отличие от прежнего CLEAR_TASK-перезапуска.
         */
        fun restartWithLocale(activity: ComponentActivity) {
            activity.recreate()
        }
    }
}

/**
 * Оборачивает контекст с нужной локалью.
 * createConfigurationContext возвращает НОВЫЙ контекст — его обязательно нужно
 * передать в super.attachBaseContext(), иначе локализация не применится.
 */
fun Context.wrapLocale(languageKey: String): Context {
    val locale = Locale(languageKey)
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration).apply { setLocale(locale) }
    return createConfigurationContext(config)
}
