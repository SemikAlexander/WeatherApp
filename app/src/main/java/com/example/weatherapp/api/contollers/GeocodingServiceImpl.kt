package com.example.weatherapp.api.contollers

import com.example.weatherapp.api.interfaces.IGeocoding
import com.example.weatherapp.api.models.response.geocoding.GeoCity
import com.example.weatherapp.api.retrofit.geocodingRetrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingServiceImpl @Inject constructor() : IGeocoding {
    override suspend fun searchCities(query: String, limit: Int): List<GeoCity> {
        val trimmed = query.trim()
        require(trimmed.isNotEmpty()) { "city query must not be blank" }
        return geocodingRetrofit.create(IGeocoding::class.java)
            .searchCities(query = trimmed, limit = limit)
    }
}
