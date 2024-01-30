package com.vapps.module_ads.control.network

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object APIClient {
    private var aperoService: AperoService? = null
    fun getAperoService(): AperoService? {
        if (aperoService == null) {
            val httpClient = okhttp3.OkHttpClient.Builder()
            httpClient.addInterceptor(HeaderInterceptor())
            httpClient.readTimeout(60, TimeUnit.SECONDS).connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)

            /*if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(Level.BODY);
                httpClient.addInterceptor(httpLoggingInterceptor);
            }*/
            val endPoint = "https://api.api-ninjas.com/"
            val retrofit = Retrofit.Builder().baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build()
            aperoService = retrofit.create(AperoService::class.java)
        }
        return aperoService
    }
}