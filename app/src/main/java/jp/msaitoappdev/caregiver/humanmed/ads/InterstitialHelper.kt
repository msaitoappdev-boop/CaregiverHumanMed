package jp.msaitoappdev.caregiver.humanmed.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.msaitoappdev.caregiver.humanmed.config.AdUnits
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: InterstitialAdRepository
) {
    private val TAG = "InterstitialHelper"
    private val scope = CoroutineScope(Dispatchers.IO)

    private var ad: InterstitialAd? = null

    fun preload() {
        if (ad != null) return
        val req = AdRequest.Builder().build()
        val unitId = AdUnits.interstitialWeaktrainComplete(context)
        InterstitialAd.load(context, unitId, req, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                ad = p0
                Log.d(TAG, "Interstitial loaded")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                ad = null
                Log.w(TAG, "Interstitial load failed: ${p0.message}")
            }
        })
    }

    suspend fun tryShow(
        activity: Activity,
        enabled: Boolean,
        sessionCap: Int,
        minIntervalSec: Long,
        onAdClosed: () -> Unit
    ) {
        val shownCount = repository.shownCountThisSession.first()
        val lastShown = repository.lastShownEpochSec.first()

        if (!enabled) return onAdClosed()
        if (sessionCap > 0 && shownCount >= sessionCap) return onAdClosed()
        val now = System.currentTimeMillis() / 1000
        if (minIntervalSec > 0 && (now - lastShown) < minIntervalSec) return onAdClosed()

        val current = ad
        if (current != null) {
            current.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    scope.launch {
                        repository.updateLastShownTimestamp()
                    }
                    ad = null // 使い捨て
                    Log.d(TAG, "Interstitial dismissed")
                    preload()
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                    ad = null
                    Log.w(TAG, "Interstitial failed to show: ${p0.message}")
                    preload()
                    onAdClosed()
                }
            }
            current.show(activity)
            scope.launch {
                repository.incrementShownCount()
            }
        } else {
            preload() // Preload for next time
            onAdClosed()
        }
    }
}
