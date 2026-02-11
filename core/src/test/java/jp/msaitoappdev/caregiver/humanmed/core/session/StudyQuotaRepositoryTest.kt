package jp.msaitoappdev.caregiver.humanmed.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import jp.msaitoappdev.caregiver.humanmed.core.session.StudyQuotaPrefs as P

class StudyQuotaRepositoryTest {

    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var repository: StudyQuotaRepository

    private val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    private val preferencesFlow = MutableStateFlow(emptyPreferences())

    @Before
    fun setUp() {
        mockDataStore = mockk<DataStore<Preferences>> {
            coEvery { data } returns preferencesFlow
            coEvery { updateData(any()) } coAnswers { invocation ->
                val updateBlock = invocation.invocation.args[0] as suspend (Preferences) -> Preferences
                val currentPrefs = preferencesFlow.value
                val newPrefs = updateBlock(currentPrefs)
                preferencesFlow.value = newPrefs
                newPrefs
            }
        }
        repository = StudyQuotaRepository(mockDataStore)
    }

    @Test
    fun `observe returns correct QuotaState when data exists for today`() = runTest {
        val initialPrefs = emptyPreferences().toMutablePreferences()
        initialPrefs[P.TODAY_KEY] = todayKey
        initialPrefs[P.USED_SETS] = 2
        initialPrefs[P.REWARDED_GRANTED] = 1
        preferencesFlow.value = initialPrefs.toPreferences()

        val quotaState = repository.observe { 5 }.first()

        assertEquals(todayKey, quotaState.todayKey)
        assertEquals(2, quotaState.usedSets)
        assertEquals(1, quotaState.rewardedGranted)
        assertEquals(5, quotaState.freeDailySets)
        assertEquals(6, quotaState.totalAllowance)
        assertTrue(quotaState.canStart)
    }

    @Test
    fun `observe resets state when date changes`() = runTest {
        val initialPrefs = emptyPreferences().toMutablePreferences()
        initialPrefs[P.TODAY_KEY] = "20230101" // 過去の日付
        initialPrefs[P.USED_SETS] = 10
        initialPrefs[P.REWARDED_GRANTED] = 5
        preferencesFlow.value = initialPrefs.toPreferences()

        val quotaState = repository.observe { 3 }.first()

        assertEquals(todayKey, quotaState.todayKey)
        assertEquals(0, quotaState.usedSets)
        assertEquals(0, quotaState.rewardedGranted)
        assertEquals(3, quotaState.freeDailySets)
    }

    @Test
    fun `markSetFinished increments used sets`() = runTest {
        val initialPrefs = emptyPreferences().toMutablePreferences()
        initialPrefs[P.TODAY_KEY] = todayKey
        initialPrefs[P.USED_SETS] = 1
        preferencesFlow.value = initialPrefs.toPreferences()

        repository.markSetFinished()

        val finalState = repository.observe { 0 }.first()
        assertEquals(2, finalState.usedSets)
    }

    @Test
    fun `grantByReward increments rewarded grants`() = runTest {
        val initialPrefs = emptyPreferences().toMutablePreferences()
        initialPrefs[P.TODAY_KEY] = todayKey
        initialPrefs[P.REWARDED_GRANTED] = 0
        preferencesFlow.value = initialPrefs.toPreferences()

        repository.grantByReward()

        val finalState = repository.observe { 0 }.first()
        assertEquals(1, finalState.rewardedGranted)
    }
}
