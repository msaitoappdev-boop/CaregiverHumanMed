package com.msaitodev.feature.billing

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.core.common.billing.BillingManager
import com.msaitodev.core.common.billing.BillingProvider
import com.msaitodev.core.common.billing.PaywallConfig
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isPremium: Boolean = false,
    val config: PaywallConfig? = null
)

sealed interface PaywallEvent {
    data class ShowMessage(@StringRes val messageResId: Int) : PaywallEvent
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billing: BillingManager,
    private val billingProvider: BillingProvider,
    private val premiumRepo: PremiumRepository
) : ViewModel() {

    val uiState: StateFlow<PaywallUiState> = premiumRepo.isPremium
        .map { isPremium ->
            PaywallUiState(
                isPremium = isPremium,
                config = billingProvider.paywallConfig
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PaywallUiState(config = billingProvider.paywallConfig)
        )

    private val _event = MutableSharedFlow<PaywallEvent>()
    val event: SharedFlow<PaywallEvent> = _event.asSharedFlow()

    fun onPurchaseClick(activity: Activity) {
        viewModelScope.launch {
            val productDetails = billing.getProductDetails()
            if (productDetails == null) {
                _event.emit(PaywallEvent.ShowMessage(R.string.paywall_error_product_details))
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
