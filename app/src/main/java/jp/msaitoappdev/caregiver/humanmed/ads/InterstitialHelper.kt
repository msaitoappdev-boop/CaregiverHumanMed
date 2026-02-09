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
        if (ad != null) {
            Log.d(TAG, "preload: Ad already loaded.")
            return
        }
        val req = AdRequest.Builder().build()
        val unitId = AdUnits.interstitialWeaktrainComplete(context)
        Log.d(TAG, "preload: Loading ad with unit ID: $unitId")
        InterstitialAd.load(context, unitId, req, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                ad = p0
                Log.d(TAG, "Interstitial loaded")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                ad = null
                Log.w(TAG, "Interstitial load failed: ${p0.code} - ${p0.message}")
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
        Log.d(TAG, "tryShow called with: enabled=$enabled, sessionCap=$sessionCap, minIntervalSec=$minIntervalSec")
        val shownCount = repository.shownCountThisSession.first()
        val lastShown = repository.lastShownEpochSec.first()
        Log.d(TAG, "Current state: shownCountThisSession=$shownCount, lastShownEpochSec=$lastShown")

        if (!enabled) {
            Log.d(TAG, "Ad not shown: Remote config is disabled.")
            onAdClosed()
            return
        }
        if (ad == null) {
            Log.d(TAG, "Ad not shown: Not loaded yet. Calling preload() for next time.")
            preload()
            onAdClosed()
            return
        }
        if (sessionCap > 0 && shownCount >= sessionCap) {
            Log.d(TAG, "Ad not shown: Session cap reached (count=$shownCount, cap=$sessionCap).")
            onAdClosed()
            return
        }
        val now = System.currentTimeMillis() / 1000
        if (minIntervalSec > 0 && (now - lastShown) < minIntervalSec) {
            Log.d(TAG, "Ad not shown: Minimum interval not reached (now=$now, lastShown=$lastShown, interval=$minIntervalSec).")
            onAdClosed()
            return
        }

        Log.d(TAG, "Ad is ready to be shown.")
        ad?.let {
            it.fullScreenContentCallback = object : FullScreenContentCallback() {
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
                    Log.w(TAG, "Interstitial failed to show: ${p0.code} - ${p0.message}")
                    preload()
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial showed successfully.")
                    scope.launch {
                        repository.incrementShownCount()
                    }
                }
            }
            it.show(activity)
        } ?: run {
            // Fallback in case 'ad' becomes null between the check and here.
            Log.e(TAG, "Ad became null unexpectedly before show().")
            onAdClosed()
        }
    }
}
