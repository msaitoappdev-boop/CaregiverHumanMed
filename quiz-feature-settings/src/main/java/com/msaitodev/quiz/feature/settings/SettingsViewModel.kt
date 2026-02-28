package com.msaitodev.quiz.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.notifications.ReminderPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val reminderEnabled: Boolean,
    val hour: Int,
    val minute: Int,
    val isPremium: Boolean
)

sealed interface SettingsEvent {
    data class RestoreResult(val messageResId: Int) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val premiumRepo: PremiumRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        dataStore.data.map { pref ->
            ReminderSettings(
                enabled = pref[ReminderPrefs.ENABLED] ?: ReminderPrefs.DEFAULT_ENABLED,
                hour = pref[ReminderPrefs.HOUR] ?: ReminderPrefs.DEFAULT_HOUR,
                minute = pref[ReminderPrefs.MINUTE] ?: ReminderPrefs.DEFAULT_MINUTE
            )
        },
        premiumRepo.isPremium
    ) { reminder, isPremium ->
        SettingsUiState(
            reminderEnabled = reminder.enabled,
            hour = reminder.hour,
            minute = reminder.minute,
            isPremium = isPremium
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(
            reminderEnabled = ReminderPrefs.DEFAULT_ENABLED,
            hour = ReminderPrefs.DEFAULT_HOUR,
            minute = ReminderPrefs.DEFAULT_MINUTE,
            isPremium = false
        )
    )

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit {
                it[ReminderPrefs.ENABLED] = enabled
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[ReminderPrefs.HOUR] = hour
                it[ReminderPrefs.MINUTE] = minute
            }
        }
    }

    /** 「購入を復元」押下時に、Play の状態をローカルへ同期 */
    fun restorePurchases() {
        viewModelScope.launch {
            try {
                val wasPremium = premiumRepo.isPremium.value
                premiumRepo.refreshFromBilling()
                val isPremium = premiumRepo.isPremium.value

                val messageRes = if (isPremium) {
                    R.string.settings_restore_success
                } else if (wasPremium) {
                    // 以前はプレミアムだったが、復元した結果プレミアムでなくなった場合
                    R.string.settings_restore_no_purchase
                } else {
                    R.string.settings_restore_no_purchase
                }
                _events.emit(SettingsEvent.RestoreResult(messageRes))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.RestoreResult(R.string.settings_restore_error))
            }
        }
    }
}

/** 旧 ReminderSettings クラスを内部データ構造として再利用 */
private data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)
