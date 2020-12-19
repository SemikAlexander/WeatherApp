package com.example.weatherapp.citiesDataClasses

data class CitiesItem(
    val latt_long: String,
    val location_type: String,
    val title: String,
    val woeid: Int
)