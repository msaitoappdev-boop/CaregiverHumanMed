package jp.msaitoappdev.caregiver.humanmed.core.premium

import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class PremiumRepositoryImplTest {

    private lateinit var mockBillingManager: BillingManager
    private lateinit var repository: PremiumRepositoryImpl

    private val isPremiumFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        mockBillingManager = mockk<BillingManager>(relaxed = true)
        every { mockBillingManager.isPremium } returns isPremiumFlow
        repository = PremiumRepositoryImpl(mockBillingManager)
    }

    @Test
    fun `isPremium flow reflects the value from BillingManager`() = runTest {
        assertEquals(false, repository.isPremium.value)

        isPremiumFlow.value = true
        assertEquals(true, repository.isPremium.first())

        isPremiumFlow.value = false
        assertEquals(false, repository.isPremium.first())
    }

    @Test
    fun `refreshFromBilling calls refreshEntitlements on BillingManager`() = runTest {
        repository.refreshFromBilling()
        coVerify { mockBillingManager.refreshEntitlements() }
    }

    @Test
    fun `setPremiumForDebug calls setPremiumForDebug on BillingManager`() = runTest {
        val isDebugPremium = true
        repository.setPremiumForDebug(isDebugPremium)
        verify { mockBillingManager.setPremiumForDebug(isDebugPremium) }
    }
}
