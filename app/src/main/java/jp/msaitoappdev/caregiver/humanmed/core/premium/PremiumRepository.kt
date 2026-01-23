// core/premium/PremiumRepository.kt
package jp.msaitoappdev.caregiver.humanmed.core.premium

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.android.billingclient.api.Purchase
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingConfig
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val billing: BillingManager
) {
    val isPremiumFlow: Flow<Boolean> =
        dataStore.data.map { it[PremiumPrefs.IS_PREMIUM] ?: false }

    suspend fun refreshFromBilling() {
        val subs = billing.queryActiveSubscriptions()
        val active = subs.any { it.products.contains(BillingConfig.PRODUCT_ID_PREMIUM_MONTHLY) &&
                it.purchaseState == Purchase.PurchaseState.PURCHASED }
        dataStore.edit {
            it[PremiumPrefs.IS_PREMIUM] = active
            it[PremiumPrefs.LAST_SYNCED_EPOCH_MS] = System.currentTimeMillis()
        }
    }

    suspend fun setPremiumForDebug(enabled: Boolean) {
        dataStore.edit {
            it[PremiumPrefs.IS_PREMIUM] = enabled
            it[PremiumPrefs.LAST_SYNCED_EPOCH_MS] = System.currentTimeMillis()
        }
    }
}
