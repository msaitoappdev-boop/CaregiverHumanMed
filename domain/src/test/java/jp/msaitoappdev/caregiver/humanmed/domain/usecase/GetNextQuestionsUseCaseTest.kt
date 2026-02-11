package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class GetNextQuestionsUseCaseTest {

    private val mockRepository = mock(QuestionRepository::class.java)
    private val useCase = GetNextQuestionsUseCase(mockRepository)

    @Test
    fun `invoke returns random unseen questions`() {
        runBlocking {
            // Arrange
            val count = 3
            val excludingIds = setOf("id1", "id2")
            val mockQuestions = listOf(
                Question("id3", "text3", listOf("o1", "o2"), 0, "exp3"),
                Question("id4", "text4", listOf("o1", "o2"), 1, "exp4"),
                Question("id5", "text5", listOf("o1", "o2"), 0, "exp5"),
            )

            `when`(mockRepository.getRandomUnseenQuestions(count, excludingIds)).thenReturn(mockQuestions)

            // Act
            val result = useCase(count, excludingIds)

            // Assert
            assertEquals(mockQuestions, result)
            verify(mockRepository, times(1)).getRandomUnseenQuestions(count, excludingIds)
        }
    }
}
