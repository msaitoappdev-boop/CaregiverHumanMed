package jp.msaitoappdev.caregiver.humanmed.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import jp.msaitoappdev.caregiver.humanmed.data.local.datastore.StudyQuotaPrefs
import jp.msaitoappdev.caregiver.humanmed.domain.model.QuotaState
import jp.msaitoappdev.caregiver.humanmed.domain.repository.StudyQuotaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyQuotaRepositoryImpl @Inject constructor(
    private val store: DataStore<Preferences>
) : StudyQuotaRepository {
    private fun todayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    override fun observe(freeDailySetsProvider: () -> Int): Flow<QuotaState> =
        store.data.map { pref ->
            val tk = pref[StudyQuotaPrefs.TODAY_KEY] ?: todayKey()
            val used = if (tk == todayKey()) pref[StudyQuotaPrefs.USED_SETS] ?: 0 else 0
            val granted = if (tk == todayKey()) pref[StudyQuotaPrefs.REWARDED_GRANTED] ?: 0 else 0
            QuotaState(
                todayKey = todayKey(),
                usedSets = used,
                rewardedGranted = granted,
                freeDailySets = freeDailySetsProvider()
            )
        }

    override suspend fun markSetFinished() {
        val today = todayKey()
        store.edit { p ->
            val tk = p[StudyQuotaPrefs.TODAY_KEY]
            var currentUsed = p[StudyQuotaPrefs.USED_SETS] ?: 0

            if (tk != today) {
                p[StudyQuotaPrefs.TODAY_KEY] = today
                p[StudyQuotaPrefs.REWARDED_GRANTED] = 0
                currentUsed = 0 // Reset used count for the new day
            }

            p[StudyQuotaPrefs.USED_SETS] = currentUsed + 1
            p[StudyQuotaPrefs.LAST_UPDATED_MS] = System.currentTimeMillis()
        }
    }

    override suspend fun grantByReward() {
        val today = todayKey()
        store.edit { p ->
            val tk = p[StudyQuotaPrefs.TODAY_KEY]
            var currentGranted = p[StudyQuotaPrefs.REWARDED_GRANTED] ?: 0

            if (tk != today) {
                p[StudyQuotaPrefs.TODAY_KEY] = today
                p[StudyQuotaPrefs.USED_SETS] = 0
                currentGranted = 0 // Reset granted count for the new day
            }

            p[StudyQuotaPrefs.REWARDED_GRANTED] = currentGranted + 1
            p[StudyQuotaPrefs.LAST_UPDATED_MS] = System.currentTimeMillis()
        }
    }
}
