# WeatherApp

Погодное Android-приложение на **Jetpack Compose** с архитектурой **MVI** и **Single Activity**.
Отображает текущую погоду (OpenWeatherMap) для выбранного города, поддерживает два языка (EN/RU) и две системы единиц (метрическая/имперская).

---

## Функциональность

| Экран | Возможности |
|---|---|
| **Home** | Карточка текущей погоды (температура, ветер, облачность, восход/закат, давление), прогноз на +3/+6/+12/+24 ч, pull-to-refresh, индикатор отсутствия сети (Snackbar), выход из приложения |
| **Settings** | Выбор города (live-поиск через OpenWeatherMap Geocoding), выбор языка (EN/RU) — применяется перезапуском Activity, выбор системы единиц (°C/км·ч⁻¹ или °F/миль·ч⁻¹), переключение тёмной темы, диалог подтверждения смены языка |

Сетевые ошибки (timeout, 401, offline) обрабатываются и показываются пользователю через MVI-эффекты.

### Выбор города

Город задаётся **только в настройках** — на главном экране его выбора нет. Диалог в Settings выполняет live-поиск (debounce 500 мс, плюс отправка по клавише Search) через OpenWeatherMap Geocoding API (`https://api.openweathermap.org/geo/1.0/direct`). Выбранный город сохраняется в `Setting.selectedCity` вместе с координатами, а Home запрашивает погоду по `lat`/`lon` — это надёжнее поиска по неоднозначному названию. При возврате из настроек Home сравнивает сохранённый город с загруженным и обновляет погоду.

Запросы к мёртвому MetaWeather (`https://www.metaweather.com/api/location/`) полностью удалены вместе с `ICity`, `CityServiceImpl` и моделями `Cities`/`CitiesItem`.

### Прогноз

Под карточкой текущей погоды показываются четыре слота: **+3, +6, +12 и +24 часа**. Источник — бесплатный OpenWeatherMap [5 day / 3 hour forecast](https://openweathermap.org/forecast5) (`GET /data/2.5/forecast`): ответ начинается со следующего 3-часового рубежа, поэтому слоты берутся по индексам 0, 1, 3 и 7. Текущая погода и прогноз запрашиваются параллельно через `async` и дожидаются в одной корутине (`HomeViewModel.loadWeather`), чтобы оба блока относились к одному городу. При смене города прогноз сбрасывается и загружается заново, а ответы устаревших запросов отсекаются сравнением с `loadedCity`.

Минутные прогнозы (+1 мин, +15 мин) требуют One Call API 3.0 с отдельной подпиской One Call by Call: текущий ключ получает 401, поэтому используются только 3-часовые слоты.

### Стартовый экран

Цвета стартового окна зависят от темы, выбранной в приложении:

- `Theme.WeatherApp` (светлая) — фон `#FFFFFF`, `Theme.WeatherApp.Dark` — фон `#212121`;
- `MainActivity` до `super.onCreate()` выбирает стиль через `isDarkThemeSelected(context)` и настраивает системные бары под выбранную тему;
- на API 31+ `syncApplicationNightMode()` переводит приложение в выбранный ночной режим, поэтому системный splash (Android 12+) и ресурсы `-night` используют ту же тему, что и приложение;
- `values-night/themes.xml` задаёт тёмный вариант для системного стартового окна, когда тема следует системе.

На API < 31 системный splash может использовать системную тему, но окно приложения всё равно перекрашивается в выбранную.

---

## Архитектура

### Single Activity + Compose Navigation
Единственная точка входа — `ui/MainActivity.kt` (обёрнута в `attachBaseContext` для применения локали). Навигация — `androidx.navigation:navigation-compose`, маршруты описаны в `ui/AppNavHost.kt`:

```kotlin
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
}
```

Legacy-код удалён: XML-layout'ы фрагментов, `nav_graph.xml`, ViewBinding — в проекте нет ни одного layout-файла экранов.

### MVI (`mvi/Mvi.kt`)
Базовый `MviViewModel<State : Any, Intent : IaIntent, Effect : IaEffect>`:

- **State** — единственный источник истины экрана, `StateFlow`, неизменяемые data class'ы;
- **Intent** — маркер пользовательского действия (`IaIntent`);
- **Effect** — однократные побочные события (Snackbar, перезапуск) через `SharedFlow(replay = 0)`;
- **reducer(state, intent) → state** — чистая функция, легко тестируется;
- **handle(intent)** — побочные эффекты (сеть) во `viewModelScope`.

Контракты экранов — в `*Contract.kt` (`HomeContract`, `SettingsContract`).

### Структура UI-кода
Повторяющиеся элементы вынесены в переиспользуемые Composable в `ui/components/CommonComponents.kt` (`CircleIconButton`, `SelectableItemRow`, `SettingsRow` и др.). Элементы каждого экрана живут в отдельном объекте-неймспейсе:

- `object MainUI` — компоненты экрана Home (`ui/screens/home/HomeScreen.kt`);
- `object SettingsUI` — компоненты экрана Settings (`ui/screens/settings/SettingsScreen.kt`).

Каждый ключевой компонент снабжён `@Preview` — вёрстку можно смотреть и править без запуска приложения (Android Studio → Split/Design view).

### Слои проекта

```
com.example.weatherapp
├── api/            // Retrofit-сервисы (IWeather, IGeocoding), модели ответов, ApiConfig, интерцепторы
├── di/             // Hilt-модули (NetworkModule)
├── extention/      // Setting (SharedPreferences), расширения, ApplicationLanguage/ApplicationMetrics
├── logic/          // Чистая логика: LanguageLogic, WeatherFormatter (unit-тестируется без Android)
├── mvi/            // Базовый MVI-каркас
└── ui/             // MainActivity, AppNavHost, theme/, components/, screens/{home,settings}/
```

DI — **Hilt** (процессор — **KSP**, kapt удалён). Сеть — **Retrofit + OkHttp + Gson**. Изображения — **Coil**.

---

## Зависимости

Все версии централизованы в Gradle Version Catalog — `gradle/libs.versions.toml` (единственное место обновления библиотек).

### Инструментальный стек
| Библиотека | Версия | Назначение |
|---|---|---|
| Android Gradle Plugin | 8.13.2 | Сборка (требует Gradle ≥ 8.13) |
| Kotlin | 2.1.21 | Язык; Compose Compiler через плагин `org.jetbrains.kotlin.plugin.compose` |
| KSP | — | Кодогенерация Hilt вместо kapt |

### AndroidX / Compose
| Библиотека | Версия | Назначение |
|---|---|---|
| Compose BOM | 2026.09.00 | Согласованные версии ui/material3/tooling |
| activity-compose | 1.10.1 | `setContent` в Activity |
| navigation-compose | 2.9.6 | Навигация single-activity |
| hilt-navigation-compose | 1.2.0 | `hiltViewModel()` в composable |
| lifecycle (viewmodel/runtime/liveData-ktx) | 2.9.4 | `collectAsStateWithLifecycle`, области ViewModel |
| core-ktx | 1.17.0 | Расширения ядра (minSdk 23 — 1.19.x не подходит) |

### DI / сеть / прочее
| Библиотека | Версия | Назначение |
|---|---|---|
| Hilt (dagger) | 2.56.1 | Dependency Injection (KSP-совместим) |
| Retrofit | 2.12.0 | HTTP-клиент (+ converter-gson) |
| OkHttp logging-interceptor | 4.12.0 | Логирование запросов |
| kotlinx-coroutines | 1.11.0 | Асинхронность (+ coroutines-test) |
| Coil-compose | 2.7.0 | Загрузка изображений иконок погоды |

### Тестирование
| Библиотека | Версия | Назначение |
|---|---|---|
| JUnit | 4.13.2 | Unit-тесты |
| androidx.test.ext:junit | 1.2.1 | Instrumentation-запуск |
| Espresso | 3.6.1 | UI-тесты |
| kotlinx-coroutines-test | 1.11.0 | Тестирование суспендирующих функций/Flow |

*(Тесты запланированы как последний этап; каркас reducer'ов и `logic/` спроектированы под чистое unit-тестирование.)*

---

## SDK и сборка

| Параметр | Значение |
|---|---|
| compileSdk / targetSdk | 36 |
| minSdk | 23 |
| versionName | 1.0.0 |
| Формат сборки | Gradle Kotlin DSL (`*.gradle.kts`) |

### Настройка перед сборкой

1. Установите Android SDK Platform 36.
2. Получите ключ OpenWeatherMap и добавьте в **`local.properties`** (файл в `.gitignore`, в VCS не попадает):

```properties
sdk.dir=/path/to/Android/Sdk
OPEN_WEATHER_MAP_API_KEY=ваш_ключ
```

Ключ пробрасывается в код через `BuildConfig.OWM_API_KEY` (`api/ApiConfig.kt`). Без ключа приложение собирается; сетевые запросы вернут 401, что обработано как штатный сценарий ошибки.

3. Сборка и проверка:

```bash
./gradlew :app:assembleDebug        # APK
./gradlew :app:testDebugUnitTest    # unit-тесты
```

---

## Известные ограничения / план

- Смена языка реализована перезапуском Activity (осознанный выбор; per-app locales — в планах ресёрча).
- Выбор города — в настройках; используется OpenWeatherMap Geocoding API на том же ключе, что и погода (без ключа поиск и погода возвращают 401).
- Прогноз строится по бесплатному 3-часовому API; минутные точки требуют подписки One Call by Call.
- Unit/UI-тесты покрытия всех пользовательских сценариев — следующий этап.
