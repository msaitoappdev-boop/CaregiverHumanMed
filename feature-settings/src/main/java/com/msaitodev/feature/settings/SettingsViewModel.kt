package com.msaitodev.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.core.notifications.ReminderPrefs
import com.msaitodev.quiz.core.common.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val reminderEnabled: Boolean = ReminderPrefs.DEFAULT_ENABLED,
    val hour: Int = ReminderPrefs.DEFAULT_HOUR,
    val minute: Int = ReminderPrefs.DEFAULT_MINUTE,
    val isPremium: Boolean = false
)

sealed interface SettingsEvent {
    data class RestoreResult(val isSuccess: Boolean) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val dataStore: DataStore<Preferences>,
    private val settingsProvider: SettingsProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    /** プライバシーポリシーのURLを取得 */
    val privacyPolicyUrl: String get() = settingsProvider.privacyPolicyUrl

    /** 定期購入管理のURLを取得 */
    val subscriptionManagementUrl: String get() = settingsProvider.subscriptionManagementUrl

    init {
        viewModelScope.launch {
            combine(
                dataStore.data,
                billingManager.isPremium
            ) { prefs, isPremium ->
                SettingsUiState(
                    reminderEnabled = prefs[ReminderPrefs.ENABLED] ?: ReminderPrefs.DEFAULT_ENABLED,
                    hour = prefs[ReminderPrefs.HOUR] ?: ReminderPrefs.DEFAULT_HOUR,
                    minute = prefs[ReminderPrefs.MINUTE] ?: ReminderPrefs.DEFAULT_MINUTE,
                    isPremium = isPremium
                )
            }.collectLatest {
                _uiState.value = it
            }
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[ReminderPrefs.ENABLED] = enabled }
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

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                billingManager.refreshEntitlements()
                _events.emit(SettingsEvent.RestoreResult(true))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.RestoreResult(false))
            }
        }
    }
}
