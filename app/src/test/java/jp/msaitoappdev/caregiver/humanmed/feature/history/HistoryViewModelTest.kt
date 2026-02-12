package jp.msaitoappdev.caregiver.humanmed.feature.history

import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ClearScoresUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ObserveScoresUseCase
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
class HistoryViewModelTest {

    private val observeScoresUseCase: ObserveScoresUseCase = mockk()
    private val clearScoresUseCase: ClearScoresUseCase = mockk(relaxed = true)
    private lateinit var viewModel: HistoryViewModel

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
    fun `observe calls use case and returns its flow`() = runTest {
        // Arrange
        val expectedFlow = flowOf(listOf(ScoreEntry(id = 1L, timestamp = 1L, score = 1, total = 3, percent = 33)))
        every { observeScoresUseCase() } returns expectedFlow
        viewModel = HistoryViewModel(observeScoresUseCase, clearScoresUseCase)

        // Act
        val actualFlow = viewModel.observe()

        // Assert
        verify { observeScoresUseCase() }
        assertThat(actualFlow).isEqualTo(expectedFlow)
    }

    @Test
    fun `clear calls use case`() = runTest {
        // Arrange
        viewModel = HistoryViewModel(observeScoresUseCase, clearScoresUseCase)

        // Act
        viewModel.clear()

        // Assert
        coVerify { clearScoresUseCase() }
    }
}
