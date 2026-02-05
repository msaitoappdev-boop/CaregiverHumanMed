package jp.msaitoappdev.caregiver.humanmed.core.billing

/**
 * Play Console と 100% 一致させること（唯一の真実源）。
 * Product ID: caregiver_humanmed_premium_monthly
 * Base plan:  monthly
 * Offer:      monthly-default
 */
object BillingConfig {
    const val PRODUCT_ID_PREMIUM_MONTHLY = "caregiver_humanmed_premium_monthly"
    const val BASE_PLAN_ID_MONTHLY = "monthly"
    const val OFFER_ID_DEFAULT = "monthly-default"

    // 端末ローカルの保存キー
    const val PREFS_NAME = "billing_entitlements"
    const val KEY_IS_PREMIUM = "is_premium"
    const val KEY_LAST_REFRESH_EPOCH_MS = "last_refresh_epoch_ms"
}