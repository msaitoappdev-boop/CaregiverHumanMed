package com.msaitodev.caregiver.humanmed

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.msaitodev.quiz.core.notifications.ReminderNotifier
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CaregiverApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // リマインド用チャネル作成
        createReminderChannel()

        // Remote Config
        val rc = Firebase.remoteConfig
        rc.setDefaultsAsync(R.xml.remote_config_defaults)
        rc.fetchAndActivate()

        // Analytics
        Firebase.analytics.setAnalyticsCollectionEnabled(false)
    }

    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // キャッシュ問題を解消するため、IDを v3 に更新
            val channel = NotificationChannel(
                ReminderNotifier.CHANNEL_ID,
                "学習リマインド",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { 
                description = "毎日3問のリマインド通知"
                // ロック画面などでの表示設定
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }
}
