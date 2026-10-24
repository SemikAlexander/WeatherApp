package com.example.weatherapp.api.contollers

import com.example.weatherapp.api.interfaces.IWeather
import com.example.weatherapp.api.models.response.weather.Weather
import com.example.weatherapp.api.retrofit.weatherRetrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherServiceImpl @Inject constructor() : IWeather {
    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        language: String,
        units: String,
    ): Weather = weatherRetrofit.create(IWeather::class.java)
        .getWeather(latitude = latitude, longitude = longitude, language = language, units = units)
}
