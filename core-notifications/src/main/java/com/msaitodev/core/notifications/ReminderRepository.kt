package com.msaitodev.core.notifications

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "reminder_prefs")

data class ReminderConfig(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

/**
 * 通知リマインドの設定状態を管理するリポジトリ。
 */
interface ReminderRepository {
    val isReminderEnabled: Flow<Boolean>
    val reminderConfig: Flow<ReminderConfig>
    suspend fun updateReminder(enabled: Boolean, hour: Int = 8, minute: Int = 0)
}

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderRepository {

    override val isReminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ReminderPrefs.ENABLED] ?: ReminderPrefs.DEFAULT_ENABLED
    }

    override val reminderConfig: Flow<ReminderConfig> = context.dataStore.data.map { prefs ->
        ReminderConfig(
            enabled = prefs[ReminderPrefs.ENABLED] ?: ReminderPrefs.DEFAULT_ENABLED,
            hour = prefs[ReminderPrefs.HOUR] ?: ReminderPrefs.DEFAULT_HOUR,
            minute = prefs[ReminderPrefs.MINUTE] ?: ReminderPrefs.DEFAULT_MINUTE
        )
    }

    override suspend fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[ReminderPrefs.ENABLED] = enabled
            prefs[ReminderPrefs.HOUR] = hour
            prefs[ReminderPrefs.MINUTE] = minute
        }
        
        if (enabled) {
            ReminderScheduler.scheduleDaily(context, hour, minute)
        } else {
            ReminderScheduler.cancel(context)
        }
    }
}
