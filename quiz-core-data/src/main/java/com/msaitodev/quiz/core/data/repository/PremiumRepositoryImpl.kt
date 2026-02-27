package com.msaitodev.quiz.core.data.repository

import com.msaitodev.quiz.core.common.billing.BillingManager
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val billing: BillingManager
) : PremiumRepository {

    override val isPremium: StateFlow<Boolean> = billing.isPremium

    override suspend fun refreshFromBilling() {
        billing.refreshEntitlements()
    }

    override suspend fun savePremiumStatus(isPremium: Boolean) {
        // This is now managed by BillingManager and SharedPreferences
        // The method is kept for API compatibility if other parts of the app use it,
        // but it should ideally be removed in a future refactoring.
    }

    override suspend fun setPremiumForDebug(enabled: Boolean) {
        billing.setPremiumForDebug(enabled)
    }
}