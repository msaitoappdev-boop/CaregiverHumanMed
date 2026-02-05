package jp.msaitoappdev.caregiver.humanmed.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds

/**
 * Google Mobile Ads SDK の初期化を「一度だけ」にするための薄いラッパー。
 * - 複数回呼んでも 1 回しか初期化しない（ガード）
 * - 既存コード（RewardedHelper からの初期化）と両立
 */
object AdsSdk {
    private const val TAG = "AdsSdk"
    @Volatile private var initialized = false

    /**
     * 必要なら初期化する（複数スレッドから呼ばれても 1 回だけ）
     */
    fun initIfNeeded(context: Context) {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    try {
                        MobileAds.initialize(context) { status ->
                            Log.d(TAG, "MobileAds.initialize() done: ${status.adapterStatusMap.keys}")
                        }
                        initialized = true
                    } catch (t: Throwable) {
                        Log.w(TAG, "MobileAds.initialize() threw: ${t.message}")
                    }
                }
            }
        }
    }
}