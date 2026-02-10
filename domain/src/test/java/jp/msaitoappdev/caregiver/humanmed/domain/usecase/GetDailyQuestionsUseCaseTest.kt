package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*

class GetDailyQuestionsUseCaseTest {

    private val mockRepository = mock(QuestionRepository::class.java)
    private val useCase = GetDailyQuestionsUseCase(mockRepository)

    @Test
    fun `test get daily questions`() = runBlocking {
        // Arrange
        val mockQuestions = listOf(/* Add mock Question data here */)
        `when`(mockRepository.getDailyQuestions()).thenReturn(mockQuestions)

        // Act
        val result = useCase.getDailyQuestions()

        // Assert
        assertNotNull(result)
        verify(mockRepository, times(1)).getDailyQuestions()
    }
}
