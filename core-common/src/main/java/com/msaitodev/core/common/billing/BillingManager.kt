package com.msaitodev.core.common.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "BillingManager"
private const val ERR_SEPARATOR = ": "

/**
 * 購読（SUBS）の購買・状態同期を担う最小ユーティリティ。
 * ドメイン（クイズ/介護）に依存しないよう、プロダクトID等は BillingProvider 経由で取得する。
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val billingProvider: BillingProvider
) : PurchasesUpdatedListener {

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(BillingConfig.PREFS_NAME, Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val client: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private var isConnected = false
    private var cachedProductDetails: ProductDetails? = null

    private val _isPremium = MutableStateFlow(loadPremiumFromPrefs())
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    sealed interface PurchaseEvent {
        data class Success(val purchase: Purchase) : PurchaseEvent
        data object Canceled : PurchaseEvent
        data object AlreadyOwned : PurchaseEvent
        data class Error(val message: String) : PurchaseEvent
    }

    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>(
        replay = 0, extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val purchaseEvents: SharedFlow<PurchaseEvent> = _purchaseEvents.asSharedFlow()

    suspend fun connect(): Boolean = suspendCancellableCoroutine { cont ->
        if (isConnected) {
            cont.resume(true); return@suspendCancellableCoroutine
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isConnected = false
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                isConnected = result.responseCode == BillingClient.BillingResponseCode.OK
                cont.resume(isConnected)
                if (isConnected) {
                    scope.launch { refreshEntitlements() }
                }
            }
        })
    }

    suspend fun getProductDetails(): ProductDetails? {
        if (!isConnected && !connect()) return null
        cachedProductDetails?.let { return it.takeIf { d -> d.productId == billingProvider.productIdPremium } }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(billingProvider.productIdPremium)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        return suspendCancellableCoroutine { cont ->
            client.queryProductDetailsAsync(params) { result: BillingResult, list: MutableList<ProductDetails> ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && list.isNotEmpty()) {
                    val details = list.first()
                    cachedProductDetails = details
                    cont.resume(details)
                } else {
                    cont.resume(null)
                }
            }
        }
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == billingProvider.basePlanId }
            ?.offerToken
            ?: run {
                _purchaseEvents.tryEmit(PurchaseEvent.Error(billingProvider.errorOfferNotFound))
                return
            }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            ).build()

        client.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) return
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            val ackParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            client.acknowledgePurchase(ackParams) { ackResult ->
                                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                    _purchaseEvents.tryEmit(PurchaseEvent.Success(purchase))
                                    scope.launch { refreshEntitlements() }
                                } else {
                                    _purchaseEvents.tryEmit(
                                        PurchaseEvent.Error("${billingProvider.errorAcknowledgeFailed}$ERR_SEPARATOR${ackResult.responseCode}")
                                    )
                                }
                            }
                        } else {
                            _purchaseEvents.tryEmit(PurchaseEvent.Success(purchase))
                            scope.launch { refreshEntitlements() }
                        }
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        _purchaseEvents.tryEmit(
                            PurchaseEvent.Error(billingProvider.errorPending)
                        )
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Canceled)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseEvents.tryEmit(PurchaseEvent.AlreadyOwned)
                scope.launch { refreshEntitlements() }
            }
            else -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Error("${billingProvider.errorGeneral}$ERR_SEPARATOR${result.responseCode}"))
            }
        }
    }

    /**
     * 最新の購読状態を Google Play から取得して同期します。
     * ネットワークエラーや接続失敗時は安全に false を返します。
     * @return プレミアム状態が有効である場合は true、そうでない場合は false を返します。
     */
    suspend fun refreshEntitlements(): Boolean {
        if (!isConnected && !connect()) {
            Log.w(TAG, "refreshEntitlements failed: BillingClient is not connected")
            return false
        }

        val owned = suspendCancellableCoroutine<List<Purchase>> { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            client.queryPurchasesAsync(params, PurchasesResponseListener { result, purchases ->
                if (continuation.isCancelled) return@PurchasesResponseListener
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchases)
                } else {
                    Log.w(TAG, "Query purchases failed: ${result.responseCode}")
                    continuation.resume(emptyList())
                }
            })
        }

        val premium = owned.any { p ->
            p.products.contains(billingProvider.productIdPremium) &&
                    p.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        savePremiumToPrefs(premium)
        _isPremium.value = premium
        return premium
    }

    fun setPremiumForDebug(enabled: Boolean) {
        savePremiumToPrefs(enabled)
        _isPremium.value = enabled
    }

    private fun loadPremiumFromPrefs(): Boolean =
        prefs.getBoolean(BillingConfig.KEY_IS_PREMIUM, false)

    private fun savePremiumToPrefs(value: Boolean) {
        prefs.edit(commit = true) {
            putBoolean(BillingConfig.KEY_IS_PREMIUM, value)
            putLong(BillingConfig.KEY_LAST_REFRESH_EPOCH_MS, System.currentTimeMillis())
        }
    }
}
