package com.example.weatherapp.api.contollers

import com.example.weatherapp.api.interfaces.ICity
import com.example.weatherapp.api.models.response.cities.Cities
import com.example.weatherapp.api.retrofit.cityRetrofit

class CityServiceImpl {
    suspend fun getCity(cityName: String): Cities {
        return cityRetrofit.create(ICity::class.java).getCities(cityName)
    }
}