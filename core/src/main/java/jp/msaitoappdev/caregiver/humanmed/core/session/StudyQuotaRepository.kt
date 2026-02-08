package jp.msaitoappdev.caregiver.humanmed.core.session

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import jp.msaitoappdev.caregiver.humanmed.core.session.StudyQuotaPrefs as P
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class QuotaState(
    val todayKey: String,
    val usedSets: Int,
    val rewardedGranted: Int,
    val freeDailySets: Int
) {
    val totalAllowance: Int get() = freeDailySets + rewardedGranted
    val canStart: Boolean get() = usedSets < totalAllowance
}

@Singleton
class StudyQuotaRepository @Inject constructor(
    private val store: DataStore<Preferences>
) {
    private fun todayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    fun observe(freeDailySetsProvider: () -> Int): Flow<QuotaState> =
        store.data.map { pref ->
            val tk = pref[P.TODAY_KEY] ?: todayKey()
            val used = if (tk == todayKey()) pref[P.USED_SETS] ?: 0 else 0
            val granted = if (tk == todayKey()) pref[P.REWARDED_GRANTED] ?: 0 else 0
            QuotaState(
                todayKey = todayKey(),
                usedSets = used,
                rewardedGranted = granted,
                freeDailySets = freeDailySetsProvider()
            )
        }

    private suspend fun ensureToday() {
        val today = todayKey()
        store.edit { p ->
            val tk = p[P.TODAY_KEY]
            if (tk != today) {
                p[P.TODAY_KEY] = today
                p[P.USED_SETS] = 0
                p[P.REWARDED_GRANTED] = 0
                p[P.LAST_UPDATED_MS] = System.currentTimeMillis()
            }
        }
    }

    suspend fun markSetFinished() {
        ensureToday()
        store.edit { p ->
            val currentUsed = p[P.USED_SETS] ?: 0
            val nextUsed = currentUsed + 1
            p[P.USED_SETS] = nextUsed
            p[P.LAST_UPDATED_MS] = System.currentTimeMillis()
            Log.d("BugHunt-Quota", "markSetFinished: usedSets updated to $nextUsed")
        }
    }

    suspend fun grantByReward() {
        ensureToday()
        store.edit { p ->
            p[P.REWARDED_GRANTED] = (p[P.REWARDED_GRANTED] ?: 0) + 1
            p[P.LAST_UPDATED_MS] = System.currentTimeMillis()
        }
    }
}
