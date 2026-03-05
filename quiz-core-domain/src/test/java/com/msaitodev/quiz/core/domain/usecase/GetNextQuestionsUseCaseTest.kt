package com.msaitodev.quiz.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetNextQuestionsUseCaseTest {

    private lateinit var questionRepository: QuestionRepository
    private lateinit var useCase: GetNextQuestionsUseCase

    @Before
    fun setup() {
        questionRepository = mock()
        useCase = GetNextQuestionsUseCase(questionRepository)
    }

    @Test
    fun `invoke - filters out seen questions and returns specified count`() = runTest {
        // GIVEN
        val allQuestions = listOf(
            Question("1", "cat", "Q1", emptyList(), 0, null),
            Question("2", "cat", "Q2", emptyList(), 0, null),
            Question("3", "cat", "Q3", emptyList(), 0, null),
            Question("4", "cat", "Q4", emptyList(), 0, null),
            Question("5", "cat", "Q5", emptyList(), 0, null)
        )
        val seenIds = setOf("1", "2")
        val expectedResult = allQuestions.filterNot { it.id in seenIds }.take(3)

        whenever(questionRepository.getRandomUnseenQuestions(3, seenIds)) doReturn expectedResult

        // WHEN
        val result = useCase(3, seenIds)

        // THEN
        assertThat(result).hasSize(3)
        assertThat(result).containsExactlyElementsIn(expectedResult)
    }
}
