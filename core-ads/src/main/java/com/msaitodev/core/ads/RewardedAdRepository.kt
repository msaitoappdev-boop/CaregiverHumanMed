package com.msaitodev.core.ads

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * リワード広告専用のリポジトリインターフェース。
 */
interface RewardedAdRepository : BaseAdRepository

/**
 * リワード広告の報酬獲得制限（1日の上限、獲得間隔）を管理するリポジトリ。
 */
@Singleton
class RewardedAdRepositoryImpl @Inject constructor(
    @Named(AdModule.QUALIFIER_AD_DATASTORE) private val dataStore: DataStore<Preferences>
) : RewardedAdRepository {

    private object PrefKeys {
        val COUNT_DAILY = intPreferencesKey("rewarded_count_daily")
        val LAST_SHOWN = longPreferencesKey("rewarded_last_shown_epoch_sec")
        val TODAY_KEY = stringPreferencesKey("rewarded_today_key")
    }

    private fun todayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    override val countDaily: Flow<Int> = dataStore.data.map { p ->
        val tk = p[PrefKeys.TODAY_KEY] ?: todayKey()
        if (tk == todayKey()) p[PrefKeys.COUNT_DAILY] ?: 0 else 0
    }

    override val lastShownEpochSec: Flow<Long> = dataStore.data.map {
        it[PrefKeys.LAST_SHOWN] ?: 0L
    }

    override suspend fun incrementCount() {
        val today = todayKey()
        dataStore.edit { p ->
            val tk = p[PrefKeys.TODAY_KEY]
            var current = p[PrefKeys.COUNT_DAILY] ?: 0

            if (tk != today) {
                p[PrefKeys.TODAY_KEY] = today
                current = 0
            }

            p[PrefKeys.COUNT_DAILY] = current + 1
        }
    }

    override suspend fun updateLastShownTimestamp() {
        dataStore.edit {
            it[PrefKeys.LAST_SHOWN] = System.currentTimeMillis() / 1000
        }
    }
}
