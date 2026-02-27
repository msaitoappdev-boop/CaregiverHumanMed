package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 次のクイズセットを開始できるかどうかを判断し、その結果を返すユースケース。
 * これまで HomeViewModel が担っていたビジネスロジックをドメイン層に移動させた、新しい「司令塔」。
 */
class StartNextQuizUseCase @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val premiumRepo: PremiumRepository,
    private val remoteConfigRepo: RemoteConfigRepository,
) {
    suspend operator fun invoke(): Result {
        val isPremium = premiumRepo.isPremium.first()
        val limitKey = if (isPremium) "premium_daily_sets" else "free_daily_sets"
        val limit = remoteConfigRepo.getLong(limitKey).toInt()
        val currentQuota = quotaRepo.observe { limit }.first()

        return if (currentQuota.canStart) {
            Result.CanStart
        } else {
            // 上限に達している場合
            if (isPremium) {
                Result.QuotaExceeded
            } else {
                // 無料ユーザーの場合、リワード付与状況を確認
                if (currentQuota.rewardedGranted < 1) {
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

        /** 上限に達している（プレミアムユーザー、またはリワード使用済み） */
        object QuotaExceeded : Result

        /** 上限に達しており、かつ本日のリワードも使用済み */
        object QuotaExceededAndRewardUsed : Result
    }
}
