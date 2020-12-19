package com.example.weatherapp.weatherDataClasses

import java.io.Serializable

data class Post(
        val title: String,
        val id: Int,
        val body: String,
        val userId: Int
) : Serializable