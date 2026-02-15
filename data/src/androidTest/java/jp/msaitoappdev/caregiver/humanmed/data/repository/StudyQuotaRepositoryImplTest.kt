package jp.msaitoappdev.caregiver.humanmed.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import jp.msaitoappdev.caregiver.humanmed.domain.model.QuotaState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class StudyQuotaRepositoryImplTest {

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testDataStoreFile = testContext.preferencesDataStoreFile("test_quota_prefs")

    @Test
    fun markSetFinished_incrementsUsedSetsCorrectly() = runTest {
        // GIVEN: runTestのスコープ内でDataStoreとRepositoryを初期化
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = this, // runTestが提供するTestScopeを使用
            produceFile = { testDataStoreFile }
        )
        val repository = StudyQuotaRepositoryImpl(testDataStore)

        repository.observe { 5 }.test {
            // THEN: initial state has 0 used sets
            val initialState = awaitItem()
            assertThat(initialState.usedSets).isEqualTo(0)

            // WHEN: mark one set as finished
            repository.markSetFinished()
            advanceUntilIdle() // Process pending coroutines

            // THEN: the new emission should have 1 used set
            val updatedState = awaitItem()
            assertThat(updatedState.usedSets).isEqualTo(1)

            // FINALLY: Cancel the collector because the flow never completes
            cancelAndIgnoreRemainingEvents()
        }

        // Cleanup
        coroutineContext.cancelChildren() // Cancel the datastore scope
        if (testDataStoreFile.exists()) {
            testDataStoreFile.delete()
        }
    }
}
