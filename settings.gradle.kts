// Единая точка конфигурации плагинов и репозиториев (modern Gradle setup).
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Ошибочные объявления репозиториев в модулях — жёсткая ошибка, а не warning.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // AGSkeletonLoading
    }
}

rootProject.name = "WeatherApp"
include(":app")
