package com.example.weatherapp.api

import com.example.weatherapp.citiesDataClasses.Cities
import com.example.weatherapp.weatherDataClasses.Weather
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

    @GET("search/")
    fun getCities(
            @Query("query") cityName: String
    ):Call<Cities>

    companion object {
        val api by lazy { retrofit.create<API>() }
        val api2 by lazy { retrofit2.create<API>() }
    }
}