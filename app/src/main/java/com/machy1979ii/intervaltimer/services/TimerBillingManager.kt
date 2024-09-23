package com.machy1979ii.intervaltimer.services

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class TimerBillingManager {

    companion object {

        private lateinit var billingClient: BillingClient
        private lateinit var sharedPreferences: SharedPreferences

        private val _adsDisabled = MutableStateFlow(true)
        val adsDisabled: StateFlow<Boolean> = _adsDisabled.asStateFlow()

        // Track whether a purchase is in progress
        @Volatile
        private var isPurchaseInProgress = false

        //musel jsem udělaj jeden listener pro MainActivity a druhý pro Setsound

        private var purchaseCompletedListenerMainActivity: OnPurchaseCompletedListenerMainActivity? = null
        interface OnPurchaseCompletedListenerMainActivity {
            fun onPurchaseCompletedMainActivity()
        }

        private var purchaseCompletedListenerSetSoundActivity: OnPurchaseCompletedListenerSetSoundActivity? = null
        interface OnPurchaseCompletedListenerSetSoundActivity {
            fun onPurchaseCompletedSetSound()
        }

        fun initialize(application: Application, listenerMainActivity: OnPurchaseCompletedListenerMainActivity? = null, listenerSetSoundActivity: OnPurchaseCompletedListenerSetSoundActivity? = null) {
            //musel jsem udělaj jeden listener pro MainActivity a druhý pro Setsound
            Log.d("Boxing listener BillM:", "initialize")
            if (listenerMainActivity != null) {
                Log.d("Boxing listener BillM:", "initialize set listener")
                    purchaseCompletedListenerMainActivity = listenerMainActivity
                }

            if (listenerSetSoundActivity != null) {
                Log.d("Boxing listener BillM:", "initialize set listener")
                purchaseCompletedListenerSetSoundActivity = listenerSetSoundActivity
            }


            sharedPreferences =
                application.getSharedPreferences("boxing_timer_prefers", Context.MODE_PRIVATE)

            _adsDisabled.value = sharedPreferences.getBoolean("ads_disabled", false)
            Log.d("Boxing no Ads2:", "1")
            billingClient = BillingClient.newBuilder(application)
                .setListener { billingResult, purchases ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                        purchases.forEach { purchase ->
                            Log.d("Boxing no Ads2:", "2")
                            handlePurchase(purchase)
                            Log.d("Boxing no Ads2:", "3")
                        }
                    }
                    // Reset isPurchaseInProgress when purchase flow finishes
                    isPurchaseInProgress = false
                }
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                )
                .build()

            startConnection()
        }

        private fun startConnection() {
            Log.d("Boxing no Ads2:", "5")

            //házelo mi to na Google Play občas chybu, více než 999 pokusů o připojení, tak to řeším takto plus níže:
            if (billingClient.isReady) {
                // Billing client is already connected, no need to reconnect
                return
            }

            billingClient.startConnection(object : BillingClientStateListener {
                /*                override fun onBillingServiceDisconnected() {
                                    startConnection() // Znovu se pokusí o připojení
                                }*/
                //házelo mi to na Google Play občas chybu, více než 999 pokusů o připojení, tak to řeším takto:
                override fun onBillingServiceDisconnected() {
                    // Add a delay or backoff strategy before attempting to reconnect
                    Handler(Looper.getMainLooper()).postDelayed({
                        startConnection()
                    }, 2000) // Retry after 2 seconds, you may adjust this time
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    Log.d("Boxing no Ads2:", "5-6")
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryPurchases()
                        Log.d("Boxing no Ads2:", "6")
                    }
                }
            })
        }

        private fun queryPurchases() {
            Log.d("Boxing no Ads2:", "51")
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchased =
                        purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    Log.d("Boxing no Ads2:", "purchase3: "+purchased.toString())

                    Log.d("Boxing no Ads2:", "52")
                    Log.d("Boxing no Ads2:", "purchased: " + purchased.toString())
                    if (purchased != _adsDisabled.value) { //v shared preferences je uložená hotnota false, jakože nezakoupeno, ale Google ověřil, že gmail účet si to už koupil
                        //bude se jednat o dříve zakoupenou aplikaci, odinstalaci a pak znovuinstalaci, tak je potřeba to nastavit i v apce, že si ji uživatel koupil
                        Log.d("Boxing no Ads2:", "53")
                        _adsDisabled.value = !_adsDisabled.value
                        savePurchaseStateToPreferences(purchased)
                        purchaseCompletedListenerMainActivity?.onPurchaseCompletedMainActivity()
                        purchaseCompletedListenerSetSoundActivity?.onPurchaseCompletedSetSound()
                        Log.d("Boxing no Ads2:", "54")
                    }

                    if (false) { //IMPORTANT, it is just for debugging billing - here is purchase deactivated after new open app - so it can be testing purchase again
                        purchases.forEach { purchase ->
                            Log.d("Boxing no Ads2:", "55")
                            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                val consumeParams = ConsumeParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()

                                billingClient.consumeAsync(consumeParams) { consumeResult, purchaseToken ->
                                    if (consumeResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                        Log.d(
                                            "Boxing no Ads2:",
                                            "Purchase consumed: $purchaseToken"
                                        )
                                        Log.d("BillingManager", "Purchase consumed: $purchaseToken")
                                        savePurchaseStateToPreferences(adsRemoved = false)
                                    } else {
                                        Log.d(
                                            "Boxing no Ads2:",
                                            "Failed to consume purchase: ${consumeResult.debugMessage}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fun startPurchase(activity: Activity) {
            if (!billingClient.isReady) {
                Log.e("Boxing no Ads:", "BillingClient is not ready")
                return
            } else  {
                Log.d("Boxing no Ads:", "BillingClient IS ready")
            }
            Log.d("Boxing no Ads:", "startPurchase1 isPurchaseInProgress: "+isPurchaseInProgress.toString())
            if (isPurchaseInProgress) {
                Log.d("Boxing no Ads:", "startPurchase1 - purchase in progress")
                return
            }
            Log.d("Boxing no Ads:", "startPurchase1")
            isPurchaseInProgress = true

            Log.d("Boxing no Ads:", "startPurchase2")
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("buy_app_remove_ads_interval_timer")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            Log.d("Boxing no Ads:", "startPurchase3")
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()
            Log.d("Boxing no Ads:", "startPurchase4")
/*            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                Log.d("Boxing no Ads:", "Response code: " + billingResult.responseCode)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                    Log.d("Boxing no Ads:", "startPurchase5")
                    Log.d("Boxing no Ads:", "Product details list size: " + productDetailsList.size)

                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetailsList[0])
                                    .build()
                            )
                        )
                        .build()
                    Log.d("Boxing no Ads:", "startPurchase5-6")
                    billingClient.launchBillingFlow(activity, flowParams)

                } else {
                    // Reset the flag if queryProductDetailsAsync fails
                    Log.d("Boxing no Ads:", "startPurchase6")
                    isPurchaseInProgress = false
                    purchaseCompletedListener?.onPurchaseCompleted()
                }
            }*/
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                Log.d("Boxing no Ads:", "Response code: " + billingResult.responseCode)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                    Log.d("Boxing no Ads:", "Product details list size: " + productDetailsList.size)

                    if (productDetailsList.isNotEmpty()) {
                        Log.d("Boxing no Ads:", "startPurchase5")
                        val flowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetailsList[0])
                                        .build()
                                )
                            )
                            .build()

                        billingClient.launchBillingFlow(activity, flowParams)
                    } else {
                        Log.e("Boxing no Ads:", "Product details list is empty!!!")
                        isPurchaseInProgress = false
                        purchaseCompletedListenerMainActivity?.onPurchaseCompletedMainActivity()
                        purchaseCompletedListenerSetSoundActivity?.onPurchaseCompletedSetSound()
                    }
                } else {
                    Log.e("Boxing no Ads:", "Error querying product details: " + billingResult.debugMessage)
                    isPurchaseInProgress = false
                    purchaseCompletedListenerMainActivity?.onPurchaseCompletedMainActivity()
                    purchaseCompletedListenerSetSoundActivity?.onPurchaseCompletedSetSound()
                }
            }

        }


        private fun handlePurchase(purchase: Purchase) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                Log.d("Boxing no Ads2:", "3-4")

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("Boxing no Ads2:", "4")
                        _adsDisabled.value = true
                        savePurchaseStateToPreferences(true)
                        purchaseCompletedListenerMainActivity?.onPurchaseCompletedMainActivity()
                        purchaseCompletedListenerSetSoundActivity?.onPurchaseCompletedSetSound()
                    }
                }
            }
        }

        private fun savePurchaseStateToPreferences(adsRemoved: Boolean) {
            Log.d("Boxing no Ads2:", "savePurchaseStateToPreferences")
            with(sharedPreferences.edit()) {
                putBoolean("ads_disabled", adsRemoved)
                apply()
            }
        }

        fun endConnection() {
            billingClient.endConnection()
        }
    }
}
