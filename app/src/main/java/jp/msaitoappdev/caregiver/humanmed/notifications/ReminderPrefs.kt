package jp.msaitoappdev.caregiver.humanmed.notifications

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object ReminderPrefs {
    val ENABLED = booleanPreferencesKey("reminder_enabled")
    val HOUR = intPreferencesKey("reminder_hour")
    val MINUTE = intPreferencesKey("reminder_minute")
}
