package com.msaitodev.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.core.notifications.ReminderPrefs
import com.msaitodev.core.common.billing.BillingManager
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
    val isPremium: Boolean = false,
    val isWeaknessMode: Boolean = false,
    val weaknessCategoryName: String? = null,
    val weaknessModeTitle: String = "",
    val weaknessModeDescription: String = ""
)

sealed interface SettingsEvent {
    /** 復元処理の結果ステータス */
    enum class RestoreStatus { SUCCESS, NO_PURCHASE, ERROR }
    data class RestoreResult(val status: RestoreStatus) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val dataStore: DataStore<Preferences>,
    private val settingsProvider: SettingsProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            weaknessModeTitle = settingsProvider.weaknessModeTitle,
            weaknessModeDescription = settingsProvider.weaknessModeDescription
        )
    )
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
                billingManager.isPremium,
                settingsProvider.isWeaknessMode,
                settingsProvider.weaknessCategoryName
            ) { prefs, isPremium, isWeaknessMode, weaknessCategoryName ->
                SettingsUiState(
                    reminderEnabled = prefs[ReminderPrefs.ENABLED] ?: ReminderPrefs.DEFAULT_ENABLED,
                    hour = prefs[ReminderPrefs.HOUR] ?: ReminderPrefs.DEFAULT_HOUR,
                    minute = prefs[ReminderPrefs.MINUTE] ?: ReminderPrefs.DEFAULT_MINUTE,
                    isPremium = isPremium,
                    isWeaknessMode = isWeaknessMode,
                    weaknessCategoryName = weaknessCategoryName,
                    weaknessModeTitle = settingsProvider.weaknessModeTitle,
                    weaknessModeDescription = settingsProvider.weaknessModeDescription
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

    /** 弱点特訓モードの設定を更新 */
    fun setWeaknessModeEnabled(enabled: Boolean, categoryId: String? = null) {
        viewModelScope.launch {
            settingsProvider.updateWeaknessMode(enabled, categoryId)
        }
    }

    /**
     * 購入情報の復元を試行します。
     */
    fun restorePurchases() {
        viewModelScope.launch {
            try {
                val hasPremium = billingManager.refreshEntitlements()
                val status = if (hasPremium) {
                    SettingsEvent.RestoreStatus.SUCCESS
                } else {
                    SettingsEvent.RestoreStatus.NO_PURCHASE
                }
                _events.emit(SettingsEvent.RestoreResult(status))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.RestoreResult(SettingsEvent.RestoreStatus.ERROR))
            }
        }
    }
}
