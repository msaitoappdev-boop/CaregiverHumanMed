package com.msaitodev.caregiver.humanmed.notifications

import android.content.Context
import com.msaitodev.caregiver.humanmed.R
import com.msaitodev.quiz.core.domain.config.RemoteConfigKeys
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import com.msaitodev.core.notifications.NotificationData
import com.msaitodev.core.notifications.NotificationPolicy
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * クイズアプリ向けのリマインド通知ポリシー実装。
 */
@Singleton
class QuizNotificationPolicy @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val premiumRepo: PremiumRepository,
    private val remoteConfigRepo: RemoteConfigRepository
) : NotificationPolicy {

    override val channelId: String = "reminder_daily_v2"
    override val channelName: String = "Daily Reminder"
    override val smallIconResId: Int = R.drawable.ic_notification_daily_quiz
    override val deepLinkUri: String = "caregiver://reminder"

    override val defaultTitle: String = ""
    override val defaultText: String = ""

    override suspend fun resolveNotificationData(context: Context): NotificationData? {
        val isPremium = premiumRepo.isPremium.value
        val limitKey = if (isPremium) RemoteConfigKeys.PREMIUM_DAILY_SETS else RemoteConfigKeys.FREE_DAILY_SETS
        val limit = remoteConfigRepo.getLong(limitKey).toInt()

        // 今日の進捗を確認
        val quota = quotaRepo.observe { limit }.first()

        return when {
            // 1セットも完了していない場合
            quota.usedSets == 0 -> {
                NotificationData(
                    title = context.getString(R.string.notification_reminder_title_start),
                    text = context.getString(R.string.notification_reminder_text_start)
                )
            }
            // プレミアムユーザーで、かつ残りセット数がある場合
            isPremium && quota.usedSets < limit -> {
                val remaining = limit - quota.usedSets
                NotificationData(
                    title = context.getString(R.string.notification_reminder_title_continue),
                    text = context.getString(R.string.notification_reminder_text_continue, remaining)
                )
            }
            else -> null
        }
    }
}
