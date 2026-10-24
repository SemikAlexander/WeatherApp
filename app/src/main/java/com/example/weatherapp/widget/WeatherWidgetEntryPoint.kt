package com.example.weatherapp.widget

import com.example.weatherapp.api.interfaces.IWeather
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WeatherWidgetEntryPoint {
    fun weatherService(): IWeather
}
