package com.example.weatherapp.api.retrofit

import okhttp3.Interceptor
import okhttp3.Response

class AppInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        builder.addHeader(
            "appid",
            apiToken
        )

        val response = chain.proceed(builder.build())
        val code = response.code
        if (code in 400..499) {

        }

        return response
    }
}