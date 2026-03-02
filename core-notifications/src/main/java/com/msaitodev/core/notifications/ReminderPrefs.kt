package com.msaitodev.core.notifications

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object ReminderPrefs {
    val ENABLED = booleanPreferencesKey("reminder_enabled")
    val HOUR = intPreferencesKey("reminder_hour")
    val MINUTE = intPreferencesKey("reminder_minute")

    // Default values
    const val DEFAULT_ENABLED = false
    const val DEFAULT_HOUR = 8
    const val DEFAULT_MINUTE = 0
}
