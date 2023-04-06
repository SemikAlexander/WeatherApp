package com.example.weatherapp.api.interfaces

import com.example.weatherapp.api.models.response.cities.Cities
import retrofit2.http.GET
import retrofit2.http.Query

interface ICity {
    @GET("search")
    suspend fun getCities(
        @Query("query") cityName: String
    ): Cities
}