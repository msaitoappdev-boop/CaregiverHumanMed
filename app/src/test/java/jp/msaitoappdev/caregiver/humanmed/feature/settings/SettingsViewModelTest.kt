package jp.msaitoappdev.caregiver.humanmed.feature.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderPrefs
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private val dataStore: DataStore<Preferences> = mockk()
    private val premiumRepo: PremiumRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(ReminderScheduler)

        // Stub the static methods of ReminderScheduler to do nothing
        every { ReminderScheduler.scheduleDaily(any(), any(), any()) } returns Unit
        every { ReminderScheduler.cancel(any()) } returns Unit

        // Mock the DataStore behavior explicitly
        coEvery { dataStore.data } returns flowOf(preferencesOf()) // Default empty flow for init
        coEvery { dataStore.updateData(any()) } returns preferencesOf() // Mock the update function to return empty preferences

        viewModel = SettingsViewModel(dataStore, premiumRepo)
    }

    @After
    fun tearDown() {
        unmockkObject(ReminderScheduler)
        Dispatchers.resetMain()
    }

    @Test
    fun `settings flow emits correct values from datastore`() = runTest {
        // Arrange
        val prefs = preferencesOf(
            ReminderPrefs.ENABLED to true,
            ReminderPrefs.HOUR to 21,
            ReminderPrefs.MINUTE to 30
        )
        coEvery { dataStore.data } returns flowOf(prefs)
        val newViewModel = SettingsViewModel(dataStore, premiumRepo)

        // Act & Assert
        newViewModel.settings.test {
            val settings = awaitItem()
            assertThat(settings.enabled).isTrue()
            assertThat(settings.hour).isEqualTo(21)
            assertThat(settings.minute).isEqualTo(30)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setEnabled true schedules reminder`() = runTest {
        // Act
        viewModel.setEnabled(context, true, 20, 0)

        // Assert
        coVerify { dataStore.updateData(any()) }
        verify { ReminderScheduler.scheduleDaily(context, 20, 0) }
    }

    @Test
    fun `setEnabled false cancels reminder`() = runTest {
        // Act
        viewModel.setEnabled(context, false, 20, 0)

        // Assert
        coVerify { dataStore.updateData(any()) }
        verify { ReminderScheduler.cancel(context) }
    }

    @Test
    fun `setTime reschedules if enabled`() = runTest {
        // Arrange
        val prefs = preferencesOf(ReminderPrefs.ENABLED to true)
        coEvery { dataStore.data } returns flowOf(prefs)

        // Act
        viewModel.setTime(context, 22, 15)

        // Assert
        coVerify { dataStore.updateData(any()) }
        verify { ReminderScheduler.scheduleDaily(context, 22, 15) }
    }

    @Test
    fun `setTime does not reschedule if disabled`() = runTest {
        // Arrange
        val prefs = preferencesOf(ReminderPrefs.ENABLED to false)
        coEvery { dataStore.data } returns flowOf(prefs)

        // Act
        viewModel.setTime(context, 22, 15)

        // Assert
        coVerify { dataStore.updateData(any()) }
        verify(exactly = 0) { ReminderScheduler.scheduleDaily(any(), any(), any()) }
    }

    @Test
    fun `restorePurchases calls repository`() = runTest {
        // Act
        viewModel.restorePurchases()

        // Assert
        coVerify { premiumRepo.refreshFromBilling() }
    }
}
