package com.example.weatherapp.api.contollers

import com.example.weatherapp.api.interfaces.ICity
import com.example.weatherapp.api.models.response.cities.CitiesItem
import com.example.weatherapp.api.retrofit.cityRetrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityServiceImpl @Inject constructor() : ICity {

    override suspend fun getCities(cityName: String): List<CitiesItem> =
        cityRetrofit.create(ICity::class.java).getCities(cityName)

    override suspend fun getCity(query: String, minQueryLength: Int): CitiesItem {
        require(query.isNotBlank()) { "city query must not be blank" }
        val cities = getCities(query.trim())
        return cities.firstOrNull()
            ?: throw NoSuchElementException("city not found for query: $query")
    }
}
