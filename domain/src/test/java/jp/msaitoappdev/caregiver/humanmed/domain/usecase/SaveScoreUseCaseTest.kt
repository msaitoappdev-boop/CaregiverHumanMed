package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.*

class SaveScoreUseCaseTest {

    private val mockRepository = mock(ScoreRepository::class.java)
    private val useCase = SaveScoreUseCase(mockRepository)

    @Test
    fun `test save score`() = runBlocking {
        // Arrange
        val scoreEntry = ScoreEntry(/* Add mock data here */)

        // Act
        useCase.save(scoreEntry)

        // Assert
        verify(mockRepository, times(1)).save(scoreEntry)
    }
}
