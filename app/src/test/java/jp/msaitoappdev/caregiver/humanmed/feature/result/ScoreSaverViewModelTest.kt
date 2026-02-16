package jp.msaitoappdev.caregiver.humanmed.feature.result

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.SaveScoreUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class ScoreSaverViewModelTest {

    private lateinit var viewModel: ScoreSaverViewModel
    private val saveScoreUseCase: SaveScoreUseCase = mock()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScoreSaverViewModel(saveScoreUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `save calls use case`() = runTest {
        // Arrange
        val scoreEntry = ScoreEntry(timestamp = 1L, score = 2, total = 3, percent = 66)

        // Act
        viewModel.save(scoreEntry)

        // Assert
        verify(saveScoreUseCase).invoke(scoreEntry)
    }
}
