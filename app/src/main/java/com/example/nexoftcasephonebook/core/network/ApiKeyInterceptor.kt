package com.example.nexoftcasephonebook.core.network

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        android.util.Log.d("NEXOFT", "ApiKey len=${apiKey.length} value=${apiKey.take(6)}***")

        val req = chain.request().newBuilder()
            .addHeader("ApiKey", apiKey) // requested header
            .build()
        return chain.proceed(req)
    }
}
