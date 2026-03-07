package com.msaitodev.core.notifications

import android.content.Context

/**
 * 通知の具体的な内容や設定を供給するポリシーインターフェース。
 * アプリケーションごとに異なるドメイン知識（文言、アイコン、遷移先URIなど）を抽象化します。
 */
interface NotificationPolicy {
    /** 通知チャンネルID */
    val channelId: String

    /** 通知チャンネル名（システム設定に表示される名前） */
    val channelName: String

    /** 通知の小アイコンリソースID */
    val smallIconResId: Int

    /** 通知タップ時の遷移先ディープリンクURI */
    val deepLinkUri: String

    /** デフォルトのタイトル（resolveNotificationData が null を返した場合などのフォールバック用） */
    val defaultTitle: String

    /** デフォルトのテキスト */
    val defaultText: String

    /**
     * 現在の状態に基づいて通知すべき内容を解決します。
     * 通知を出す必要がない場合は null を返します。
     */
    suspend fun resolveNotificationData(context: Context): NotificationData?
}

/**
 * 通知の表示内容
 */
data class NotificationData(
    val title: String,
    val text: String
)
