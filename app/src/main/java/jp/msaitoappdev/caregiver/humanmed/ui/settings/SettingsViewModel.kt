package jp.msaitoappdev.caregiver.humanmed.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.lifecycle.ViewModel
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderPrefs
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderScheduler
import kotlinx.coroutines.flow.firstOrNull

import jp.msaitoappdev.caregiver.humanmed.core.premium.PremiumRepository
import kotlinx.coroutines.flow.first

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    val settings: Flow<ReminderSettings> = dataStore.data.map { pref ->
        ReminderSettings(
            enabled = pref[ReminderPrefs.ENABLED] ?: false,
            hour = pref[ReminderPrefs.HOUR] ?: 20,
            minute = pref[ReminderPrefs.MINUTE] ?: 0
        )
    }

    // 現在のプレミアム状態をそのまま出す（UIでバッジ表示）
    val isPremium: Flow<Boolean> = premiumRepository.isPremiumFlow

    suspend fun setEnabled(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        dataStore.edit {
            it[ReminderPrefs.ENABLED] = enabled
            if (enabled) {
                it[ReminderPrefs.HOUR] = hour
                it[ReminderPrefs.MINUTE] = minute
            }
        }
        if (enabled) {
            ReminderScheduler.scheduleDaily(context, hour, minute)
        } else {
            ReminderScheduler.cancel(context)
        }
    }

    suspend fun setTime(context: Context, hour: Int, minute: Int) {
        dataStore.edit {
            it[ReminderPrefs.HOUR] = hour
            it[ReminderPrefs.MINUTE] = minute
        }
        // ON のときのみ再スケジュール
        val enabled = dataStore.data.map { it[ReminderPrefs.ENABLED] ?: false }.first()
        if (enabled) {
            ReminderScheduler.scheduleDaily(context, hour, minute)
        }
    }

    /** 手動「購入を復元」 */
    suspend fun restorePurchases() {
        premiumRepository.refreshFromBilling()
    }
}
