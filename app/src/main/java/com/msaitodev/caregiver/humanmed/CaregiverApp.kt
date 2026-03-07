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
import com.msaitodev.core.notifications.NotificationPolicy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CaregiverApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationPolicy: NotificationPolicy

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
            val channel = NotificationChannel(
                notificationPolicy.channelId,
                notificationPolicy.channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply { 
                description = notificationPolicy.defaultText
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }
}
