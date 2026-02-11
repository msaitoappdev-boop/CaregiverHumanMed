package jp.msaitoappdev.caregiver.humanmed.feature.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isPremium: Boolean = false
)

sealed interface PaywallEvent {
    data class ShowMessage(val message: String) : PaywallEvent
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billing: BillingManager,
    private val premiumRepo: PremiumRepository
) : ViewModel() {

    val uiState: StateFlow<PaywallUiState> = premiumRepo.isPremium
        .map { PaywallUiState(isPremium = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PaywallUiState()
        )

    private val _event = MutableSharedFlow<PaywallEvent>()
    val event: SharedFlow<PaywallEvent> = _event.asSharedFlow()

    fun onPurchaseClick(activity: Activity) {
        viewModelScope.launch {
            val productDetails = billing.getProductDetails()
            if (productDetails == null) {
                _event.emit(PaywallEvent.ShowMessage("商品情報を取得できませんでした"))
                return@launch
            }
            billing.launchPurchase(activity, productDetails)
        }
    }

    fun refresh() {
        viewModelScope.launch { premiumRepo.refreshFromBilling() }
    }

    fun devTogglePremium(enable: Boolean) {
        viewModelScope.launch { premiumRepo.setPremiumForDebug(enable) }
    }
}
