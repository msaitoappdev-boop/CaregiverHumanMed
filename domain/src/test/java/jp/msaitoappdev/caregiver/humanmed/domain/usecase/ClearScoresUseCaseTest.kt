package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class ClearScoresUseCaseTest {

    private val mockRepository = mock(ScoreRepository::class.java)
    private val useCase = ClearScoresUseCase(mockRepository)

    @Test
    fun `test clear scores`() {
        runBlocking {
            // Act
            useCase()

            // Assert
            verify(mockRepository, times(1)).clear()
        }
    }
}
