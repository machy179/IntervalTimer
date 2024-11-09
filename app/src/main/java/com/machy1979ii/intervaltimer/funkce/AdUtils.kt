package com.machy1979ii.intervaltimer.funkce

import android.graphics.Rect
import android.os.Build
import android.view.WindowMetrics
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class AdUtils {
    companion object { // Adding a companion object to define static methods/fields
        @JvmStatic // This annotation ensures compatibility with Java
        fun loadBanner(
            adView: AdView,
            AD_UNIT_ID: String?,
            activity: AppCompatActivity?,
            adContainerView: FrameLayout?,
        ) {
            val adUtils = AdUtils() // Creating an instance of AdUtils
            adUtils.internalLoadBanner(adView, AD_UNIT_ID, activity, adContainerView)
        }

        //Google Billing
        @JvmStatic
        fun removeAds(adView: AdView?, adContainerView: FrameLayout) {
            adView?.let {
                it.destroy() // Zničit AdView
                adContainerView.removeView(it) // Odstranit AdView z kontejneru
                adContainerView.viewTreeObserver.removeOnGlobalLayoutListener { } // Odebrat posluchače globálního rozvržení
            }
        }
    }

    private fun internalLoadBanner(
        adView: AdView,
        AD_UNIT_ID: String?,
        activity: AppCompatActivity?,
        adContainerView: FrameLayout?,
    ) {
        adView.adUnitId = AD_UNIT_ID!!
        val adRequest = AdRequest.Builder().build()
        val adSize: AdSize = getAdSize(activity!!, adContainerView!!)
        adView.setAdSize(adSize)
        adView.loadAd(adRequest)
    }

    private fun getAdSize(activity: AppCompatActivity, adContainerView: FrameLayout): AdSize {
        var windowMetrics: WindowMetrics? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowMetrics = activity.windowManager.currentWindowMetrics
        }
        var bounds: Rect? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bounds = windowMetrics!!.bounds
        }
        var adWidthPixels = adContainerView.width.toFloat()

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0f) {
            if (bounds != null) {
                adWidthPixels = bounds!!.width().toFloat()
            } else {
                // Set a fallback width if bounds is null
                adWidthPixels = activity.resources.displayMetrics.widthPixels.toFloat()
            }

        }


        val density = activity.resources.displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }


}
