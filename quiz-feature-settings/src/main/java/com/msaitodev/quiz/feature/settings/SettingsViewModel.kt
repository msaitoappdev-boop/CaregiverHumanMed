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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val premiumRepo: PremiumRepository
) : ViewModel() {

    val settings: StateFlow<ReminderSettings> = dataStore.data.map { pref ->
        ReminderSettings(
            enabled = pref[ReminderPrefs.ENABLED] ?: false,
            hour = pref[ReminderPrefs.HOUR] ?: 20,
            minute = pref[ReminderPrefs.MINUTE] ?: 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReminderSettings(enabled = false, hour = 20, minute = 0)
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
            premiumRepo.refreshFromBilling()
        }
    }
}
