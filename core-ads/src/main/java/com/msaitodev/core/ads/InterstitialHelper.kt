package com.msaitodev.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
class InterstitialHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnits: AdUnits,
    private val repository: InterstitialAdRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var ad: InterstitialAd? = null
    private var isSideloading = false

    companion object {
        /** インタースティシャル広告の1日あたりの表示上限数 */
        const val MAX_DAILY_QUOTA = 1
    }

    /**
     * 本日さらにインタースティシャル広告を表示できるかどうか（カウントベース）。
     */
    val canShowToday: Flow<Boolean> = repository.countDaily.map { it < MAX_DAILY_QUOTA }

    fun preload() {
        if (ad != null || isSideloading) return
        if (!ConsentManager.canRequestAds(context)) return

        // SDK 初期化を確認
        AdsSdk.initIfNeeded(context)

        isSideloading = true
        val req = AdRequest.Builder().build()
        val unitId = adUnits.interstitialUnitA
        InterstitialAd.load(context, unitId, req, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
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
     * インタースティシャル広告の表示を試みる。
     * 表示制限のチェックとカウント更新を内部で行う。
     * @param isPremium プレミアムユーザーかどうか。true の場合は表示しない。
     */
    suspend fun tryShow(
        activity: Activity,
        isPremium: Boolean,
        enabled: Boolean = true
    ): Boolean {
        if (isPremium) return false
        if (!ConsentManager.canRequestAds(activity)) return false
        if (!enabled) return false

        // SDK 初期化を確認
        AdsSdk.initIfNeeded(activity)

        // 本日の表示回数をチェック
        val shownCount = repository.countDaily.first()
        if (shownCount >= MAX_DAILY_QUOTA) return false

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

                override fun onAdShowedFullScreenContent() {
                    scope.launch {
                        repository.incrementCount()
                    }
                }
            }
            try {
                currentAd.show(activity)
            } catch (t: Throwable) {
                ad = null
                isSideloading = false
                preload()
                if (cont.isActive) cont.resume(false)
            }
        }
    }
}
