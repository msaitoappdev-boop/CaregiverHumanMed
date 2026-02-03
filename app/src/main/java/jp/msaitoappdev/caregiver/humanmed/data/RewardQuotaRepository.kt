package jp.msaitoappdev.caregiver.humanmed.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardQuotaRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val LAST_DAY = longPreferencesKey("reward_last_day")
        val COUNT    = intPreferencesKey("reward_count_today")
    }

    fun grantedCountTodayFlow() = dataStore.data.map { pref ->
        val today = LocalDate.now().toEpochDay()
        val last  = pref[Keys.LAST_DAY] ?: -1L
        val count = pref[Keys.COUNT] ?: 0
        if (last == today) count else 0
    }

    /**
     * 今日の付与回数が上限(=1)に達していなければ +1 して true を返す。
     * 既に上限達成なら false。
     */
    suspend fun tryGrantOncePerDay(): Boolean {
        val today = LocalDate.now().toEpochDay()
        var granted = false
        dataStore.updateData { pref ->
            val last  = pref[Keys.LAST_DAY] ?: -1L
            val count = if (last == today) (pref[Keys.COUNT] ?: 0) else 0
            if (count < 1) {
                granted = true
                pref.toMutablePreferences().apply {
                    this[Keys.LAST_DAY] = today
                    this[Keys.COUNT] = count + 1
                }
            } else {
                pref
            }
        }
        return granted
    }
}