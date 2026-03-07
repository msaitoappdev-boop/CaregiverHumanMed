package com.msaitodev.core.common.billing

/**
 * 課金機能で使用する共通の定数。
 * プロダクトIDなどのアプリ固有値はここには含めず、BillingProvider経由で提供する。
 */
object BillingConfig {
    // 端末ローカルの保存キー（これは全アプリ共通で良い）
    const val PREFS_NAME = "billing_entitlements"
    const val KEY_IS_PREMIUM = "is_premium"
    const val KEY_LAST_REFRESH_EPOCH_MS = "last_refresh_epoch_ms"
}