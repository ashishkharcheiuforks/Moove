package com.backdoor.moove.modern_ui.donate

import android.app.Activity
import android.content.Context
import androidx.lifecycle.*
import com.android.billingclient.api.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class DonateViewModel : ViewModel(), KoinComponent, LifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    private val context: Context by inject()

    private val _purchases = MutableLiveData<List<Purchase>>()
    val purchases: LiveData<List<Purchase>> = _purchases

    private val _skuDetails = MutableLiveData<List<SkuDetails>>()
    val skuDetails: LiveData<List<SkuDetails>> = _skuDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var billingClient: BillingClient
    private var isReady = false

    init {
        billingClient = BillingClient.newBuilder(context).setListener(this).build()
        billingClient.startConnection(this)
    }

    fun buy(activity: Activity, sku: String) {
        val skuDetails = findDetails(sku)
        if (isReady && skuDetails != null) {
            _isLoading.postValue(true)
            val params = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()
            billingClient.launchBillingFlow(activity, params)
        }
    }

    private fun findDetails(sku: String): SkuDetails? {
        return skuDetails.value.run {
            if (this != null) {
                var details: SkuDetails? = null
                for (skuDetails in this) {
                    if (skuDetails.sku == sku) {
                        details = skuDetails
                        break
                    }
                }
                details
            } else null
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        Timber.d("onPurchasesUpdated: ${billingResult?.responseCode}, $purchases")
        _isLoading.postValue(false)
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            _purchases.postValue(purchases)
        }
    }

    override fun onBillingServiceDisconnected() {
        isReady = false
    }

    override fun onBillingSetupFinished(billingResult: BillingResult?) {
        Timber.d("onBillingSetupFinished: ${billingResult?.responseCode}")
        isReady = true
        loadSkuDetails()
        loadPurchases()
    }

    private fun loadPurchases() {
        val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            onPurchasesUpdated(result.billingResult, result.purchasesList)
        }
    }

    private fun loadSkuDetails() {
        _isLoading.postValue(true)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(listOf(SKU_1, SKU_2, SKU_3, SKU_4)).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            Timber.d("loadSkuDetails: $billingResult, $skuDetailsList")
            _isLoading.postValue(false)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _skuDetails.postValue(skuDetailsList)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (isReady) {
            billingClient.endConnection()
        }
    }

    companion object {
        const val SKU_1 = "donate01"
        const val SKU_2 = "donate02"
        const val SKU_3 = "donate03"
        const val SKU_4 = "donate04"
    }
}
