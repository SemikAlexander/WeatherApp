package com.example.weatherapp.api

import com.example.weatherapp.dataClasses.Weather
import retrofit2.Call
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    @GET("weather")
    fun getWeather(
            @Query("q") cityName: String,
            @Query("units") units: String = "metric",
            @Query("appid") appId: String = apiToken
    ): Call<Weather>

    companion object {
        val api by lazy { retrofit.create<API>() }
    }
}