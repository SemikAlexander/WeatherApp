package com.example.weatherapp.api.models.response.weather

import com.google.gson.annotations.SerializedName

data class Main(
        val feels_like: Double,
        val grnd_level: Int,
        val humidity: Int,
        val pressure: Int,
        @SerializedName("sea_level") val seaLevel: Int,
        val temp: Double,
        val temp_max: Double,
        val temp_min: Double
)