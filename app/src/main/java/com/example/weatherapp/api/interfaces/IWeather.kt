package com.example.weatherapp.api.interfaces

import com.example.weatherapp.api.models.response.weather.Weather
import com.example.weatherapp.api.retrofit.apiToken
import retrofit2.http.GET
import retrofit2.http.Query

interface IWeather {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") cityName: String,
        @Query("lang") language: String = "en",
        @Query("appid") appid: String = apiToken,
        @Query("units") units: String = "metric"
    ): Weather
}