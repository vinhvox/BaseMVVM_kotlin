package com.vapps.module_ads.control.helper.adnative.factory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.vapps.module_ads.R
import com.vapps.module_ads.control.admob.getAdRequest
import com.vapps.module_ads.control.billing.AppPurchase
import com.vapps.module_ads.control.config.ErrorCode
import com.vapps.module_ads.control.data.ContentAd
import com.vapps.module_ads.control.event.VioLogEventManager
import com.vapps.module_ads.control.listener.NativeAdCallback
import com.vapps.module_ads.control.utils.AdType

class AdmobNativeFactoryImpl : AdmobNativeFactory {
    override fun requestNativeAd(context: Context, adId: String, adCallback: NativeAdCallback) {
        if (AppPurchase.instance?.isPurchased == true) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    1999,
                    "App isPurchased",
                    "",
                    null,
                    null
                )
            )
            return
        }
        val builder = AdLoader.Builder(context, adId)

        val videoOptions =
            VideoOptions.Builder().setStartMuted(true).build()

        val adOptions = com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
            .setVideoOptions(videoOptions).build()

        builder.withNativeAdOptions(adOptions)
        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { nativeAd ->
                adCallback.onAdLoaded(ContentAd.AdmobAd.ApNativeAd(nativeAd))
                nativeAd.setOnPaidEventListener { adValue: AdValue ->
                    VioLogEventManager.logPaidAdImpression(
                        context,
                        adValue,
                        adId,
                        nativeAd.responseInfo!!.mediationAdapterClassName!!, AdType.NATIVE
                    )
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    adCallback.onAdFailedToLoad(error)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback.onAdImpression()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback.onAdClicked()
                    VioLogEventManager.logClickAdsEvent(context, adId)
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(getAdRequest())
    }

    override fun populateNativeAdView(
        activity: Context,
        nativeAd: NativeAd,
        nativeAdViewId: Int,
        adPlaceHolder: FrameLayout,
        containerShimmerLoading: ShimmerFrameLayout?,
        adCallback: NativeAdCallback
    ) {
        val adView = LayoutInflater.from(activity).inflate(nativeAdViewId, null) as NativeAdView
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.mediaContent?.let { (adView.mediaView)?.setMediaContent(it) }


        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        nativeAd.body?.let {
            adView.bodyView?.visibility = View.VISIBLE
            adView.bodyView?.let { view ->
                (view as TextView).text = it
            }
        } ?: kotlin.run {
            adView.bodyView?.visibility = View.INVISIBLE
        }

        nativeAd.callToAction?.let {
            adView.callToActionView?.visibility = View.VISIBLE
            adView.callToActionView?.let { view ->
                (view as TextView).text = it
            }
        } ?: kotlin.run {
            adView.callToActionView?.visibility = View.INVISIBLE
        }
        nativeAd.icon?.let {
            adView.iconView?.visibility = View.VISIBLE
            adView.iconView?.let { view ->
                (view as ImageView).setImageDrawable(it.drawable)
            }
        } ?: kotlin.run {
            adView.iconView?.visibility = View.GONE
        }
        nativeAd.price?.let {
            adView.priceView?.visibility = View.VISIBLE
            adView.priceView?.let { view ->
                (view as TextView).text = it
            }
        } ?: kotlin.run {
            adView.priceView?.visibility = View.INVISIBLE
        }

        nativeAd.starRating?.let {
            adView.starRatingView?.visibility = View.VISIBLE
            adView.starRatingView?.let { view ->
                (view as RatingBar).rating = it.toFloat()
            }
        } ?: kotlin.run {
            adView.starRatingView?.visibility = View.INVISIBLE
        }
        nativeAd.advertiser?.let {
            adView.advertiserView?.visibility = View.VISIBLE
            adView.advertiserView?.let { view ->
                (view as TextView).text = it
            }

        } ?: kotlin.run {
            adView.advertiserView?.visibility = View.INVISIBLE
        }
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val mediaContent = nativeAd.mediaContent
        val vc = mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc != null && mediaContent.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {
                        // Publishers should allow native ads to complete video playback before
                        // refreshing or replacing them with another ad in the same UI location.
                        super.onVideoEnd()
                    }
                }
        }
        try {
            adPlaceHolder.visibility = View.VISIBLE
            adPlaceHolder.removeAllViews()
            adPlaceHolder.addView(adView)
            containerShimmerLoading?.visibility = View.GONE
        } catch (ex: Exception) {
            adCallback.onAdFailedToShow(AdError(ErrorCode.SHOW_FAIL_CODE, ex.message.toString(), ""))
        }
    }
}