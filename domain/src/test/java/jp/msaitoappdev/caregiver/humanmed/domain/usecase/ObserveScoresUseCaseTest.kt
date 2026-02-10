package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.*

class ObserveScoresUseCaseTest {

    private val mockRepository = mock(ScoreRepository::class.java)
    private val useCase = ObserveScoresUseCase(mockRepository)

    @Test
    fun `test observe scores`() = runBlocking {
        // Arrange
        val mockFlow = flowOf(listOf(/* Add mock ScoreEntry data here */))
        `when`(mockRepository.observeScores()).thenReturn(mockFlow)

        // Act
        val result = useCase.observe()

        // Assert
        assertEquals(mockFlow, result)
        verify(mockRepository, times(1)).observeScores()
    }
}
