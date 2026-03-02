package com.msaitodev.core.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 日次のリマインド通知を実行する Worker。
 * 具体的な通知条件や内容は [NotificationPolicy] に委譲することで、
 * 本モジュールからドメイン知識（クイズの進捗など）を排除しています。
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val policy: NotificationPolicy,
    private val notifier: ReminderNotifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            // 通知権限の確認（Android 13以降）
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return Result.success()
                }
            }

            // ポリシーに基づいて通知内容を解決
            val data = policy.resolveNotificationData(applicationContext)

            if (data != null) {
                // 通知の表示
                notifier.show(
                    context = applicationContext,
                    title = data.title,
                    text = data.text
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
