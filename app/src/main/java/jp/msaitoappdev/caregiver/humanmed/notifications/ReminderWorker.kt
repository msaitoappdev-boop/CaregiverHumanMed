package jp.msaitoappdev.caregiver.humanmed.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            ReminderNotifier.show(context)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS 権限がない場合など
            return Result.failure()
        }
        return Result.success()
    }
}
