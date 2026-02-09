package jp.msaitoappdev.caregiver.humanmed.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * プレミアム状態（課金済みか）の取得・保存を担う。
 * 実装は :core にあり、BillingManager を真実源とする。
 */
interface PremiumRepository {
    val isPremium: StateFlow<Boolean>
    suspend fun refreshFromBilling()
    suspend fun savePremiumStatus(isPremium: Boolean)
    suspend fun setPremiumForDebug(enabled: Boolean)
}
