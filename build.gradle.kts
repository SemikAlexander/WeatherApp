// Top-level build file — конфигурация репозиториев и версий плагинов вынесена
// в settings.gradle.kts (pluginManagement) и gradle/libs.versions.toml (Version Catalog).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt) apply false
}
