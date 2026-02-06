package jp.msaitoappdev.caregiver.humanmed.config

import jp.msaitoappdev.caregiver.humanmed.R
import android.content.Context

object AdUnits {
    fun interstitialWeaktrainComplete(ctx: Context): String =
        ctx.getString(R.string.ad_unit_interstitial_weaktrain_complete)

    fun rewardedWeaktrainPlusOne(ctx: Context): String =
        ctx.getString(R.string.ad_unit_rewarded_weaktrain_plus1)
}
