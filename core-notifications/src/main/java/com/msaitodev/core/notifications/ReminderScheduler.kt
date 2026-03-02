package com.msaitodev.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 通知のスケジュール管理を行うユーティリティ。
 */
object ReminderScheduler {

    private const val UNIQUE_NAME = "daily-reminder"

    fun scheduleDaily(context: Context, hour: Int = 8, minute: Int = 0) {
        val initialDelay = computeInitialDelayMinutes(hour, minute)
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .build()

        // 時刻変更を即座に反映させるため CANCEL_AND_REENQUEUE を使用
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    private fun computeInitialDelayMinutes(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val duration = Duration.between(now, next)
        return duration.toMinutes()
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
    }
}
