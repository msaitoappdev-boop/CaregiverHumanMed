package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*

class GetNextQuestionsUseCaseTest {

    private val mockRepository = mock(QuestionRepository::class.java)
    private val useCase = GetNextQuestionsUseCase(mockRepository)

    @Test
    fun `test get next questions`() = runBlocking {
        // Arrange
        val mockQuestions = listOf(/* Add mock Question data here */)
        `when`(mockRepository.getNextQuestions()).thenReturn(mockQuestions)

        // Act
        val result = useCase.getNextQuestions()

        // Assert
        assertNotNull(result)
        verify(mockRepository, times(1)).getNextQuestions()
    }
}
