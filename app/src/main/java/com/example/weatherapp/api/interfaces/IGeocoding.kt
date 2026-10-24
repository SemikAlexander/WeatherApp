package com.example.weatherapp.api.interfaces

import com.example.weatherapp.api.models.response.geocoding.GeoCity
import retrofit2.http.GET
import retrofit2.http.Query

interface IGeocoding {
    @GET("direct")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("limit") limit: Int = DEFAULT_LIMIT,
    ): List<GeoCity>

    companion object {
        const val DEFAULT_LIMIT = 5
    }
}
