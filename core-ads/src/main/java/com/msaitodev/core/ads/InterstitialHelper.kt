package com.msaitodev.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.msaitodev.core.common.config.AdUnits
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class InterstitialHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adUnits: AdUnits,
    private val repository: InterstitialAdRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var ad: InterstitialAd? = null

    fun preload() {
        if (ad != null) {
            return
        }
        val req = AdRequest.Builder().build()
        // 汎用化されたプロパティ名を参照
        val unitId = adUnits.interstitialUnitA
        InterstitialAd.load(context, unitId, req, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                ad = p0
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                ad = null
            }
        })
    }

    suspend fun tryShow(
        activity: Activity,
        enabled: Boolean,
        sessionCap: Int,
        minIntervalSec: Long
    ): Boolean {
        val shownCount = repository.shownCountThisSession.first()
        val lastShown = repository.lastShownEpochSec.first()

        if (!enabled) {
            return false
        }
        if (ad == null) {
            preload()
            return false
        }
        if (sessionCap > 0 && shownCount >= sessionCap) {
            return false
        }
        val now = System.currentTimeMillis() / 1000
        if (minIntervalSec > 0 && (now - lastShown) < minIntervalSec) {
            return false
        }

        return suspendCancellableCoroutine { cont ->
            val currentAd = ad
            if (currentAd == null) {
                cont.resume(false)
                return@suspendCancellableCoroutine
            }

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    scope.launch {
                        repository.updateLastShownTimestamp()
                    }
                    ad = null // 使い捨て
                    preload()
                    if (cont.isActive) cont.resume(true)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    ad = null
                    preload()
                    if (cont.isActive) cont.resume(false)
                }

                override fun onAdShowedFullScreenContent() {
                    scope.launch {
                        repository.incrementShownCount()
                    }
                }
            }
            try {
                currentAd.show(activity)
            } catch (t: Throwable) {
                ad = null
                preload()
                if (cont.isActive) cont.resume(false)
            }

            cont.invokeOnCancellation {
            }
        }
    }
}
