package com.vapps.module_ads.control.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HeaderInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("X-Api-Key", "O89H2zDXcE5XX2qmAr99vQ==h9oKWxcyEWyTG3B7")
        return chain.proceed(requestBuilder.build())
    }
}