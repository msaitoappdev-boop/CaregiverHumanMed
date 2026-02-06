package jp.msaitoappdev.caregiver.humanmed.core.session

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object StudyQuotaPrefs {
    // yyyymmdd 文字列で当日を識別
    val TODAY_KEY = stringPreferencesKey("quota_today_key")
    val USED_SETS = intPreferencesKey("quota_used_sets")
    val REWARDED_GRANTED = intPreferencesKey("quota_rewarded_granted")
    val LAST_UPDATED_MS = longPreferencesKey("quota_last_updated_ms")
}
