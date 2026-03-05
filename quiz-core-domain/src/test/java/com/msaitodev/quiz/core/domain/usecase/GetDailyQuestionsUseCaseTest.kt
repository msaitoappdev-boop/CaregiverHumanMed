package com.msaitodev.quiz.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import com.msaitodev.quiz.core.domain.ui.DailyQuestionSelector
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetDailyQuestionsUseCaseTest {

    private lateinit var questionRepository: QuestionRepository
    private lateinit var selector: DailyQuestionSelector
    private lateinit var useCase: GetDailyQuestionsUseCase

    @Before
    fun setup() {
        questionRepository = mock()
        selector = mock()
        useCase = GetDailyQuestionsUseCase(questionRepository, selector)
    }

    @Test
    fun `invoke - loads all questions and selects from them`() = runTest {
        // GIVEN
        val allQuestions = listOf(
            Question("1", "cat", "Q1", emptyList(), 0, null),
            Question("2", "cat", "Q2", emptyList(), 0, null)
        )
        val selected = listOf(allQuestions[0])
        
        whenever(questionRepository.loadAll()) doReturn allQuestions
        whenever(selector.select(allQuestions, 1)) doReturn selected

        // WHEN
        val result = useCase(1)

        // THEN
        assertThat(result).isEqualTo(selected)
    }
}
