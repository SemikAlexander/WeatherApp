import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.ksp) // Hilt-процессор через KSP (быстрее kapt; официальный путь Dagger)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.weatherapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.weatherapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API-ключ OpenWeatherMap: из local.properties (файл исключён из VCS через .gitignore).
        // Значение подставляется без кавычек и экранируется buildConfigField автоматически.
        val localPropsFile: File = rootProject.file("local.properties")
        val localProperties = Properties().apply {
            if (localPropsFile.exists()) localPropsFile.inputStream().use { load(it) }
        }
        val owmApiKey = localProperties.getProperty("OPEN_WEATHER_MAP_API_KEY", "")
        buildConfigField("String", "OWM_API_KEY", "\"$owmApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true // BuildConfig.VERSION_NAME / OWM_API_KEY (используется в Settings/Home)
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            // Compose требует jvmTarget >= 9; синхронно с compileOptions (AGP проверяет совпадение).
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    // AndroidX базовое
    implementation(libs.androidx.core.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.kotlinx.collections)

    // Сеть
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // DI (Hilt-процессор через KSP вместо kapt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Изображения
    implementation(libs.coil.compose)

    // --- Compose ---
    // BOM гарантирует согласованные версии compose-артефактов без явных номеров.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Pull-to-refresh: нативный material3 (androidx.compose.material3.pulltorefresh),
    // версия из BOM. Accompanist-зависимость не нужна.
    // Навигация: Compose Navigation (SingleActivity, маршруты — строки, не nav_graph.xml)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling) // CompositionPreview / tooling только в debug
    debugImplementation(libs.androidx.compose.ui.test.manifest) // Manifest для compose-test в instrumentation

    // --- Тесты ---
    testImplementation(libs.junit)                    // unit-тесты (src/test)
    testImplementation(libs.kotlinx.coroutines.test) // тестирование suspend/Flow-логики ViewModel
    androidTestImplementation(libs.androidx.test.junit)          // JUnit4-обёртка instrumentation
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // UI-тесты Compose-экранов
}
