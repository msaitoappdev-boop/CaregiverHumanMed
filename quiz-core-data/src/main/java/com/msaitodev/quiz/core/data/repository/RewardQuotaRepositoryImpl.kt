package com.msaitodev.quiz.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.msaitodev.quiz.core.domain.repository.RewardQuotaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardQuotaRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : RewardQuotaRepository {
    private object Keys {
        val LAST_DAY = longPreferencesKey("reward_last_day")
        val COUNT    = intPreferencesKey("reward_count_today")
    }

    // API level 24でも安全な方法で「今日」のキーを生成する
    private fun getTodayKeyAsLong(): Long {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()).toLong()
    }

    override fun grantedCountTodayFlow(): Flow<Int> = dataStore.data.map { pref ->
        val today = getTodayKeyAsLong()
        val last  = pref[Keys.LAST_DAY] ?: -1L
        val count = pref[Keys.COUNT] ?: 0
        if (last == today) count else 0
    }

    /**
     * 今日の付与回数が上限(=1)に達していなければ +1 して true を返す。
     * 既に上限達成なら false。
     */
    override suspend fun tryGrantOncePerDay(): Boolean {
        val today = getTodayKeyAsLong()
        var granted = false
        dataStore.edit { mutablePrefs -> // 引数は既にMutable
            val last  = mutablePrefs[Keys.LAST_DAY] ?: -1L
            val count = if (last == today) (mutablePrefs[Keys.COUNT] ?: 0) else 0
            if (count < 1) {
                granted = true
                mutablePrefs[Keys.LAST_DAY] = today
                mutablePrefs[Keys.COUNT] = count + 1
            }
        }
        return granted
    }
}
