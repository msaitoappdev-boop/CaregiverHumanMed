package com.msaitodev.quiz.core.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object ReminderNotifier {
    // 重要度設定を確実に反映させるため、IDを v2 に更新
    const val CHANNEL_ID = "reminder_daily_v2"
    private const val NOTIFICATION_ID = 1001

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun show(
        context: Context,
        title: String? = null,
        text: String? = null
    ) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("caregiver://reminder") // DeepLink
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= 23)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        val contentPI = PendingIntent.getActivity(context, 0, intent, flags)

        val finalTitle = title ?: context.getString(R.string.notification_reminder_title_start)
        val finalText = text ?: context.getString(R.string.notification_reminder_text_start)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_daily_quiz)
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
