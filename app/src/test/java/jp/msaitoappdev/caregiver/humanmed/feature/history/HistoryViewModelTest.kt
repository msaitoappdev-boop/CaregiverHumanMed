package jp.msaitoappdev.caregiver.humanmed.feature.history

import com.msaitodev.quiz.feature.history.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ClearScoresUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ObserveScoresUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class HistoryViewModelTest {

    private val observeScoresUseCase: ObserveScoresUseCase = mock()
    private val clearScoresUseCase: ClearScoresUseCase = mock()

    private lateinit var viewModel: HistoryViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HistoryViewModel(
            observeScores = observeScoresUseCase,
            clearScores = clearScoresUseCase
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observe calls ObserveScoresUseCase`() {
        // WHEN
        viewModel.observe()

        // THEN
        verify(observeScoresUseCase).invoke()
    }

    @Test
    fun `clearHistory calls ClearScoresUseCase`() = runTest {
        // WHEN
        viewModel.clearHistory()
        testDispatcher.scheduler.advanceUntilIdle() // Process the launched coroutine

        // THEN
        verify(clearScoresUseCase).invoke()
    }
}
