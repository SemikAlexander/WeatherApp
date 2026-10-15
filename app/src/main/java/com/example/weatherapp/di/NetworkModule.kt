package com.example.weatherapp.di

import com.example.weatherapp.api.contollers.CityServiceImpl
import com.example.weatherapp.api.contollers.WeatherServiceImpl
import com.example.weatherapp.api.interfaces.ICity
import com.example.weatherapp.api.interfaces.IWeather
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI-модуль сетевых сервисов.
 *
 * Раньше ViewModel создавала `WeatherServiceImpl()` напрямую — это ломало
 * тестируемость (не подставить fake) и скрытую зависимость от object-синглтонов
 * retrofit с глобальным apiToken. Теперь сервисы инжектятся по интерфейсу.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWeatherService(): IWeather = WeatherServiceImpl()

    @Provides
    @Singleton
    fun provideCityService(): ICity = CityServiceImpl()
}
