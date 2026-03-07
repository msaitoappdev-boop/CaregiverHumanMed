package com.msaitodev.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * UMP（User Messaging Platform）による同意取得の最小実装。
 * - アプリ起動時に呼び出し、必要な場合のみフォームを表示します。
 * - フォームが不要／表示完了／エラー時は onReady を呼んで処理継続します。
 *
 * 依存関係："com.google.android.ump:user-messaging-platform:2.2.0"
 */
object ConsentManager {
    fun obtain(activity: Activity, onReady: () -> Unit = {}) {
        val paramsBuilder = ConsentRequestParameters.Builder()

        // デバッグビルドでは、強制的にEEA（欧州経済領域）からアクセスしたとみなし、
        // フォームが必ず表示されるようにしてテストを容易にする。
        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        val ci: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)
        // 毎起動で最新状態を取得
        ci.requestConsentInfoUpdate(activity, paramsBuilder.build(),
            {
                // 必要なら同意フォームを即時表示
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    // フォーム不要、または表示→閉じた後
                    onReady()
                }
            },
            { _ ->
                // 同意情報の取得エラー時も継続（NPAなどはAdMob側の設定に従う）
                onReady()
            }
        )
    }

    /**
     * 広告リクエストが可能かどうかを判定します。
     * UMP SDK 2.1.0 以降の canRequestAds() を使用します。
     */
    fun canRequestAds(context: Context): Boolean {
        val ci = UserMessagingPlatform.getConsentInformation(context)
        return ci.canRequestAds()
    }
}
