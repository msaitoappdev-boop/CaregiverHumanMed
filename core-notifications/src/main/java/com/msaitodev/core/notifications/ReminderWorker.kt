package com.msaitodev.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notifier: ReminderNotifier
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            notifier.show(applicationContext)
            Result.success()
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS 権限がない場合など
            Result.failure()
        }
    }
}