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
        // lang нормализуем к lowercase ISO-639 ("EN" -> "en"): OWM принимает только такой формат.
        @Query("lang") language: String = LanguageLogic.apiLangParam(App.setting.appLanguage.languageKey),
        @Query("units") units: String = App.setting.appMetrics.metrics,
        // appid добавляет AppInterceptor; параметр оставлен с дефолтом для явности контракта.
        @Query("appid") appid: String = "",
    ): Weather
}
