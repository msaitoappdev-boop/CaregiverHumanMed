package jp.msaitoappdev.caregiver.humanmed.feature.premium

import android.app.Activity
import app.cash.turbine.test
import com.android.billingclient.api.ProductDetails
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PremiumViewModelTest {

    private val billingManager: BillingManager = mockk(relaxed = true)
    private val premiumRepo: PremiumRepository = mockk(relaxed = true)
    private val activity: Activity = mockk(relaxed = true)
    private lateinit var viewModel: PremiumViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

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
        // Arrange
        val isPremiumFlow = MutableStateFlow(false)
        coEvery { premiumRepo.isPremium } returns isPremiumFlow
        viewModel = PremiumViewModel(billingManager, premiumRepo)

        // Act & Assert
        viewModel.uiState.test {
            assertThat(awaitItem().isPremium).isFalse()

            isPremiumFlow.value = true
            assertThat(awaitItem().isPremium).isTrue()
        }
    }

    @Test
    fun `onPurchaseClick launches billing flow when product details are available`() = runTest {
        // Arrange
        val productDetails = mockk<ProductDetails>()
        coEvery { billingManager.getProductDetails() } returns productDetails
        viewModel = PremiumViewModel(billingManager, premiumRepo)

        // Act
        viewModel.onPurchaseClick(activity)

        // Assert
        verify { billingManager.launchPurchase(activity, productDetails) }
    }

    @Test
    fun `onPurchaseClick shows message when product details are unavailable`() = runTest {
        // Arrange
        coEvery { billingManager.getProductDetails() } returns null
        viewModel = PremiumViewModel(billingManager, premiumRepo)

        // Act & Assert
        viewModel.event.test {
            viewModel.onPurchaseClick(activity)
            val event = awaitItem()
            assertThat(event).isInstanceOf(PaywallEvent.ShowMessage::class.java)
            assertThat((event as PaywallEvent.ShowMessage).message).isEqualTo("商品情報を取得できませんでした")
        }
    }

    @Test
    fun `refresh calls repository`() = runTest {
        // Arrange
        viewModel = PremiumViewModel(billingManager, premiumRepo)

        // Act
        viewModel.refresh()

        // Assert
        coVerify { premiumRepo.refreshFromBilling() }
    }

    @Test
    fun `devTogglePremium calls repository`() = runTest {
        // Arrange
        viewModel = PremiumViewModel(billingManager, premiumRepo)

        // Act
        viewModel.devTogglePremium(true)

        // Assert
        coVerify { premiumRepo.setPremiumForDebug(true) }
    }
}
