package jp.msaitoappdev.caregiver.humanmed.core.premium

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val billing: BillingManager
) : PremiumRepository {

    private val TAG = "BugHunt-Premium"

    override val isPremiumFlow: Flow<Boolean> =
        dataStore.data.map { it[PremiumPrefs.IS_PREMIUM] ?: false }.distinctUntilChanged()

    override suspend fun refreshFromBilling() {
        Log.d(TAG, "PremiumRepositoryImpl.refreshFromBilling: Delegating to BillingManager")
        billing.refreshEntitlements()
    }

    override suspend fun savePremiumStatus(isPremium: Boolean) {
        dataStore.edit {
            it[PremiumPrefs.IS_PREMIUM] = isPremium
            it[PremiumPrefs.LAST_SYNCED_EPOCH_MS] = System.currentTimeMillis()
        }
    }

    override suspend fun setPremiumForDebug(enabled: Boolean) {
        savePremiumStatus(enabled)
    }
}
