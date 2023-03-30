package com.example.weatherapp.api.retrofit

import com.example.weatherapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val apiToken = "adb3ecc1532bf5aac42c285ca0d26653"
const val BASE_WEATHER_LINK = "https://api.openweathermap.org/data/2.5/"
const val BASE_CITY_LINK = "https://www.metaweather.com/api/location/"
const val CLIENT_TIME_OUT = 10L

val weatherRetrofit: Retrofit by lazy {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE

    val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addInterceptor(AppInterceptor())
        .connectTimeout(CLIENT_TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(CLIENT_TIME_OUT, TimeUnit.SECONDS)
        .build()

    Retrofit.Builder()
        .baseUrl(BASE_WEATHER_LINK)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}

val cityRetrofit: Retrofit by lazy {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE

    val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .connectTimeout(CLIENT_TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(CLIENT_TIME_OUT, TimeUnit.SECONDS)
        .build()

    Retrofit.Builder()
        .baseUrl(BASE_CITY_LINK)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}