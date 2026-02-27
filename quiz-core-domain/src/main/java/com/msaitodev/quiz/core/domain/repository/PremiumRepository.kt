package com.msaitodev.quiz.core.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface PremiumRepository {
    val isPremium: StateFlow<Boolean>
    suspend fun refreshFromBilling()
    suspend fun savePremiumStatus(isPremium: Boolean)
    suspend fun setPremiumForDebug(enabled: Boolean)
}
