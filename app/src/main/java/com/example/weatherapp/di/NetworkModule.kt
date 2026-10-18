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
