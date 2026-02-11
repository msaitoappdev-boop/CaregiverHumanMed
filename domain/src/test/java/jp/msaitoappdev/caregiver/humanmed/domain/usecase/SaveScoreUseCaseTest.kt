package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class SaveScoreUseCaseTest {

    private val mockRepository = mock(ScoreRepository::class.java)
    private val useCase = SaveScoreUseCase(mockRepository)

    @Test
    fun `invoke calls repository's add method`() {
        runBlocking {
            // Arrange
            val scoreEntry = ScoreEntry(
                timestamp = System.currentTimeMillis(),
                score = 8,
                total = 10,
                percent = 80
            )

            // Act
            useCase(scoreEntry)

            // Assert
            verify(mockRepository, times(1)).add(scoreEntry)
        }
    }
}
