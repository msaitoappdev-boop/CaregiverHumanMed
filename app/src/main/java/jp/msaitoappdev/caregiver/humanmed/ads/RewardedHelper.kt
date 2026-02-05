package jp.msaitoappdev.caregiver.humanmed.ads

import android.app.Activity
import android.util.Log
import jp.msaitoappdev.caregiver.humanmed.BuildConfig
import jp.msaitoappdev.caregiver.humanmed.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedHelper {
    private const val TAG = "RewardedHelper"

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
            Log.d(TAG, "Abort: canShowToday=false (daily cap reached)")
            onFail()
            return
        }

        // 1) 広告 SDK 初期化（必要なら一度だけ）
        AdsSdk.initIfNeeded(activity.applicationContext)

        // 2) AdUnit ID（DEBUG はデモID、RELEASE は strings.xml の値）
        val adUnitId = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/5224354917" // Google のテスト用 Rewarded（安全）
        else
            activity.getString(R.string.ad_unit_rewarded_weaktrain_plus1)

        // 3) リクエスト作成
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
