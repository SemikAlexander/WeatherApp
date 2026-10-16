package com.example.weatherapp.api.interfaces

import com.example.weatherapp.App
import com.example.weatherapp.api.models.response.weather.Weather
import com.example.weatherapp.logic.LanguageLogic
import retrofit2.http.GET
import retrofit2.http.Query

interface IWeather {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") cityName: String,
        @Query("lang") language: String = LanguageLogic.apiLangParam(App.setting.appLanguage.languageKey),
        @Query("units") units: String = App.setting.appMetrics.metrics,
    ): Weather
}
