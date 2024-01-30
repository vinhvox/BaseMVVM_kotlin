package com.vapps.module_ads.control.billing

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.IntDef
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.PricingPhase
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.common.collect.ImmutableList
import com.vapps.module_ads.control.dialog.PurchaseDevBottomSheet
import com.vapps.module_ads.control.event.VioLogEventManager.onTrackRevenuePurchase
import com.vapps.module_ads.control.event.FirebaseAnalyticsUtil.logConfirmPurchaseGoogle
import com.vapps.module_ads.control.event.FirebaseAnalyticsUtil.logRevenuePurchase
import com.vapps.module_ads.control.listener.BillingListener
import com.vapps.module_ads.control.listener.PurchaseListener
import com.vapps.module_ads.control.listener.UpdatePurchaseListener
import com.vapps.module_ads.control.model.ConvertCurrencyResponseModel
import com.vapps.module_ads.control.model.PurchaseItem
import com.vapps.module_ads.control.model.PurchaseResult
import com.vapps.module_ads.control.network.APIClient.getAperoService
import com.vapps.module_ads.control.utils.AppUtil.VARIANT_DEV
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Currency
import java.util.Objects

class AppPurchase private constructor() {
    @get:Deprecated("")
    @SuppressLint("StaticFieldLeak")
    var price = "1.49$"
        get() = getPrice(productId)
    private var oldPrice = "2.99$"

    @Deprecated("")
    private val productId: String? = null
    private var listSubscriptionId: ArrayList<QueryProductDetailsParams.Product>? = null
    private var listINAPId: ArrayList<QueryProductDetailsParams.Product>? = null
    private var purchaseItems: List<PurchaseItem>? = null
    private var purchaseListener: PurchaseListener? = null
    private var updatePurchaseListener: UpdatePurchaseListener? = null
    private var billingListener: BillingListener? = null
    var initBillingFinish = false
        private set
    private var billingClient: BillingClient? = null
    private var skuListINAPFromStore: List<ProductDetails>? = null
    private var skuListSubsFromStore: List<ProductDetails>? = null
    private val skuDetailsINAPMap: MutableMap<String?, ProductDetails> = HashMap()
    private val skuDetailsSubsMap: MutableMap<String, ProductDetails> = HashMap()
    var isAvailable = false
        private set
    private var isListGot = false
    private var isConsumePurchase = false
    private var retryConsumeTimes = 0
    private val MAX_RETRY_CONSUME_TIMES = 1
    private val countReconnectBilling = 0
    private val countMaxReconnectBilling = 4

    //tracking purchase adjust
    private var idPurchaseCurrent = ""
    private var typeIap = 0

    // status verify purchase INAP & SUBS
    private var verifyFinish = false
    private var isVerifyINAP = false
    private var isVerifySUBS = false
    private var isUpdateInapps = false
    private var isUpdateSubs = false
    var isPurchased = false //state purchase on app
        private set
    val idPurchased = "" //id purchased
    private val ownerIdSubs: MutableList<PurchaseResult> = ArrayList() //id sub
    private val ownerIdInapps: MutableList<String> = ArrayList() //id inapp
    var enableTrackingRevenue = false
    private var handlerTimeout: Handler? = null
    private var rdTimeout: Runnable? = null
    fun setPurchaseListener(purchaseListener: PurchaseListener?) {
        this.purchaseListener = purchaseListener
    }

    fun setUpdatePurchaseListener(listener: UpdatePurchaseListener?) {
        updatePurchaseListener = listener
    }

    /**
     * Listener init billing app
     * When init available auto call onInitBillingFinish with resultCode = 0
     *
     * @param billingListener
     */
    fun setBillingListener(billingListener: BillingListener) {
        this.billingListener = billingListener
        if (isAvailable) {
            billingListener.onInitBillingFinished(0)
            initBillingFinish = true
        }
    }

    fun setEventConsumePurchaseTest(view: View) {
        view.setOnClickListener { view1: View? ->
            if (VARIANT_DEV) {
                Log.d(TAG, "setEventConsumePurchaseTest: success")
                instance!!.consumePurchase(PRODUCT_ID_TEST)
            }
        }
    }

    /**
     * Listener init billing app with timeout
     * When init available auto call onInitBillingFinish with resultCode = 0
     *
     * @param billingListener
     * @param timeout
     */
    fun setBillingListener(billingListener: BillingListener, timeout: Int) {
        Log.d(TAG, "setBillingListener: timeout $timeout")
        this.billingListener = billingListener
        if (isAvailable) {
            Log.d(TAG, "setBillingListener: finish")
            billingListener.onInitBillingFinished(0)
            initBillingFinish = true
            return
        }
        handlerTimeout = Handler()
        rdTimeout = Runnable {
            Log.d(TAG, "setBillingListener: timeout run ")
            initBillingFinish = true
            billingListener.onInitBillingFinished(BillingClient.BillingResponseCode.ERROR)
        }
        handlerTimeout!!.postDelayed(rdTimeout!!, timeout.toLong())
    }

    fun setConsumePurchase(consumePurchase: Boolean) {
        isConsumePurchase = consumePurchase
    }

    fun setOldPrice(oldPrice: String) {
        this.oldPrice = oldPrice
    }

    var purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, list ->
        Log.e(TAG, "onPurchasesUpdated code: " + billingResult.responseCode)
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
            for (purchase in list) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            if (purchaseListener != null) purchaseListener!!.onUserCancelBilling()
            Log.d(TAG, "onPurchasesUpdated:USER_CANCELED ")
        } else {
            Log.d(TAG, "onPurchasesUpdated:... ")
        }
    }
    var purchaseClientStateListener: BillingClientStateListener =
        object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isAvailable = false
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d(TAG, "onBillingSetupFinished:  " + billingResult.responseCode)
                if (!initBillingFinish) {
                    verifyPurchased(true)
                }
                initBillingFinish = true
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isAvailable = true
                    // check product detail INAP
                    if (listINAPId!!.size > 0) {
                        val paramsINAP = QueryProductDetailsParams.newBuilder()
                            .setProductList(listINAPId!!)
                            .build()
                        billingClient!!.queryProductDetailsAsync(
                            paramsINAP
                        ) { billingResult, productDetailsList ->
                            if (productDetailsList != null) {
                                Log.d(TAG, "onSkuINAPDetailsResponse: " + productDetailsList.size)
                                skuListINAPFromStore = productDetailsList
                                isListGot = true
                                addSkuINAPToMap(productDetailsList)
                            }
                        }
                    }
                    // check product detail SUBS
                    if (listSubscriptionId!!.size > 0) {
                        val paramsSUBS = QueryProductDetailsParams.newBuilder()
                            .setProductList(listSubscriptionId!!)
                            .build()
                        for (item in listSubscriptionId!!) {
                            Log.d(TAG, "onBillingSetupFinished: " + item.zza())
                        }
                        billingClient!!.queryProductDetailsAsync(
                            paramsSUBS
                        ) { billingResult, productDetailsList ->
                            if (productDetailsList != null) {
                                Log.d(TAG, "onSkuSubsDetailsResponse: " + productDetailsList.size)
                                skuListSubsFromStore = productDetailsList
                                isListGot = true
                                addSkuSubsToMap(productDetailsList)
                            }
                        }
                    } else {
                        Log.d(TAG, "onBillingSetupFinished: listSubscriptionId empty")
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
                    || billingResult.responseCode == BillingClient.BillingResponseCode.ERROR
                ) {
                    Log.e(TAG, "onBillingSetupFinished:ERROR ")
                }
            }
        }

    fun getOwnerIdSubs(): List<PurchaseResult> {
        return ownerIdSubs
    }

    fun getOwnerIdInapps(): List<String> {
        return ownerIdInapps
    }

    @Deprecated("")
    fun initBilling(
        application: Application?,
        listINAPId: MutableList<String?>,
        listSubsId: List<String?>
    ) {
        if (VARIANT_DEV) {
            // auto add purchase test when dev
            listINAPId.add(PRODUCT_ID_TEST)
        }
        listSubscriptionId = listIdToListProduct(listSubsId, BillingClient.ProductType.SUBS)
        this.listINAPId = listIdToListProduct(listINAPId, BillingClient.ProductType.INAPP)
        billingClient = BillingClient.newBuilder(application!!)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient!!.startConnection(purchaseClientStateListener)
    }

    fun initBilling(application: Application?, purchaseItemList: MutableList<PurchaseItem>) {
        if (VARIANT_DEV) {
            // auto add purchase test when dev
            purchaseItemList.add(PurchaseItem(PRODUCT_ID_TEST, "", TYPE_IAP.PURCHASE))
        }
        purchaseItems = purchaseItemList
        syncPurchaseItemsToListProduct(purchaseItems)
        billingClient = BillingClient.newBuilder(application!!)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient!!.startConnection(purchaseClientStateListener)
    }

    private fun addSkuSubsToMap(skuList: List<ProductDetails>) {
        for (skuDetails in skuList) {
            skuDetailsSubsMap[skuDetails.productId] = skuDetails
        }
    }

    private fun addSkuINAPToMap(skuList: List<ProductDetails>) {
        for (skuDetails in skuList) {
            skuDetailsINAPMap[skuDetails.productId] = skuDetails
        }
    }

    fun setPurchase(purchase: Boolean) {
        isPurchased = purchase
    }

    fun isPurchased(context: Context?): Boolean {
        return isPurchased
    }

    private fun addOrUpdateOwnerIdSub(purchaseResult: PurchaseResult, id: String) {
        var isExistId = false
        for (p in ownerIdSubs) {
            if (p.productId.contains(id)) {
                isExistId = true
                ownerIdSubs.remove(p)
                ownerIdSubs.add(purchaseResult)
                break
            }
        }
        if (!isExistId) {
            ownerIdSubs.add(purchaseResult)
        }
    }

    // kiểm tra trạng thái purchase
    fun verifyPurchased(isCallback: Boolean) {
        Log.d(TAG, "isPurchased : " + listSubscriptionId!!.size)
        verifyFinish = false
        if (listINAPId != null) {
            billingClient!!.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult: BillingResult, list: List<Purchase> ->
                Log.d(
                    TAG,
                    "verifyPurchased INAPP  code:" + billingResult.responseCode + " ===   size:" + list.size
                )
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in list) {
                        var purchaseState: Int
                        Log.i(TAG, "verifyPurchased: Original json: " + purchase.originalJson)
                        purchaseState = try {
                            val jsonObject = JSONObject(purchase.originalJson)
                            jsonObject.getInt("purchaseState")
                        } catch (e: JSONException) {
                            Log.e(
                                TAG,
                                "verifyPurchased: Convert original json object failed, exception's  message: " + e.message
                            )
                            purchase.purchaseState
                        }
                        Log.i(TAG, "verifyPurchased: Purchase state after verify: $purchaseState")
                        for (id in listINAPId!!) {
                            if (purchase.products.contains(id.zza()) && purchaseState == Purchase.PurchaseState.PURCHASED) {
                                Log.i(TAG, "verifyPurchased INAPP: Order Id: " + purchase.orderId)
                                ownerIdInapps.add(id.zza())
                                isPurchased = true
                            }
                        }
                    }
                    isVerifyINAP = true
                    if (isVerifySUBS) {
                        if (billingListener != null && isCallback) {
                            billingListener!!.onInitBillingFinished(billingResult.responseCode)
                            if (handlerTimeout != null && rdTimeout != null) {
                                handlerTimeout!!.removeCallbacks(rdTimeout!!)
                            }
                        }
                        verifyFinish = true
                    }
                } else {
                    isVerifyINAP = true
                    if (isVerifySUBS) {
                        // chưa mua subs và IAP
                        billingListener!!.onInitBillingFinished(billingResult.responseCode)
                        if (handlerTimeout != null && rdTimeout != null) {
                            handlerTimeout!!.removeCallbacks(rdTimeout!!)
                        }
                        verifyFinish = true
                    }
                }
            }
        }
        if (listSubscriptionId != null) {
            billingClient!!.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult: BillingResult, list: List<Purchase> ->
                Log.d(
                    TAG,
                    "verifyPurchased SUBS  code:" + billingResult.responseCode + " ===   size:" + list.size
                )
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in list) {
                        for (id in listSubscriptionId!!) {
                            if (purchase.products.contains(id.zza())) {
                                val purchaseResult = PurchaseResult(
                                    purchase.orderId!!,
                                    purchase.packageName,
                                    purchase.products,
                                    purchase.purchaseTime,
                                    purchase.purchaseState,
                                    purchase.purchaseToken,
                                    purchase.quantity,
                                    purchase.isAutoRenewing,
                                    purchase.isAcknowledged
                                )
                                addOrUpdateOwnerIdSub(purchaseResult, id.zza())
                                Log.d(TAG, "verifyPurchased SUBS: true")
                                isPurchased = true
                            }
                        }
                    }
                    isVerifySUBS = true
                    if (isVerifyINAP) {
                        if (billingListener != null && isCallback) {
                            billingListener!!.onInitBillingFinished(billingResult.responseCode)
                            if (handlerTimeout != null && rdTimeout != null) {
                                handlerTimeout!!.removeCallbacks(rdTimeout!!)
                            }
                        }
                        verifyFinish = true
                    }
                } else {
                    isVerifySUBS = true
                    if (isVerifyINAP) {
                        // chưa mua subs và IAP
                        if (billingListener != null && isCallback) {
                            billingListener!!.onInitBillingFinished(billingResult.responseCode)
                            if (handlerTimeout != null && rdTimeout != null) {
                                handlerTimeout!!.removeCallbacks(rdTimeout!!)
                            }
                            verifyFinish = true
                        }
                    }
                }
            }
        }
    }

    fun updatePurchaseStatus() {
        if (listINAPId != null) {
            billingClient!!.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult: BillingResult, list: List<Purchase> ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in list) {
                        for (id in listINAPId!!) {
                            if (purchase.products.contains(id.zza())) {
                                if (!ownerIdInapps.contains(id.zza())) {
                                    ownerIdInapps.add(id.zza())
                                }
                            }
                        }
                    }
                }
                isUpdateInapps = true
                if (isUpdateSubs) {
                    if (updatePurchaseListener != null) {
                        updatePurchaseListener!!.onUpdateFinished()
                    }
                }
            }
        }
        if (listSubscriptionId != null) {
            billingClient!!.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult: BillingResult, list: List<Purchase> ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in list) {
                        for (id in listSubscriptionId!!) {
                            if (purchase.products.contains(id.zza())) {
                                val purchaseResult = PurchaseResult(
                                    purchase.orderId!!,
                                    purchase.packageName,
                                    purchase.products,
                                    purchase.purchaseTime,
                                    purchase.purchaseState,
                                    purchase.purchaseToken,
                                    purchase.quantity,
                                    purchase.isAutoRenewing,
                                    purchase.isAcknowledged
                                )
                                addOrUpdateOwnerIdSub(purchaseResult, id.zza())
                            }
                        }
                    }
                }
                isUpdateSubs = true
                if (isUpdateInapps) {
                    if (updatePurchaseListener != null) {
                        updatePurchaseListener!!.onUpdateFinished()
                    }
                }
            }
        }
    }

    /*    private String logResultBilling(Purchase.PurchasesResult result) {
        if (result == null || result.getPurchasesList() == null)
            return "null";
        StringBuilder log = new StringBuilder();
        for (Purchase purchase : result.getPurchasesList()) {
            for (String s : purchase.getSkus()) {
                log.append(s).append(",");
            }
        }
        return log.toString();
    }*/
    @Deprecated("")
    fun purchase(activity: Activity?) {
        if (productId == null) {
            Log.e(TAG, "Purchase false:productId null")
            Toast.makeText(activity, "Product id must not be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        purchase(activity, productId)
    }

    fun purchase(activity: Activity?, productId: String): String {
        if (skuListINAPFromStore == null) {
            if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Billing error init")
            return ""
        }
        val productDetails = skuDetailsINAPMap[productId]
        //ProductDetails{jsonString='{"productId":"android.test.purchased","type":"inapp","title":"Tiêu đề mẫu","description":"Mô tả mẫu về sản phẩm: android.test.purchased.","skuDetailsToken":"AEuhp4Izz50wTvd7YM9wWjPLp8hZY7jRPhBEcM9GAbTYSdUM_v2QX85e8UYklstgqaRC","oneTimePurchaseOfferDetails":{"priceAmountMicros":23207002450,"priceCurrencyCode":"VND","formattedPrice":"23.207 ₫"}}', parsedJson={"productId":"android.test.purchased","type":"inapp","title":"Tiêu đề mẫu","description":"Mô tả mẫu về sản phẩm: android.test.purchased.","skuDetailsToken":"AEuhp4Izz50wTvd7YM9wWjPLp8hZY7jRPhBEcM9GAbTYSdUM_v2QX85e8UYklstgqaRC","oneTimePurchaseOfferDetails":{"priceAmountMicros":23207002450,"priceCurrencyCode":"VND","formattedPrice":"23.207 ₫"}}, productId='android.test.purchased', productType='inapp', title='Tiêu đề mẫu', productDetailsToken='AEuhp4Izz50wTvd7YM9wWjPLp8hZY7jRPhBEcM9GAbTYSdUM_v2QX85e8UYklstgqaRC', subscriptionOfferDetails=null}
        if (VARIANT_DEV) {
            // Auto using id purchase test in variant dev
            val purchaseDevBottomSheet = PurchaseDevBottomSheet(
                TYPE_IAP.PURCHASE,
                productDetails,
                activity!!,
                purchaseListener
            )
            purchaseDevBottomSheet.show()
            return ""
        }
        if (productDetails == null) {
            return "Not found item with id: $productId"
        }
        Log.d(TAG, "purchase: $productDetails")
        idPurchaseCurrent = productId
        typeIap = TYPE_IAP.PURCHASE
        val productDetailsParamsList = ImmutableList.of(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult = billingClient!!.launchBillingFlow(activity!!, billingFlowParams)
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Billing not supported for type of request")
                return "Billing not supported for type of request"
            }

            BillingClient.BillingResponseCode.ITEM_NOT_OWNED, BillingClient.BillingResponseCode.DEVELOPER_ERROR -> return ""
            BillingClient.BillingResponseCode.ERROR -> {
                if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Error completing request")
                return "Error completing request"
            }

            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> return "Error processing request."
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> return "Selected item is already owned"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> return "Item not available"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> return "Play Store service is not connected now"
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> return "Timeout"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Network error.")
                return "Network Connection down"
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Request Canceled")
                return "Request Canceled"
            }

            BillingClient.BillingResponseCode.OK -> return "Subscribed Successfully"
        }
        return ""
    }

    fun subscribe(activity: Activity?, SubsId: String): String {
        if (skuListSubsFromStore == null) {
            if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Billing error init")
            return ""
        }
        if (VARIANT_DEV) {
            // sử dụng ID Purchase test
            purchase(activity, PRODUCT_ID_TEST)
            return "Billing test"
        }
        val skuDetails = skuDetailsSubsMap[SubsId] ?: return "Product ID invalid"
        val subsDetail = skuDetails.subscriptionOfferDetails
        if (subsDetail == null || subsDetail.isEmpty()) {
            return "Can't found offer for this subscription!"
        }
        var trailId: String? = null
        for (item in purchaseItems!!) {
            if (item.itemId == SubsId) {
                trailId = item.trialId
                break
            }
        }
        var offerToken = ""
        for (item in subsDetail) {
            val offerId = item.offerId
            if (offerId != null && offerId == trailId) {
                offerToken = item.offerToken
                break
            }
        }
        if (offerToken.isEmpty()) {
            offerToken = subsDetail[subsDetail.size - 1].offerToken
        }
        Log.d(TAG, "subscribe: offerToken: $offerToken")
        idPurchaseCurrent = SubsId
        typeIap = TYPE_IAP.SUBSCRIPTION
        val productDetailsParamsList = ImmutableList.of(
            ProductDetailsParams.newBuilder()
                .setProductDetails(skuDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult = billingClient!!.launchBillingFlow(activity!!, billingFlowParams)
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Billing not supported for type of request")
                return "Billing not supported for type of request"
            }

            BillingClient.BillingResponseCode.ITEM_NOT_OWNED, BillingClient.BillingResponseCode.DEVELOPER_ERROR -> return ""
            BillingClient.BillingResponseCode.ERROR -> {
                if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Error completing request")
                return "Error completing request"
            }

            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> return "Error processing request."
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> return "Selected item is already owned"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> return "Item not available"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> return "Play Store service is not connected now"
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> return "Timeout"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Network error.")
                return "Network Connection down"
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                if (purchaseListener != null) purchaseListener!!.displayErrorMessage("Request Canceled")
                return "Request Canceled"
            }

            BillingClient.BillingResponseCode.OK -> return "Subscribed Successfully"
        }
        return ""
    }

    fun consumePurchase() {
        if (productId == null) {
            Log.e(TAG, "Consume Purchase false:productId null ")
            return
        }
        consumePurchase(productId)
    }

    fun consumePurchase(productId: String) {
        Log.d(TAG, "consumePurchase: $productId")
        retryConsumeTimes = 0
        val queryPurchasesParams =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        billingClient!!.queryPurchasesAsync(queryPurchasesParams) { billingResult: BillingResult, list: List<Purchase> ->
            var pc: Purchase? = null
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.products.contains(productId)) {
                        pc = purchase
                    }
                }
            }
            if (pc == null) return@queryPurchasesAsync
            try {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(pc.purchaseToken)
                    .build()
                val listener =
                    ConsumeResponseListener { billingResult1: BillingResult, purchaseToken: String? ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.e(TAG, "onConsumeResponse: OK")
                            retryConsumeTimes = 0
                            verifyPurchased(false)
                        } else {
                            Log.e(TAG, "consumePurchase: error $billingResult1")
                            if (retryConsumeTimes >= MAX_RETRY_CONSUME_TIMES) {
                                retryConsumeTimes = 0
                                return@ConsumeResponseListener
                            }
                            retryConsumeTimes++
                            consumePurchase(productId)
                        }
                    }
                billingClient!!.consumeAsync(consumeParams, listener)
            } catch (e: Exception) {
                Log.e(TAG, "consumePurchase: error", e)
            }
        }
    }

    private val listInappId: List<String>
        private get() {
            val list: MutableList<String> = ArrayList()
            for (product in listINAPId!!) {
                list.add(product.zza())
            }
            return list
        }
    private val listSubId: List<String>
        private get() {
            val list: MutableList<String> = ArrayList()
            for (product in listSubscriptionId!!) {
                list.add(product.zza())
            }
            return list
        }

    private fun handlePurchase(purchase: Purchase) {

        //tracking adjust
        val price = getPriceWithoutCurrency(idPurchaseCurrent, typeIap)
        val currency = getCurrency(idPurchaseCurrent, typeIap)
        onTrackRevenuePurchase(price as Float, currency, idPurchaseCurrent, typeIap)
        if (purchaseListener != null) {
            isPurchased = true
            purchaseListener!!.onProductPurchased(purchase.orderId, purchase.originalJson)
        }
        if (isConsumePurchase) {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            val listener =
                ConsumeResponseListener { billingResult: BillingResult, _: String? ->
                    Log.d(
                        TAG, "onConsumeResponse: " + billingResult.debugMessage
                    )
                }
            billingClient!!.consumeAsync(consumeParams, listener)
        } else {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                if (!purchase.isAcknowledged) {
                    billingClient!!.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
                        Log.d(TAG, "onAcknowledgePurchaseResponse: " + billingResult.debugMessage)
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            val pricePurchase =
                                getPriceWithoutCurrency(
                                    idPurchaseCurrent,
                                    typeIap
                                ) as Double / 1000000
                            val currencyPurchase = getCurrency(idPurchaseCurrent, typeIap)
                            logConfirmPurchaseGoogle(
                                purchase.orderId, idPurchaseCurrent, purchase.purchaseToken
                            )
                            trackingRevenuePurchase(pricePurchase, currencyPurchase)
                        }
                    }
                }
            }
        }
    }

    private fun trackingRevenuePurchase(amount: Double, currency: String) {
        if (!enableTrackingRevenue) return
        val convertRequest: Call<ConvertCurrencyResponseModel?>? =
            Objects.requireNonNull(getAperoService())
                ?.getAmountBySpecifyCurrency(currency, "USD", amount)
        assert(convertRequest != null)
        convertRequest!!.enqueue(object : Callback<ConvertCurrencyResponseModel?> {
            override fun onResponse(
                call: Call<ConvertCurrencyResponseModel?>,
                response: Response<ConvertCurrencyResponseModel?>
            ) {
                if (response.body() != null) {
                    Log.d(TAG, "getAmountBySpecifyCurrency: " + response.body())
                    logRevenuePurchase(response.body()!!.newAmount)
                }
            }

            override fun onFailure(call: Call<ConvertCurrencyResponseModel?>, t: Throwable) {
                Log.e(TAG, "getAmountBySpecifyCurrency onFailure: ", t)
            }
        })
    }

    fun getPrice(productId: String?): String {
        val skuDetails = skuDetailsINAPMap[productId] ?: return ""
        Log.e(
            TAG, "getPrice: " + skuDetails.oneTimePurchaseOfferDetails!!
                .formattedPrice
        )
        return skuDetails.oneTimePurchaseOfferDetails!!.formattedPrice
    }

    fun getPriceSub(productId: String): String {
        val skuDetails = skuDetailsSubsMap[productId] ?: return ""
        val subsDetail = skuDetails.subscriptionOfferDetails
        val pricingPhaseList = subsDetail!![subsDetail.size - 1].pricingPhases.pricingPhaseList
        Log.e(TAG, "getPriceSub: " + pricingPhaseList[pricingPhaseList.size - 1].formattedPrice)
        return pricingPhaseList[pricingPhaseList.size - 1].formattedPrice
    }

    /**
     * Get Price Pricing Phase List Subs
     *
     * @param productId
     * @return
     */
    fun getPricePricingPhaseList(productId: String): List<PricingPhase>? {
        val skuDetails = skuDetailsSubsMap[productId] ?: return null
        val subsDetail =
            skuDetails.subscriptionOfferDetails
        return subsDetail!![subsDetail.size - 1].pricingPhases.pricingPhaseList
    }

    /**
     * Get Formatted Price by country
     * Get final price with id
     *
     * @param productId
     * @return
     */
    fun getIntroductorySubPrice(productId: String): String {
        val skuDetails = skuDetailsSubsMap[productId] ?: return ""
        return if (skuDetails.oneTimePurchaseOfferDetails != null) skuDetails.oneTimePurchaseOfferDetails!!
            .formattedPrice else if (skuDetails.subscriptionOfferDetails != null) {
            val subsDetail = skuDetails.subscriptionOfferDetails
            val pricingPhaseList = subsDetail!![subsDetail.size - 1].pricingPhases.pricingPhaseList
            pricingPhaseList[pricingPhaseList.size - 1].formattedPrice
        } else {
            ""
        }
    }

    /**
     * Get Currency subs or IAP by country
     *
     * @param productId
     * @param typeIAP
     * @return
     */
    fun getCurrency(productId: String, typeIAP: Int): String {
        val skuDetails =
            (if (typeIAP == TYPE_IAP.PURCHASE) skuDetailsINAPMap[productId] else skuDetailsSubsMap[productId])
                ?: return ""
        return if (typeIAP == TYPE_IAP.PURCHASE) skuDetails.oneTimePurchaseOfferDetails!!
            .priceCurrencyCode else {
            val subsDetail = skuDetails.subscriptionOfferDetails
            val pricingPhaseList = subsDetail!![subsDetail.size - 1].pricingPhases.pricingPhaseList
            pricingPhaseList[pricingPhaseList.size - 1].priceCurrencyCode
        }
    }

    /**
     * Get Price Amount Micros subs or IAP
     * Get final price with id
     *
     * @param productId
     * @param typeIAP
     * @return
     */
    fun getPriceWithoutCurrency(productId: String, typeIAP: Int): Any {
        val skuDetails =
            (if (typeIAP == TYPE_IAP.PURCHASE) skuDetailsINAPMap[productId] else skuDetailsSubsMap[productId])
                ?: return 0
        return if (typeIAP == TYPE_IAP.PURCHASE) skuDetails.oneTimePurchaseOfferDetails!!
            .priceAmountMicros.toDouble() else {
            val subsDetail = skuDetails.subscriptionOfferDetails
            val pricingPhaseList = subsDetail!![subsDetail.size - 1].pricingPhases.pricingPhaseList
            pricingPhaseList[pricingPhaseList.size - 1].priceAmountMicros.toDouble()
        }
    }
    //
    //    public String getOldPrice() {
    //        SkuDetails skuDetails = bp.getPurchaseListingDetails(productId);
    //        if (skuDetails == null)
    //            return "";
    //        return formatCurrency(skuDetails.priceValue / discount, skuDetails.currency);
    //    }
    /**
     * Format currency and price by country
     *
     * @param price
     * @param currency
     * @return
     */
    private fun formatCurrency(price: Double, currency: String): String {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance(currency)
        return format.format(price)
    }

    var discount = 1.0

    @Deprecated("")
    private fun listIdToListProduct(
        listId: List<String?>,
        styleBilling: String
    ): ArrayList<QueryProductDetailsParams.Product> {
        val listProduct = ArrayList<QueryProductDetailsParams.Product>()
        for (id in listId) {
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id!!)
                .setProductType(styleBilling)
                .build()
            listProduct.add(product)
        }
        return listProduct
    }

    private fun syncPurchaseItemsToListProduct(purchaseItems: List<PurchaseItem>?) {
        val listInAppProduct = ArrayList<QueryProductDetailsParams.Product>()
        val listSubsProduct = ArrayList<QueryProductDetailsParams.Product>()
        for (item in purchaseItems!!) {
            var product: QueryProductDetailsParams.Product
            if (item.type == TYPE_IAP.PURCHASE) {
                product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(item.itemId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                listInAppProduct.add(product)
            } else {
                product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(item.itemId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
                listSubsProduct.add(product)
            }
        }
        listINAPId = listInAppProduct
        Log.d(TAG, "syncPurchaseItemsToListProduct: listINAPId " + listINAPId!!.size)
        listSubscriptionId = listSubsProduct
        Log.d(
            TAG,
            "syncPurchaseItemsToListProduct: listSubscriptionId " + listSubscriptionId!!.size
        )
    }

    @IntDef(*[TYPE_IAP.PURCHASE, TYPE_IAP.SUBSCRIPTION])
    annotation class TYPE_IAP {
        companion object {
            const val PURCHASE = 1
            const val SUBSCRIPTION = 2
        }
    }

    companion object {
        private val LICENSE_KEY: String? = null
        private val MERCHANT_ID: String? = null
        private const val TAG = "PurchaseEG"
        const val PRODUCT_ID_TEST = "android.test.purchased"

        @JvmStatic
        @SuppressLint("StaticFieldLeak")
        var instance: AppPurchase? = null
            get() {
                if (field == null) {
                    field = AppPurchase()
                }
                return field
            }
            private set
    }
}
