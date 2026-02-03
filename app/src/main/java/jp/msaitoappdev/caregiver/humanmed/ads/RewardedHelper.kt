// app/src/main/java/jp/msaitoappdev/caregiver/humanmed/ads/RewardedHelper.kt
package jp.msaitoappdev.caregiver.humanmed.ads

import android.app.Activity
import android.util.Log
import jp.msaitoappdev.caregiver.humanmed.BuildConfig
import jp.msaitoappdev.caregiver.humanmed.R
import com.google.android.gms.ads.*

import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedHelper {
    private const val TAG = "RewardedHelper"

    /**
     * 同意取得状況に関わらず initialize は呼んでOK（複数回安全）。完了待ちは不要。
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
            Log.d(TAG, "Abort: canShowToday=false (daily cap reached)")
            onFail()
            return
        }

        // 1) 念のため常に initialize（非同期・複数回OK）
        try {
            MobileAds.initialize(activity.applicationContext) { status ->
                Log.d(TAG, "MobileAds.initialize() done: ${status.adapterStatusMap.keys}")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "MobileAds.initialize() threw: ${t.message}")
        }

        // 2) AdUnit ID
        val adUnitId = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/5224354917" // Google のテスト用 Rewarded
        else
            activity.getString(R.string.ad_unit_rewarded_weaktrain_plus1)

        // 3) リクエストを作成（テスト端末IDを設定したい場合は RequestConfiguration を別途設定）
        val request = AdRequest.Builder().build()

        // 4) ロード
        RewardedAd.load(
            activity,
            adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "onAdFailedToLoad: code=${error.code}, msg=${error.message}, domain=${error.domain}")
                    onFail()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "onAdLoaded: ready to show")
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.w(TAG, "onAdFailedToShow: code=${adError.code}, msg=${adError.message}")
                            onFail()
                        }
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "onAdShowedFullScreenContent")
                        }
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "onAdDismissedFullScreenContent")
                        }
                    }
                    ad.show(activity) { reward: RewardItem ->
                        Log.d(TAG, "onUserEarnedReward: type=${reward.type}, amount=${reward.amount}")
                        onEarned(reward)
                    }
                }
            }
        )
    }
}