package com.msaitodev.core.ads

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
import javax.inject.Singleton
import com.msaitodev.core.common.config.AdUnits
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class RewardedHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnits: AdUnits
) {
    private var ad: RewardedAd? = null
    private var isSideloading = false

    fun preload() {
        if (ad != null || isSideloading) return
        if (!ConsentManager.canRequestAds(context)) return

        isSideloading = true
        val req = AdRequest.Builder().build()
        val unitId = adUnits.rewardedUnitA

        RewardedAd.load(context, unitId, req, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(p0: RewardedAd) {
                ad = p0
                isSideloading = false
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                ad = null
                isSideloading = false
            }
        })
    }

    suspend fun tryShow(
        activity: Activity,
        canShowToday: Boolean
    ): Boolean {
        // UMP 同意がない場合は即座に失敗
        if (!ConsentManager.canRequestAds(activity)) {
            return false
        }

        // 外部（リポジトリ等）からの制限フラグチェック
        if (!canShowToday) {
            return false
        }

        val currentAd = ad
        if (currentAd == null) {
            preload()
            return false
        }

        return suspendCancellableCoroutine { cont ->
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    ad = null
                    preload()
                    if (cont.isActive) cont.resume(true)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    ad = null
                    preload()
                    if (cont.isActive) cont.resume(false)
                }
            }

            try {
                currentAd.show(activity) {
                    // 報酬獲得イベントは FullScreenContentCallback では取れないため、
                    // 本来はここで onEarned を呼ぶべきだが、
                    // インターフェースを InterstitialHelper に寄せるため、
                    // Dismiss 時に true を返すことで「視聴完了」とみなす運用とする。
                    // (AdMob の標準的な動作では、show の lambda が呼ばれた = 報酬確定)
                }
            } catch (t: Throwable) {
                ad = null
                preload()
                if (cont.isActive) cont.resume(false)
            }
        }
    }

    /**
     * 既存コードとの互換性のための非推奨メソッド。
     * 将来的には tryShow への移行を推奨。
     */
    @Deprecated("Use tryShow instead")
    fun show(
        activity: Activity,
        canShowToday: () -> Boolean,
        onEarned: () -> Unit,
        onFail: () -> Unit
    ) {
        if (!canShowToday() || !ConsentManager.canRequestAds(activity)) {
            onFail()
            return
        }

        AdsSdk.initIfNeeded(activity)
        val request = AdRequest.Builder().build()

        RewardedAd.load(activity, adUnits.rewardedUnitA, request, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                onFail()
            }

            override fun onAdLoaded(adLoaded: RewardedAd) {
                adLoaded.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        onFail()
                    }
                }
                adLoaded.show(activity) {
                    onEarned()
                }
            }
        })
    }
}
