package jp.msaitoappdev.caregiver.humanmed.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()   // 一回購入を扱う場合
                // .enablePrepaidPlans()   // 任意：プリペイド型を扱うなら
                .build()
        )
        .build()

    private var connected = false
    private var cachedProductDetails: ProductDetails? = null

    // UI/Repo に購入イベントを配信
    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 4)
    val purchaseEvents = _purchaseEvents.asSharedFlow()

    suspend fun connect(): Boolean = suspendCancellableCoroutine { cont ->
        if (connected) {
            cont.resume(true); return@suspendCancellableCoroutine
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                connected = false
            }
            override fun onBillingSetupFinished(result: BillingResult) {
                connected = result.responseCode == BillingClient.BillingResponseCode.OK
                cont.resume(connected)
            }
        })
    }

    suspend fun getProductDetails(productId: String): ProductDetails? {
        if (!connected && !connect()) return null
        cachedProductDetails?.let { if (it.productId == productId) return it }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        return suspendCancellableCoroutine { cont ->
            client.queryProductDetailsAsync(params) { result, details ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && details.isNotEmpty()) {
                    cachedProductDetails = details.first()
                    cont.resume(cachedProductDetails)
                } else cont.resume(null)
            }
        }
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull() // 無料トライアルなしのフルプライス・オファーを1つ用意しておく
            ?.offerToken ?: run {
            _purchaseEvents.tryEmit(PurchaseEvent.Error("オファーが見つかりません"))
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
                    when (purchase.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> {
                            if (!purchase.isAcknowledged) {
                                val ackParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                                client.acknowledgePurchase(ackParams) { ackResult ->
                                    if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                        _purchaseEvents.tryEmit(PurchaseEvent.Success(purchase))
                                    } else {
                                        _purchaseEvents.tryEmit(
                                            PurchaseEvent.Error("ACK失敗: ${ackResult.responseCode}")
                                        )
                                    }
                                }
                            } else {
                                _purchaseEvents.tryEmit(PurchaseEvent.Success(purchase))
                            }
                        }
                        Purchase.PurchaseState.PENDING -> {
                            // 保留中：権利は付与しない
                            _purchaseEvents.tryEmit(PurchaseEvent.Error("お支払い処理中です。完了後に有効化されます。"))
                        }
                        else -> { /* UNSPECIFIED_STATE などは無視 */ }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Canceled)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseEvents.tryEmit(PurchaseEvent.AlreadyOwned)
            }
            else -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Error("エラー: ${result.responseCode}"))
            }
        }
    }

    suspend fun queryActiveSubscriptions(): List<Purchase> {
        if (!connected && !connect()) return emptyList()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS).build()

        return suspendCancellableCoroutine { cont ->
            client.queryPurchasesAsync(params) { _, list ->
                cont.resume(list)
            }
        }
    }

    sealed interface PurchaseEvent {
        data class Success(val purchase: Purchase) : PurchaseEvent
        data object Canceled : PurchaseEvent
        data object AlreadyOwned : PurchaseEvent
        data class Error(val message: String) : PurchaseEvent
    }
}
