plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.kapt) // Hilt 2.56.x официально поддерживает KAPT; KSP-миграция — отдельная задача
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

    buildFeatures {
        viewBinding = true
        // Подготовка к внедрению Compose: композаблы доступны в модулях ViewBinding сразу.
        compose = true
    }
}

dependencies {
    // AndroidX базовое
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.swiperefreshlayout)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Навигация (View-based, до миграции экранов на Compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Сеть
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Изображения
    implementation(libs.coil.compose)

    // Скелетоны загрузки
    implementation(libs.agskeleton.loading)

    // --- Compose (подготовка к внедрению) ---
    // BOM гарантирует согласованные версии compose-артефактов без явных номеров.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
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
