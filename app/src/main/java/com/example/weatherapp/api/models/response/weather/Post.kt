package com.example.weatherapp.api.models.response.weather

import java.io.Serializable

data class Post(
        val title: String,
        val id: Int,
        val body: String,
        val userId: Int
) : Serializable