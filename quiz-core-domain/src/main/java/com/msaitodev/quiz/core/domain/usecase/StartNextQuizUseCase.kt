package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.core.ads.RewardedHelper
import com.msaitodev.quiz.core.domain.config.RemoteConfigKeys
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 次のクイズセットを開始できるかどうかを判断し、その結果を返すユースケース。
 * 学習進捗（StudyQuota）と広告獲得状況（RewardedHelper）の両方を参照して最終判断を行う。
 */
class StartNextQuizUseCase @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val rewardedHelper: RewardedHelper,
    private val premiumRepo: PremiumRepository,
    private val remoteConfigRepo: RemoteConfigRepository,
) {
    suspend operator fun invoke(): Result {
        val isPremium = premiumRepo.isPremium.first()
        val limitKey = if (isPremium) RemoteConfigKeys.PREMIUM_DAILY_SETS else RemoteConfigKeys.FREE_DAILY_SETS
        val limit = remoteConfigRepo.getLong(limitKey).toInt()
        
        // 学習セットの消化状況を取得
        val currentQuota = quotaRepo.observe { limit }.first()
        // リワード広告の視聴が可能（上限に達していない）か確認
        val canShowReward = rewardedHelper.canShowToday.first()

        return if (currentQuota.canStart) {
            Result.CanStart
        } else {
            // 上限に達している場合
            if (isPremium) {
                Result.QuotaExceeded
            } else {
                // 無料ユーザーの場合、リワード視聴が可能であればオファーを出す
                if (canShowReward) {
                    Result.ShowRewardOffer
                } else {
                    Result.QuotaExceededAndRewardUsed
                }
            }
        }
    }

    sealed interface Result {
        /** クイズを開始できる */
        object CanStart : Result

        /** 上限に達しており、リワード広告のオファーを出すべき */
        object ShowRewardOffer : Result

        /** 上限に達している（プレミアムユーザー） */
        object QuotaExceeded : Result

        /** 上限に達しており、かつ本日のリワードも使用済み */
        object QuotaExceededAndRewardUsed : Result
    }
}
