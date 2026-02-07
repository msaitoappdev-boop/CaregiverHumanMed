package jp.msaitoappdev.caregiver.humanmed.feature.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billing: BillingManager,
    private val premiumRepo: PremiumRepository
) : ViewModel() {

    // UIに公開する isPremium は、BillingManager からのリアルタイムの値を直接参照する
    val isPremium: StateFlow<Boolean> = billing.isPremium

    private val _uiMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    init {
        // BillingManager の isPremium の変更を監視し、DataStore への永続化を指示する
        // これにより、UIの即時性と、アプリ再起動後の状態復元を両立する
        viewModelScope.launch {
            billing.isPremium.collect { isPremiumValue ->
                premiumRepo.savePremiumStatus(isPremiumValue)
            }
        }
    }

    fun startPurchase(activity: Activity) {
        viewModelScope.launch {
            val productDetails = billing.getProductDetails()
            if (productDetails == null) {
                _uiMessage.emit("商品情報を取得できませんでした")
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
