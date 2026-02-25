package com.msaitodev.caregiver.humanmed

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.getSystemService
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.msaitodev.quiz.core.notifications.ReminderNotifier
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CaregiverApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 20:00 リマインド用チャネル
        createReminderChannel()

        // Remote Config（Analyticsと独立運用）
        val rc = Firebase.remoteConfig
        rc.setDefaultsAsync(R.xml.remote_config_defaults)
        rc.fetchAndActivate()

        // Analytics は既定オフ。同意後にONにする（Main側で切替）
        Firebase.analytics.setAnalyticsCollectionEnabled(false)
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