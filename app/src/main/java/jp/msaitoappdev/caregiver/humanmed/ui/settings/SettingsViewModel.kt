package jp.msaitoappdev.caregiver.humanmed.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.lifecycle.ViewModel
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderPrefs
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderScheduler

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    val settings: Flow<ReminderSettings> = dataStore.data.map { pref ->
        ReminderSettings(
            enabled = pref[ReminderPrefs.ENABLED] ?: false,
            hour = pref[ReminderPrefs.HOUR] ?: 20,
            minute = pref[ReminderPrefs.MINUTE] ?: 0
        )
    }
    suspend fun setEnabled(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        dataStore.edit {
            it[ReminderPrefs.ENABLED] = enabled
            if (enabled) {
                it[ReminderPrefs.HOUR] = hour
                it[ReminderPrefs.MINUTE] = minute
            }
        }
        if (enabled) ReminderScheduler.scheduleDaily(context, hour, minute)
        else ReminderScheduler.cancel(context)
    }
    suspend fun setTime(context: Context, hour: Int, minute: Int) {
        dataStore.edit {
            it[ReminderPrefs.HOUR] = hour
            it[ReminderPrefs.MINUTE] = minute
        }
        val enabled = dataStore.data.map { it[ReminderPrefs.ENABLED] ?: false }.first()
        if (enabled) ReminderScheduler.scheduleDaily(context, hour, minute)
    }
    // PR④の「購入を復元」用スタブ（後続で実装）
    suspend fun restorePurchases() {}
}
