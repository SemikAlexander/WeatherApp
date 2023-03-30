package com.example.weatherapp.api.models.response.cities

data class CitiesItem(
    val latt_long: String,
    val location_type: String,
    val title: String,
    val woeid: Int
)