package com.vapps.module_ads.control.network

import com.vapps.module_ads.control.model.ConvertCurrencyResponseModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface AperoService {
    @GET("/v1/convertcurrency")
    @Headers("Accept: application/json", "Content-Type: application/json")
    fun getAmountBySpecifyCurrency(
        @Query("have") sourceCurrency: String?,
        @Query("want") targetCurrency: String?,
        @Query("amount") amount: Double
    ): Call<ConvertCurrencyResponseModel?>?
}