package jp.msaitoappdev.caregiver.humanmed.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import jp.msaitoappdev.caregiver.humanmed.config.AdUnits

object InterstitialHelper {
    private const val TAG = "InterstitialHelper"

    private var ad: InterstitialAd? = null
    private var shownCountThisSession = 0
    private var lastShownEpochSec = 0L

    fun preload(context: Context) {
        if (ad != null) return
        val req = AdRequest.Builder().build()
        val unitId = AdUnits.interstitialWeaktrainComplete(context)
        InterstitialAd.load(context, unitId, req, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                ad = p0
                ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        ad = null // 使い捨て。閉じたら次をプリロード
                        lastShownEpochSec = System.currentTimeMillis() / 1000
                        Log.d(TAG, "Interstitial dismissed")
                        preload(context)
                    }
                    override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                        ad = null
                        Log.w(TAG, "Interstitial failed to show: ${p0.message}")
                        preload(context)
                    }
                }
                Log.d(TAG, "Interstitial loaded")
            }
            override fun onAdFailedToLoad(p0: LoadAdError) {
                ad = null
                Log.w(TAG, "Interstitial load failed: ${p0.message}")
            }
        })
    }

    /**
     * Remote Config の閾値で表示を制御（session cap / インターバル）
     */
    fun tryShow(activity: Activity,
                enabled: Boolean,
                sessionCap: Int,
                minIntervalSec: Long,
                onNotShown: () -> Unit = {}) {
        if (!enabled) return onNotShown()
        if (sessionCap > 0 && shownCountThisSession >= sessionCap) return onNotShown()
        val now = System.currentTimeMillis() / 1000
        if (minIntervalSec > 0 && (now - lastShownEpochSec) < minIntervalSec) return onNotShown()

        val current = ad
        if (current != null) {
            current.show(activity)
            shownCountThisSession++
        } else {
            onNotShown()
        }
    }
}
