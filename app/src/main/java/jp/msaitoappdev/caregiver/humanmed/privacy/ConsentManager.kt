package jp.msaitoappdev.caregiver.humanmed.privacy

import android.app.Activity
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
        val params = ConsentRequestParameters.Builder().build()
        val ci: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)
        // 毎起動で最新状態を取得
        ci.requestConsentInfoUpdate(activity, params,
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
}
