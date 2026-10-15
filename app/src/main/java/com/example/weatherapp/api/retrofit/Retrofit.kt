package com.example.weatherapp.api.retrofit

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.api.ApiConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private fun loggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE // в release тело ответа не пишем в logcat
        }
    }

private fun baseClient(withAppInterceptor: Boolean): OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor())
        .apply { if (withAppInterceptor) addInterceptor(AppInterceptor()) }
        .connectTimeout(ApiConfig.CLIENT_TIME_OUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.CLIENT_TIME_OUT_SECONDS, TimeUnit.SECONDS)
        .build()

val weatherRetrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(ApiConfig.WEATHER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseClient(withAppInterceptor = true))
        .build()
}

val cityRetrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(ApiConfig.CITY_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseClient(withAppInterceptor = false))
        .build()
}
