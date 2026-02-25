package com.msaitodev.quiz.core.domain.repository

import com.msaitodev.quiz.core.domain.model.QuotaState
import kotlinx.coroutines.flow.Flow

interface StudyQuotaRepository {
    fun observe(freeDailySetsProvider: () -> Int): Flow<QuotaState>
    suspend fun markSetFinished()
    suspend fun grantByReward()
}
