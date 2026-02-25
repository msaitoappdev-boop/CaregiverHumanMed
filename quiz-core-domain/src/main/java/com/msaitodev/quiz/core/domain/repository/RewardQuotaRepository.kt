package com.msaitodev.quiz.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface RewardQuotaRepository {
    fun grantedCountTodayFlow(): Flow<Int>
    suspend fun tryGrantOncePerDay(): Boolean
}
