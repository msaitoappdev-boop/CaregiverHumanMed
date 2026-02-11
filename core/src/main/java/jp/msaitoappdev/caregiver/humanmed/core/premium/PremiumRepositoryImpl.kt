package jp.msaitoappdev.caregiver.humanmed.core.premium

import android.util.Log
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val billing: BillingManager
) : PremiumRepository {

    private val TAG = "BugHunt-Premium"

    override val isPremium: StateFlow<Boolean> = billing.isPremium

    override suspend fun refreshFromBilling() {
        // Log.d(TAG, "PremiumRepositoryImpl.refreshFromBilling: Delegating to BillingManager")
        billing.refreshEntitlements()
    }

    override suspend fun savePremiumStatus(isPremium: Boolean) {
        // This is now managed by BillingManager and SharedPreferences
        // The method is kept for API compatibility if other parts of the app use it,
        // but it should ideally be removed in a future refactoring.
        // Log.w(TAG, "savePremiumStatus is deprecated. The value is now directly controlled by BillingManager.")
    }

    override suspend fun setPremiumForDebug(enabled: Boolean) {
        billing.setPremiumForDebug(enabled)
    }
}
