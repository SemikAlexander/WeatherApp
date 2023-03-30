package com.example.weatherapp.api.contollers

import com.example.weatherapp.api.interfaces.IWeather
import com.example.weatherapp.api.retrofit.weatherRetrofit
import com.example.weatherapp.api.models.response.weather.Weather

class WeatherServiceImpl {
    suspend fun getWeather(cityName: String): Weather {
        return weatherRetrofit.create(IWeather::class.java).getWeather(cityName)
    }
}