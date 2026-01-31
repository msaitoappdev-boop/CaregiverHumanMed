package jp.msaitoappdev.caregiver.humanmed.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import jp.msaitoappdev.caregiver.humanmed.config.AdUnits

object RewardedHelper {
    private const val TAG = "RewardedHelper"
    private var ad: RewardedAd? = null

    fun preload(context: Context) {
        if (ad != null) return
        val unitId = AdUnits.rewardedWeaktrainPlusOne(context)
        RewardedAd.load(context, unitId, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(p0: RewardedAd) {
                    ad = p0
                    ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() { ad = null; preload(context) }
                        override fun onAdFailedToShowFullScreenContent(p0: AdError) { ad = null }
                    }
                    Log.d(TAG, "Rewarded loaded")
                }
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    ad = null
                    Log.w(TAG, "Rewarded load failed: ${p0.message}")
                }
            })
    }

    fun show(activity: Activity, onEarned: (RewardItem) -> Unit, onFail: () -> Unit) {
        val current = ad ?: return onFail()
        current.show(activity) { reward ->
            onEarned(reward) // ここで＋1セットを付与
        }
    }
}
