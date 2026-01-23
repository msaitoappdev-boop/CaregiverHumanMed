package jp.msaitoappdev.caregiver.humanmed.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import jp.msaitoappdev.caregiver.humanmed.MainActivity
import jp.msaitoappdev.caregiver.humanmed.R

object ReminderNotifier {
    const val CHANNEL_ID = "reminder_daily_channel"
    private const val NOTIFICATION_ID = 1001

    fun show(context: Context) {
        // 通知タップでアプリ（Home）を開く。必要に応じて deep link に差し替え可。
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setClass(context, MainActivity::class.java)
            data = android.net.Uri.parse("caregiver://reminder")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= 23)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        val contentPI = PendingIntent.getActivity(context, 0, intent, flags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // ★可能なら白一色の通知用アイコンに差し替え
            .setContentTitle("今日の3問を解きましょう")
            .setContentText("毎日の積み重ねが合格に近づきます。今すぐスタート！")
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }
}
