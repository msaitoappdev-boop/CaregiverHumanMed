package jp.msaitoappdev.caregiver.humanmed

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.getSystemService
import dagger.hilt.android.HiltAndroidApp
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderNotifier

@HiltAndroidApp

class CaregiverApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createReminderChannel()
    }
    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderNotifier.CHANNEL_ID,
                "学習リマインド",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "毎日3問のリマインド通知" }
            getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }
}
