// core/premium/PremiumPrefs.kt
package jp.msaitoappdev.caregiver.humanmed.core.premium

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

object PremiumPrefs {
    val IS_PREMIUM = booleanPreferencesKey("is_premium")
    val LAST_SYNCED_EPOCH_MS = longPreferencesKey("last_synced_epoch_ms")
}
