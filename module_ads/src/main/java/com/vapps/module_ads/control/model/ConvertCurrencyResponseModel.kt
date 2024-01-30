package com.vapps.module_ads.control.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class ConvertCurrencyResponseModel {
    @SerializedName("new_amount")
    var newAmount = 0.0

    @SerializedName("new_currency")
    var targetCurrency: String? = null

    @SerializedName("old_currency")
    var sourceCurrency: String? = null

    @SerializedName("old_amount")
    var oldAmount = 0.0
    override fun toString(): String {
        return "oldAmount: $oldAmount$sourceCurrency newAmount: $newAmount$targetCurrency"
    }
}