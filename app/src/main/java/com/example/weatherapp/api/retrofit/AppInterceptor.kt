package com.example.weatherapp.api.retrofit

import com.example.weatherapp.api.ApiConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Добавляет API-ключ к каждому запросу OpenWeatherMap.
 *
 * Исправление безопасности: ключ передаётся query-параметром `appid`, а не HTTP-заголовком —
 * OWM заголовок "appid" игнорирует, т.е. прежняя реализация вообще не работала бы без
 * дублирования ключа в @Query. Плюс удалена пустая ветка `if (code in 400..499) {}`.
 */
class AppInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("appid", ApiConfig.apiKey)
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}
