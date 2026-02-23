package com.msaitodev.quiz.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

class RewardedHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("rewarded_ad_id") private val adUnitId: String
) {
    /**
     * 同意取得状況に関わらず initIfNeeded は呼んでOK（複数回でも 1 回だけ初期化）。
     * 将来的には UMP で canRequestAds() = true になったタイミングの 1 回に寄せる想定。
     * @param canShowToday 日次上限チェック（例：1回/日）
     */
    fun show(
        activity: Activity,
        canShowToday: () -> Boolean,
        onEarned: (RewardItem) -> Unit,
        onFail: () -> Unit
    ) {
        // 0) 日次上限
        if (!canShowToday()) {
            onFail()
            return
        }

        // 1) 広告 SDK 初期化（必要なら一度だけ）
        AdsSdk.initIfNeeded(context)

        // 2) AdUnit ID はコンストラクタ経由で注入される

        // 3) リクエスト作成
        val request = AdRequest.Builder().build()

        // 4) ロード
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
                        override fun onAdShowedFullScreenContent() {}
                        override fun onAdDismissedFullScreenContent() {}
                    }
                    ad.show(activity) { reward: RewardItem ->
                        onEarned(reward)
                    }
                }
            }
        )
    }
}
