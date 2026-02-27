package jp.msaitoappdev.caregiver.humanmed.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.data.local.datastore.StudyQuotaPrefs
import com.msaitodev.quiz.core.data.repository.StudyQuotaRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class StudyQuotaRepositoryImplTest {

    private fun runTestWithDataStore(block: suspend (DataStore<Preferences>, StudyQuotaRepositoryImpl) -> Unit) = runTest {
        val testDataStoreFile = File.createTempFile("test", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = TestScope(UnconfinedTestDispatcher() + Job())) {
            testDataStoreFile
        }
        val repository = StudyQuotaRepositoryImpl(dataStore)
        block(dataStore, repository)
        testDataStoreFile.delete()
    }

    @Test
    fun `observe - initial state is correct`() = runTestWithDataStore { _, repository ->
        // Act
        val state = repository.observe { 5 }.first()

        // Assert
        assertThat(state.usedSets).isEqualTo(0)
        assertThat(state.rewardedGranted).isEqualTo(0)
        assertThat(state.freeDailySets).isEqualTo(5)
        assertThat(state.canStart).isTrue()
    }

    @Test
    fun `markSetFinished - increments used sets on the same day`() = runTestWithDataStore { _, repository ->
        // Act
        repository.markSetFinished()
        repository.markSetFinished()
        val state = repository.observe { 5 }.first()

        // Assert
        assertThat(state.usedSets).isEqualTo(2)
        assertThat(state.rewardedGranted).isEqualTo(0)
        assertThat(state.canStart).isTrue()
    }

    @Test
    fun `grantByReward - increments rewarded granted on the same day`() = runTestWithDataStore { _, repository ->
        // Act
        repository.grantByReward()
        val state = repository.observe { 5 }.first()

        // Assert
        assertThat(state.usedSets).isEqualTo(0)
        assertThat(state.rewardedGranted).isEqualTo(1)
        assertThat(state.canStart).isTrue()
    }

    @Test
    fun `markSetFinished - resets counters on a new day`() = runTestWithDataStore { dataStore, repository ->
        // Arrange: Set a state for a past day
        val yesterdayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
        dataStore.edit {
            it[StudyQuotaPrefs.TODAY_KEY] = yesterdayKey
            it[StudyQuotaPrefs.USED_SETS] = 3
            it[StudyQuotaPrefs.REWARDED_GRANTED] = 1
        }

        // Act: Mark finished for today
        repository.markSetFinished()
        val state = repository.observe { 5 }.first()

        // Assert: used sets are reset to 1, rewarded is reset to 0
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        assertThat(state.todayKey).isEqualTo(todayKey)
        assertThat(state.usedSets).isEqualTo(1)
        assertThat(state.rewardedGranted).isEqualTo(0)
    }
}
