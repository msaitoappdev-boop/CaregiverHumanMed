package com.msaitodev.quiz.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.msaitodev.quiz.core.domain.model.QuotaState
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
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

    private object PrefKeys {
        val USED_SETS = intPreferencesKey("study_quota_used_sets")
        val TODAY_KEY = stringPreferencesKey("study_quota_today_key")
        val LAST_UPDATED_MS = longPreferencesKey("study_quota_last_updated_ms")
    }

    private fun todayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    override fun observe(freeDailySetsProvider: () -> Int): Flow<QuotaState> =
        store.data.map { pref ->
            val tk = pref[PrefKeys.TODAY_KEY] ?: todayKey()
            val used = if (tk == todayKey()) pref[PrefKeys.USED_SETS] ?: 0 else 0
            QuotaState(
                todayKey = todayKey(),
                usedSets = used,
                freeDailySets = freeDailySetsProvider()
            )
        }

    override suspend fun markSetFinished() {
        val today = todayKey()
        store.edit { p ->
            val tk = p[PrefKeys.TODAY_KEY]
            var currentUsed = p[PrefKeys.USED_SETS] ?: 0

            if (tk != today) {
                p[PrefKeys.TODAY_KEY] = today
                currentUsed = 0 // Reset for the new day
            }

            p[PrefKeys.USED_SETS] = currentUsed + 1
            p[PrefKeys.LAST_UPDATED_MS] = System.currentTimeMillis()
        }
    }
}
