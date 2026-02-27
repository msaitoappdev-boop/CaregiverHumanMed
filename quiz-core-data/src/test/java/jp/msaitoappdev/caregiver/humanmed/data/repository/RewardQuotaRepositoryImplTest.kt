package jp.msaitoappdev.caregiver.humanmed.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.data.repository.RewardQuotaRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class RewardQuotaRepositoryImplTest {

    private fun runTestWithDataStore(block: suspend (DataStore<Preferences>, RewardQuotaRepositoryImpl) -> Unit) = runTest {
        val testDataStoreFile = File.createTempFile("test", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = TestScope(UnconfinedTestDispatcher() + Job())) {
            testDataStoreFile
        }
        val repository = RewardQuotaRepositoryImpl(dataStore)
        block(dataStore, repository)
        testDataStoreFile.delete()
    }

    @Test
    fun `grantedCountTodayFlow - initial is zero`() = runTestWithDataStore { _, repository ->
        val count = repository.grantedCountTodayFlow().first()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `tryGrantOncePerDay - grants on first try`() = runTestWithDataStore { _, repository ->
        val granted = repository.tryGrantOncePerDay()
        assertThat(granted).isTrue()
        val count = repository.grantedCountTodayFlow().first()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `tryGrantOncePerDay - fails on second try on same day`() = runTestWithDataStore { _, repository ->
        // First try should succeed
        repository.tryGrantOncePerDay()

        // Second try should fail
        val granted = repository.tryGrantOncePerDay()
        assertThat(granted).isFalse()
        val count = repository.grantedCountTodayFlow().first()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `tryGrantOncePerDay - grants again on a new day`() = runTestWithDataStore { dataStore, repository ->
        // Arrange: Grant once for a past day
        val lastDayKey = longPreferencesKey("reward_last_day")
        val countKey = intPreferencesKey("reward_count_today")
        dataStore.edit {
            it[lastDayKey] = 20230101L
            it[countKey] = 1
        }

        // Act: Try to grant for today
        val granted = repository.tryGrantOncePerDay()

        // Assert: Should succeed and count should reset to 1
        assertThat(granted).isTrue()
        val count = repository.grantedCountTodayFlow().first()
        assertThat(count).isEqualTo(1)
    }
}
