package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class ObserveScoresUseCaseTest {

    private val mockRepository = mock(ScoreRepository::class.java)
    private val useCase = ObserveScoresUseCase(mockRepository)

    @Test
    fun `invoke returns flow of score history`() {
        runBlocking {
            // Arrange
            val mockHistory = listOf(
                ScoreEntry(id = 1L, timestamp = 100L, score = 10, total = 10, percent = 100),
                ScoreEntry(id = 2L, timestamp = 200L, score = 8, total = 10, percent = 80)
            )
            val mockFlow = flowOf(mockHistory)
            `when`(mockRepository.history()).thenReturn(mockFlow)

            // Act
            val resultFlow = useCase()

            // Assert
            assertEquals(mockHistory, resultFlow.first())
            verify(mockRepository, times(1)).history()
        }
    }
}
