package jp.msaitoappdev.caregiver.humanmed.domain.repository

import jp.msaitoappdev.caregiver.humanmed.domain.model.QuotaState
import kotlinx.coroutines.flow.Flow

interface StudyQuotaRepository {
    fun observe(freeDailySetsProvider: () -> Int): Flow<QuotaState>
    suspend fun markSetFinished()
    suspend fun grantByReward()
}
