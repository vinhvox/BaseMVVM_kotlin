package com.vapps.module_ads.control.model

class PurchaseResult(
    var orderId: String,
    var packageName: String,
    var productId: List<String>,
    var purchaseTime: Long,
    var purchaseState: Int,
    var purchaseToken: String,
    var quantity: Int,
    var isAutoRenewing: Boolean,
    var isAcknowledged: Boolean
)