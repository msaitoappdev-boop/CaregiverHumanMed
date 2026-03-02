package com.msaitodev.core.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotifier @Inject constructor(
    private val policy: NotificationPolicy
) {
    companion object {
        private const val NOTIFICATION_ID = 1001
    }

    /**
     * 指定された内容で通知を表示します。
     * 内容が指定されない場合は、ポリシーのデフォルト値を使用します。
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun show(
        context: Context,
        title: String? = null,
        text: String? = null
    ) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(policy.deepLinkUri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= 23)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        val contentPI = PendingIntent.getActivity(context, 0, intent, flags)

        val finalTitle = title ?: policy.defaultTitle
        val finalText = text ?: policy.defaultText

        val builder = NotificationCompat.Builder(context, policy.channelId)
            .setSmallIcon(policy.smallIconResId)
            .setContentTitle(finalTitle)
            .setContentText(finalText)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission missing at runtime
        }
    }
}
