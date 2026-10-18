package com.example.weatherapp.api.interfaces

import com.example.weatherapp.api.models.response.cities.CitiesItem
import retrofit2.http.GET
import retrofit2.http.Query

interface ICity {
    @GET("search")
    suspend fun getCities(@Query("query") cityName: String): List<CitiesItem>

    suspend fun getCity(query: String, minQueryLength: Int = 2): CitiesItem
}
