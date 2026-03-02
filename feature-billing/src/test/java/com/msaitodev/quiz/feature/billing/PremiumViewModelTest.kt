package com.msaitodev.quiz.feature.billing

import android.app.Activity
import app.cash.turbine.test
import com.android.billingclient.api.ProductDetails
import com.google.common.truth.Truth.assertThat
import com.msaitodev.feature.billing.PaywallEvent
import com.msaitodev.feature.billing.PremiumViewModel
import com.msaitodev.quiz.core.common.billing.BillingManager
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class PremiumViewModelTest {

    private val billingManager: BillingManager = mock(BillingManager::class.java)
    private val premiumRepo: PremiumRepository = mock(PremiumRepository::class.java)
    private val activity: Activity = mock(Activity::class.java)
    private lateinit var viewModel: PremiumViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState reflects premium status`() = runTest {
        val isPremiumFlow = MutableStateFlow(false)
        `when`(premiumRepo.isPremium).thenReturn(isPremiumFlow)
        viewModel = PremiumViewModel(billingManager, premiumRepo)

        viewModel.uiState.test {
            assertThat(awaitItem().isPremium).isFalse()

            isPremiumFlow.value = true
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem().isPremium).isTrue()
        }
    }

    @Test
    fun `onPurchaseClick launches billing flow when product details are available`() = runTest {
        val productDetails: ProductDetails = mock(ProductDetails::class.java)
        `when`(billingManager.getProductDetails()).thenReturn(productDetails)
        viewModel = PremiumViewModel(billingManager, premiumRepo)

        viewModel.onPurchaseClick(activity)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(billingManager).launchPurchase(activity, productDetails)
    }

    @Test
    fun `onPurchaseClick emits error event when product details are null`() = runTest {
        `when`(billingManager.getProductDetails()).thenReturn(null)
        viewModel = PremiumViewModel(billingManager, premiumRepo)

        viewModel.event.test {
            viewModel.onPurchaseClick(activity)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem() as PaywallEvent.ShowMessage
            assertThat(event.messageResId).isEqualTo(R.string.paywall_error_product_details)
        }
    }

    @Test
    fun `refresh calls repository`() = runTest {
        viewModel = PremiumViewModel(billingManager, premiumRepo)
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(premiumRepo).refreshFromBilling()
    }

    @Test
    fun `devTogglePremium calls repository`() = runTest {
        viewModel = PremiumViewModel(billingManager, premiumRepo)
        viewModel.devTogglePremium(true)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(premiumRepo).setPremiumForDebug(true)
    }
}
