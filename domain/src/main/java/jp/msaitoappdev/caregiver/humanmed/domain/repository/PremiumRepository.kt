package jp.msaitoappdev.caregiver.humanmed.domain.repository

import kotlinx.coroutines.flow.Flow

interface PremiumRepository {
    val isPremiumFlow: Flow<Boolean>
    suspend fun refreshFromBilling()
    suspend fun setPremiumForDebug(enabled: Boolean)
}
