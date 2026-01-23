package jp.msaitoappdev.caregiver.humanmed.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        ReminderNotifier.show(applicationContext)
        return Result.success()
    }
}
