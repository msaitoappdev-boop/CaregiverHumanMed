// app/src/main/java/jp/msaitoappdev/caregiver/humanmed/ads/RewardedHelper.kt
package jp.msaitoappdev.caregiver.humanmed.ads

import android.app.Activity
import jp.msaitoappdev.caregiver.humanmed.BuildConfig

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedHelper {

    /**
     * 同意取得後（かつ MobileAds.initialize 完了後）に呼ぶことを前提。
     * @param canShowToday 今日の付与上限（例：1回/日）に達していないかを事前チェック
     */
    fun show(
        activity: Activity,
        canShowToday: () -> Boolean,
        onEarned: (RewardItem) -> Unit,
        onFail: () -> Unit
    ) {
        // 1) MobileAds 初期化の完了を確認
        val ready = MobileAds.getInitializationStatus()
            ?.adapterStatusMap
            ?.values
            ?.all { it.initializationState == AdapterStatus.State.READY }
            ?: false
        if (!ready) { onFail(); return }

        // 2) 日次上限の事前チェック
        if (!canShowToday()) { onFail(); return }

        // 3) Rewarded のロード＆表示
        val adUnitId = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/5224354917" // Google のテスト用 Rewarded
        else
            "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"   // TODO: 本番IDを設定

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    onFail()
                }
                override fun onAdLoaded(ad: RewardedAd) {
                    ad.show(activity) { reward: RewardItem ->
                        onEarned(reward)
                    }
                }
            }
        )
    }
}