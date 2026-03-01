package com.msaitodev.quiz.core.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.msaitodev.quiz.core.domain.config.RemoteConfigKeys
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val quotaRepo: StudyQuotaRepository,
    private val premiumRepo: PremiumRepository,
    private val remoteConfigRepo: RemoteConfigRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            // 通知権限の確認
            if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
                return Result.success()
            }

            val isPremium = premiumRepo.isPremium.value
            val limitKey = if (isPremium) RemoteConfigKeys.PREMIUM_DAILY_SETS else RemoteConfigKeys.FREE_DAILY_SETS
            val limit = remoteConfigRepo.getLong(limitKey).toInt()

            // 今日の進捗を確認
            val quota = quotaRepo.observe { limit }.first()

            // 1セットも完了していない（未実施）場合のみ通知を表示
            if (quota.usedSets == 0) {
                ReminderNotifier.show(applicationContext)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
