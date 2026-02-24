package jp.msaitoappdev.caregiver.humanmed.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RewardQuotaRepositoryImplTest {

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testDataStoreFile = testContext.preferencesDataStoreFile("test_reward_quota_prefs")

    @Test
    fun tryGrantOncePerDay_grantsOnceAndThenRejects() = runTest {
        // GIVEN: runTestのスコープ内でDataStoreとRepositoryを初期化
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = this, // runTestが提供するTestScopeを使用
            produceFile = { testDataStoreFile }
        )
        val repository = RewardQuotaRepositoryImpl(testDataStore)

        // WHEN & THEN
        repository.grantedCountTodayFlow().test {
            assertThat(awaitItem()).isEqualTo(0)

            // 1回目の付与を試みる
            val firstAttempt = repository.tryGrantOncePerDay()
            assertThat(firstAttempt).isTrue()
            assertThat(awaitItem()).isEqualTo(1)

            // 2回目の付与を試みる
            val secondAttempt = repository.tryGrantOncePerDay()
            assertThat(secondAttempt).isFalse()
            expectNoEvents() // 新しい放射がないことを確認

            cancelAndIgnoreRemainingEvents()
        }

        // Cleanup
        coroutineContext.cancelChildren() // Cancel the datastore scope
        if (testDataStoreFile.exists()) {
            testDataStoreFile.delete()
        }
    }
}
