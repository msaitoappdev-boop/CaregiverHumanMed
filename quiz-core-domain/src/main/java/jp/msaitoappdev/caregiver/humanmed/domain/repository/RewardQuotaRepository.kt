package jp.msaitoappdev.caregiver.humanmed.domain.repository

import kotlinx.coroutines.flow.Flow

interface RewardQuotaRepository {
    fun grantedCountTodayFlow(): Flow<Int>
    suspend fun tryGrantOncePerDay(): Boolean
}
