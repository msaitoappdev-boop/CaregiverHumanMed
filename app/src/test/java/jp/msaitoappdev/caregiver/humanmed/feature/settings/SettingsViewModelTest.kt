package jp.msaitoappdev.caregiver.humanmed.feature.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderPrefs
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
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    private val dataStore: DataStore<Preferences> = mock()
    private val premiumRepo: PremiumRepository = mock()
    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)

        // Mock the DataStore behavior explicitly
        dataStore.stub { onBlocking { data } doReturn flowOf(preferencesOf()) } // Default empty flow
        dataStore.stub { onBlocking { updateData(any()) } doReturn preferencesOf() } // Mock update

        viewModel = SettingsViewModel(dataStore, premiumRepo)
    }

    @After
    fun tearDown() {
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
        dataStore.stub { onBlocking { data } doReturn flowOf(prefs) }
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
        verify(dataStore).updateData(any())
        val workInfos = workManager.getWorkInfosForUniqueWork("daily-quiz-reminder").get()
        assertThat(workInfos).hasSize(1)
        assertThat(workInfos.first().state).isEqualTo(WorkInfo.State.ENQUEUED)
    }

    @Test
    fun `setEnabled false cancels reminder`() = runTest {
        // Arrange: First, schedule a reminder
        viewModel.setEnabled(context, true, 20, 0)
        val initialWorkInfos = workManager.getWorkInfosForUniqueWork("daily-quiz-reminder").get()
        assertThat(initialWorkInfos.first().state).isEqualTo(WorkInfo.State.ENQUEUED)

        // Act
        viewModel.setEnabled(context, false, 20, 0)

        // Assert
        verify(dataStore, Mockito.times(2)).updateData(any())
        val finalWorkInfo = workManager.getWorkInfosForUniqueWork("daily-quiz-reminder").get().first()
        assertThat(finalWorkInfo.state).isEqualTo(WorkInfo.State.CANCELLED)
    }

    @Test
    fun `setTime reschedules if enabled`() = runTest {
        // Arrange
        val prefs = preferencesOf(ReminderPrefs.ENABLED to true)
        dataStore.stub { onBlocking { data } doReturn flowOf(prefs) }

        // Act
        viewModel.setTime(context, 22, 15)

        // Assert
        verify(dataStore).updateData(any())
        val workInfos = workManager.getWorkInfosForUniqueWork("daily-quiz-reminder").get()
        assertThat(workInfos).hasSize(1)
        assertThat(workInfos.first().state).isEqualTo(WorkInfo.State.ENQUEUED)
    }

    @Test
    fun `setTime does not reschedule if disabled`() = runTest {
        // Arrange
        val prefs = preferencesOf(ReminderPrefs.ENABLED to false)
        dataStore.stub { onBlocking { data } doReturn flowOf(prefs) }

        // Act
        viewModel.setTime(context, 22, 15)

        // Assert
        verify(dataStore).updateData(any())
        val workInfos = workManager.getWorkInfosForUniqueWork("daily-quiz-reminder").get()
        assertThat(workInfos).isEmpty()
    }

    @Test
    fun `restorePurchases calls repository`() = runTest {
        // Act
        viewModel.restorePurchases()

        // Assert
        verify(premiumRepo).refreshFromBilling()
    }
}
