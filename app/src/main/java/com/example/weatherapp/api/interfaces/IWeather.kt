package com.example.weatherapp.api.interfaces

import com.example.weatherapp.App
import com.example.weatherapp.api.models.response.weather.Weather
import com.example.weatherapp.api.retrofit.apiToken
import retrofit2.http.GET
import retrofit2.http.Query

interface IWeather {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") cityName: String,
        @Query("lang") language: String = App.setting.appLanguage.languageKey,
        @Query("appid") appid: String = apiToken,
        @Query("units") units: String = App.setting.appMetrics.metrics
    ): Weather

    @GET("forecast/daily")
    suspend fun getDailyForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("cnt") cnt: Int = App.setting.dailyCount,
        @Query("appid") appid: String = apiToken
    ): Weather
}