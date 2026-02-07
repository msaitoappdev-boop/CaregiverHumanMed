package jp.msaitoappdev.caregiver.humanmed.core.billing

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

/**
 * 購読（SUBS）の購買・状態同期を担う最小ユーティリティ。
 * - ProductDetails の取得
 * - 購入フロー起動
 * - ACK（3日以内必須）
 * - 所有状態の問い合わせ → プレミアム権限の保存/復元
 *
 * 将来:
 * - RTDN + SubscriptionsV2 を真実源に（TODO）
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) : PurchasesUpdatedListener {

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(BillingConfig.PREFS_NAME, Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val TAG = "BugHunt-Premium"

    // ---- BillingClient -------------------------------------------------------
    private val client: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts() // 一回購入を扱うならON
                .build()
        )
        .build()

    private var isConnected = false
    private var cachedProductDetails: ProductDetails? = null

    // ---- 外部公開：権利（isPremium）と購入イベント ----------------------------
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

    // ---- Public API ----------------------------------------------------------

    suspend fun connect(): Boolean = suspendCancellableCoroutine { cont ->
        if (isConnected) { cont.resume(true); return@suspendCancellableCoroutine }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isConnected = false
            }
            override fun onBillingSetupFinished(result: BillingResult) {
                isConnected = result.responseCode == BillingClient.BillingResponseCode.OK
                cont.resume(isConnected)
                if (isConnected) {
                    // 起動直後の整合性取り
                    scope.launch { refreshEntitlements() }
                }
            }
        })
    }

    /**
     * 無引数版：固定 Product ID（Console と同期）で ProductDetails を取得。
     */
    suspend fun getProductDetails(): ProductDetails? {
        if (!isConnected && !connect()) return null
        cachedProductDetails?.let { return it.takeIf { d -> d.productId == BillingConfig.PRODUCT_ID_PREMIUM_MONTHLY } }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingConfig.PRODUCT_ID_PREMIUM_MONTHLY)
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
        // basePlanId のみで特典を選択（より安全な方法は offerId も指定すること）
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == BillingConfig.BASE_PLAN_ID_MONTHLY }
            ?.offerToken
            ?: run {
                _purchaseEvents.tryEmit(PurchaseEvent.Error("購入オファー（月額）が見つかりません"))
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
                            // 3日以内の ACK は必須。
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
                                            PurchaseEvent.Error("承認に失敗: ${ackResult.responseCode}")
                                        )
                                    }
                                }
                            } else {
                                _purchaseEvents.tryEmit(PurchaseEvent.Success(purchase))
                                scope.launch { refreshEntitlements() }
                            }
                        }
                        Purchase.PurchaseState.PENDING -> {
                            _purchaseEvents.tryEmit(
                                PurchaseEvent.Error("お支払い処理中です。完了後に自動で有効化されます。")
                            )
                        }
                        else -> Unit // UNSPECIFIED_STATE 等
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Canceled)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // 既に所有 → 状態再同期
                _purchaseEvents.tryEmit(PurchaseEvent.AlreadyOwned)
                scope.launch { refreshEntitlements() }
            }
            else -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Error("購入エラー: ${result.responseCode}"))
            }
        }
    }

    /**
     * 端末ローカルの所有状況から isPremium を更新。
     * 本番運用ではサーバー（SubscriptionsV2 + RTDN）を真実源にして二段チェックを推奨。
     */
    suspend fun refreshEntitlements() {
        if (!isConnected && !connect()) return

        val owned = suspendCancellableCoroutine<List<Purchase>> { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            client.queryPurchasesAsync(params, PurchasesResponseListener { result, purchases ->
                if (continuation.isCancelled) {
                    return@PurchasesResponseListener
                }
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchases)
                } else {
                    Log.e(TAG, "Failed to query purchases: ${result.debugMessage}")
                    continuation.resume(emptyList())
                }
            })
        }

        // ローカル判定：対象 Product のアクティブ所有があれば Premium とみなす
        val premium = owned.any { p ->
            p.products.contains(BillingConfig.PRODUCT_ID_PREMIUM_MONTHLY) &&
                    p.purchaseState == Purchase.PurchaseState.PURCHASED && p.isAutoRenewing
        }

        Log.d(TAG, "BillingManager: Querying purchases finished. Result: $premium")

        savePremiumToPrefs(premium)
        _isPremium.value = premium
    }

    // ---- 互換API（既存 PremiumRepositoryImpl から呼ばれている想定） ----------
    suspend fun queryActiveSubscriptions(): List<Purchase> {
        if (!isConnected && !connect()) return emptyList()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        return suspendCancellableCoroutine<List<Purchase>> { continuation ->
            client.queryPurchasesAsync(params, PurchasesResponseListener { result, purchases ->
                if (continuation.isCancelled) {
                    return@PurchasesResponseListener
                }
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchases)
                } else {
                    continuation.resume(emptyList())
                }
            })
        }
    }

    // ---- Prefs ヘルパ -------------------------------------------------------
    private fun loadPremiumFromPrefs(): Boolean =
        prefs.getBoolean(BillingConfig.KEY_IS_PREMIUM, false)

    private fun savePremiumToPrefs(value: Boolean) {
        prefs.edit(commit = true) {
            putBoolean(BillingConfig.KEY_IS_PREMIUM, value)
            putLong(BillingConfig.KEY_LAST_REFRESH_EPOCH_MS, System.currentTimeMillis())
        }
    }
}
