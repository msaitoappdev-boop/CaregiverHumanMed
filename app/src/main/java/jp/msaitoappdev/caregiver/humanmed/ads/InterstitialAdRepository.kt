package jp.msaitoappdev.caregiver.humanmed.ads

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ad_settings")

@Singleton
class InterstitialAdRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PrefKeys {
        val SHOWN_COUNT_THIS_SESSION = intPreferencesKey("interstitial_shown_count_this_session")
        val LAST_SHOWN_EPOCH_SEC = longPreferencesKey("interstitial_last_shown_epoch_sec")
    }

    val shownCountThisSession: Flow<Int> = context.dataStore.data.map {
        it[PrefKeys.SHOWN_COUNT_THIS_SESSION] ?: 0
    }

    val lastShownEpochSec: Flow<Long> = context.dataStore.data.map {
        it[PrefKeys.LAST_SHOWN_EPOCH_SEC] ?: 0L
    }

    suspend fun incrementShownCount() {
        context.dataStore.edit {
            val currentCount = it[PrefKeys.SHOWN_COUNT_THIS_SESSION] ?: 0
            it[PrefKeys.SHOWN_COUNT_THIS_SESSION] = currentCount + 1
        }
    }

    suspend fun updateLastShownTimestamp() {
        context.dataStore.edit {
            it[PrefKeys.LAST_SHOWN_EPOCH_SEC] = System.currentTimeMillis() / 1000
        }
    }

    // Note: You might want a way to reset the session count, 
    // for example, when the app is backgrounded for a long time.
    // This is not implemented here for simplicity.
}
