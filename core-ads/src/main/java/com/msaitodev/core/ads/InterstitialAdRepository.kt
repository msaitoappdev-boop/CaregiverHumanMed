package com.msaitodev.core.ads

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

private const val PREFS_NAME = "ad_settings"
private const val KEY_SHOWN_COUNT = "interstitial_shown_count_this_session"
private const val KEY_LAST_SHOWN = "interstitial_last_shown_epoch_sec"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

@Singleton
class InterstitialAdRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PrefKeys {
        val SHOWN_COUNT_THIS_SESSION = intPreferencesKey(KEY_SHOWN_COUNT)
        val LAST_SHOWN_EPOCH_SEC = longPreferencesKey(KEY_LAST_SHOWN)
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
}
