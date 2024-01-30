package com.vapps.module_ads.control.model

class PurchaseItem {
    @JvmField
    var itemId: String
    @JvmField
    var trialId: String? = null
    @JvmField
    var type: Int

    constructor(itemId: String, type: Int) {
        this.itemId = itemId
        this.type = type
    }

    constructor(itemId: String, trialId: String?, type: Int) {
        this.itemId = itemId
        this.trialId = trialId
        this.type = type
    }
}