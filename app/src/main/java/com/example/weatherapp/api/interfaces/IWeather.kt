package com.example.weatherapp.api.interfaces

import com.example.weatherapp.App
import com.example.weatherapp.api.models.response.forecast.Forecast
import com.example.weatherapp.api.models.response.weather.Weather
import com.example.weatherapp.logic.LanguageLogic
import retrofit2.http.GET
import retrofit2.http.Query

interface IWeather {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") language: String = LanguageLogic.apiLangParam(App.setting.appLanguage.languageKey),
        @Query("units") units: String = App.setting.appMetrics.metrics,
    ): Weather

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") language: String = LanguageLogic.apiLangParam(App.setting.appLanguage.languageKey),
        @Query("units") units: String = App.setting.appMetrics.metrics,
    ): Forecast
}
