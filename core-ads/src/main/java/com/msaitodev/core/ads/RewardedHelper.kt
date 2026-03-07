package com.msaitodev.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.msaitodev.core.common.config.AdUnits
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class RewardedHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnits: AdUnits,
    private val repository: RewardedAdRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var ad: RewardedAd? = null
    private var isSideloading = false

    companion object {
        /** リワード広告の1日あたりの視聴上限数 */
        const val MAX_DAILY_QUOTA = 1
    }

    /**
     * 本日さらにリワード広告を視聴して報酬を獲得できるかどうか（カウントベース）。
     */
    val canShowToday: Flow<Boolean> = repository.countDaily.map { it < MAX_DAILY_QUOTA }

    fun preload() {
        if (ad != null || isSideloading) return
        if (!ConsentManager.canRequestAds(context)) return

        // SDK 初期化を確認
        AdsSdk.initIfNeeded(context)

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

    /**
     * リワード広告の表示を試みる。
     * 表示制限のチェックとカウント更新を内部で行う。
     * @param isPremium プレミアムユーザーかどうか。true の場合は表示しない。
     */
    suspend fun tryShow(
        activity: Activity,
        isPremium: Boolean,
        maxDailyQuota: Int = MAX_DAILY_QUOTA
    ): Boolean {
        if (isPremium) return false
        if (!ConsentManager.canRequestAds(activity)) return false

        // SDK 初期化を確認
        AdsSdk.initIfNeeded(activity)

        // 本日の獲得回数をチェック
        val rewardedCount = repository.countDaily.first()
        if (rewardedCount >= maxDailyQuota) return false

        val currentAd = ad
        if (currentAd == null) {
            preload()
            return false
        }

        return suspendCancellableCoroutine { cont ->
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    scope.launch {
                        repository.updateLastShownTimestamp()
                    }
                    ad = null
                    isSideloading = false
                    preload()
                    if (cont.isActive) cont.resume(true)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    ad = null
                    isSideloading = false
                    preload()
                    if (cont.isActive) cont.resume(false)
                }
            }

            try {
                currentAd.show(activity) {
                    // 報酬獲得確定時にカウントアップ
                    scope.launch {
                        repository.incrementCount()
                    }
                }
            } catch (t: Throwable) {
                ad = null
                isSideloading = false
                preload()
                if (cont.isActive) cont.resume(false)
            }
        }
    }

    /**
     * 既存コードとの互換性のための非推奨メソッド。
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
