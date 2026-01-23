package jp.msaitoappdev.caregiver.humanmed.notifications

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val UNIQUE_NAME = "daily-quiz-reminder"

    fun scheduleDaily(context: Context, hour: Int = 20, minute: Int = 0) {
        val initialDelay = computeInitialDelayMinutes(hour, minute)
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // 最新設定で差し替え
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
