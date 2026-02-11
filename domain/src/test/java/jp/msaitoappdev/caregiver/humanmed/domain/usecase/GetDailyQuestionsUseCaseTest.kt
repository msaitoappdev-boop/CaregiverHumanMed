package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import jp.msaitoappdev.caregiver.humanmed.domain.util.DailyQuestionSelector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class GetDailyQuestionsUseCaseTest {

    private val mockRepository = mock(QuestionRepository::class.java)
    private val mockSelector = mock(DailyQuestionSelector::class.java)
    private val useCase = GetDailyQuestionsUseCase(mockRepository, mockSelector)

    @Test
    fun `invoke returns selected questions from repository`() {
        runBlocking {
            // Arrange
            val allQuestions = listOf(
                Question("id1", "text1", listOf("o1", "o2"), 0, "exp1"),
                Question("id2", "text2", listOf("o1", "o2"), 1, "exp2"),
                Question("id3", "text3", listOf("o1", "o2"), 0, "exp3"),
                Question("id4", "text4", listOf("o1", "o2"), 1, "exp4"),
            )
            val selectedQuestions = allQuestions.take(3)
            val defaultCount = 3

            `when`(mockRepository.loadAll()).thenReturn(allQuestions)
            `when`(mockSelector.select(allQuestions, defaultCount)).thenReturn(selectedQuestions)

            // Act
            val result = useCase() // Use default parameter

            // Assert
            assertEquals(selectedQuestions, result)
            verify(mockRepository, times(1)).loadAll()
            verify(mockSelector, times(1)).select(allQuestions, defaultCount)
        }
    }
}
