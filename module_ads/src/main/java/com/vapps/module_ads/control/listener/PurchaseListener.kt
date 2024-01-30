package com.vapps.module_ads.control.listener

interface PurchaseListener {
    fun onProductPurchased(productId: String?, transactionDetails: String?)
    fun displayErrorMessage(errorMsg: String?)
    fun onUserCancelBilling()
}