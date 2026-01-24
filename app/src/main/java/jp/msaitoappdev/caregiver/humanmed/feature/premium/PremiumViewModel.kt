
// feature/premium/PremiumViewModel.kt
package jp.msaitoappdev.caregiver.humanmed.feature.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingConfig
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billing: BillingManager,
    private val repo: jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = repo.isPremiumFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    private val _uiMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    private var productDetails: ProductDetails? = null

    init {
        // 起動時：接続→商品取得→購読復元
        viewModelScope.launch {
            val ok = billing.connect()
            if (ok) {
                productDetails = billing.getProductDetails(BillingConfig.PRODUCT_ID_PREMIUM_MONTHLY)
                repo.refreshFromBilling()
            } else {
                _uiMessage.tryEmit("Billing サービスに接続できません")
            }
        }
        // 購入イベント購読
        viewModelScope.launch {
            billing.purchaseEvents.collect { e ->
                when (e) {
                    is BillingManager.PurchaseEvent.Success -> {
                        repo.refreshFromBilling()
                        _uiMessage.emit("購入が完了しました")
                    }
                    BillingManager.PurchaseEvent.Canceled ->
                        _uiMessage.emit("購入をキャンセルしました")
                    BillingManager.PurchaseEvent.AlreadyOwned -> {
                        repo.refreshFromBilling()
                        _uiMessage.emit("すでに購入済みです")
                    }
                    is BillingManager.PurchaseEvent.Error ->
                        _uiMessage.emit("購入エラー: ${e.message}")
                }
            }
        }
    }

    fun startPurchase(activity: Activity) {
        val pd = productDetails
        if (pd == null) {
            viewModelScope.launch { _uiMessage.emit("商品情報を取得できませんでした") }
            return
        }
        billing.launchPurchase(activity, pd)
    }

    fun refresh() {
        viewModelScope.launch { repo.refreshFromBilling() }
    }

    // 開発時: Play Consoleが未設定でもUI動作を確認したい場合
    fun devTogglePremium(enable: Boolean) {
        viewModelScope.launch { repo.setPremiumForDebug(enable) }
    }
}
