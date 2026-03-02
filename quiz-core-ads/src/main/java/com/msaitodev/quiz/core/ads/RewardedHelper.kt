package com.msaitodev.quiz.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

class RewardedHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named(AdModule.NAME_REWARDED_AD_ID) private val adUnitId: String
) {
    /**
     * リワード広告を表示する。
     * 報酬獲得時に引数なしの [onEarned] を呼び出すことで、AdMob への直接依存を隠蔽する。
     */
    fun show(
        activity: Activity,
        canShowToday: () -> Boolean,
        onEarned: () -> Unit,
        onFail: () -> Unit
    ) {
        if (!canShowToday()) {
            onFail()
            return
        }

        AdsSdk.initIfNeeded(context)

        val request = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    onFail()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            onFail()
                        }
                    }
                    ad.show(activity) {
                        onEarned()
                    }
                }
            }
        )
    }
}
