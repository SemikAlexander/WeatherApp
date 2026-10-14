package com.example.weatherapp.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.weatherapp.App
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.startCheckingConnection
import com.example.weatherapp.toast
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

/**
 * Единственная Activity приложения (SingleActivity-подход).
 *
 * Зоны ответственности:
 *  - размещение NavHostFragment: все экраны — фрагменты из nav_graph.xml;
 *  - применение сохранённой локали (attachBaseContext + wrapLocale);
 *  - мониторинг состояния сети.
 *
 * Бизнес-логика экранов — во фрагментах и ViewModel, здесь её быть не должно.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        // App.setting инициализируется в Application.onCreate() до создания Activity,
        // но runCatching — страховка от первого запуска/повреждённых prefs.
        val languageKey = runCatching { App.setting.appLanguage.languageKey.lowercase(Locale.ROOT) }
            .getOrNull() ?: Locale.getDefault().language
        super.attachBaseContext(newBase.wrapLocale(languageKey))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Перезапуск после смены языка: стартуем сразу с HomeFragment,
        // чтобы пользователь не «проваливался» обратно в настройки.
        if (intent.getBooleanExtra(EXTRA_RESTART_FOR_LOCALE, false)) {
            resetStartDestinationToHome()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // setIntent обязателен: без него getIntent() вернёт старый intent,
        // и recreate()/resetStartDestinationToHome не увидят новый extra.
        setIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        startInternetMonitoring()
    }

    private fun resetStartDestinationToHome() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.mainContainer) as? NavHostFragment ?: return

        val graph = navHost.navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(R.id.homeFragment)
        navHost.navController.setGraph(graph, intent.extras)
    }

    private fun startInternetMonitoring() {
        startCheckingConnection(applicationContext) {
            // Колбэк вызывается из корутины на Dispatchers.Main — toast безопасен.
            toast(getString(R.string.internet_error))
        }
    }

    companion object {
        const val EXTRA_RESTART_FOR_LOCALE = "extra_restart_for_locale"
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
