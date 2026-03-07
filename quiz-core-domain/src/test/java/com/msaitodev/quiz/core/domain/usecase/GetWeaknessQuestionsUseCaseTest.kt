package com.msaitodev.quiz.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.model.QuestionStats
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import com.msaitodev.quiz.core.domain.repository.WrongAnswerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetWeaknessQuestionsUseCaseTest {

    private lateinit var questionRepo: QuestionRepository
    private lateinit var wrongAnswerRepo: WrongAnswerRepository
    private lateinit var useCase: GetWeaknessQuestionsUseCase

    private val questions = listOf(
        Question("q1", "cat1", "Text 1", emptyList(), 0, null),
        Question("q2", "cat1", "Text 2", emptyList(), 0, null),
        Question("q3", "cat2", "Text 3", emptyList(), 0, null),
        Question("q4", "cat2", "Text 4", emptyList(), 0, null)
    )

    @Before
    fun setup() {
        questionRepo = mock()
        wrongAnswerRepo = mock()
        useCase = GetWeaknessQuestionsUseCase(questionRepo, wrongAnswerRepo)
    }

    @Test
    fun `execute returns questions sorted by error rate descending`() = runTest {
        // GIVEN
        val stats = listOf(
            QuestionStats("q1", correctCount = 1, incorrectCount = 1, lastTime = 100L), // 50%
            QuestionStats("q2", correctCount = 0, incorrectCount = 2, lastTime = 100L), // 100%
            QuestionStats("q3", correctCount = 2, incorrectCount = 1, lastTime = 100L)  // 33%
        )
        whenever(questionRepo.loadAll()).thenReturn(questions)
        whenever(wrongAnswerRepo.allStats).thenReturn(flowOf(stats))

        // WHEN
        val result = useCase.execute(count = 3)

        // THEN
        assertThat(result).hasSize(3)
        assertThat(result[0].id).isEqualTo("q2") // 100%
        assertThat(result[1].id).isEqualTo("q1") // 50%
        assertThat(result[2].id).isEqualTo("q3") // 33%
    }

    @Test
    fun `execute tie-breaks with timestamp descending`() = runTest {
        // GIVEN
        val stats = listOf(
            QuestionStats("q1", correctCount = 0, incorrectCount = 1, lastTime = 100L),
            QuestionStats("q2", correctCount = 0, incorrectCount = 1, lastTime = 200L) // newer
        )
        whenever(questionRepo.loadAll()).thenReturn(questions)
        whenever(wrongAnswerRepo.allStats).thenReturn(flowOf(stats))

        // WHEN
        val result = useCase.execute(count = 2)

        // THEN
        assertThat(result[0].id).isEqualTo("q2")
        assertThat(result[1].id).isEqualTo("q1")
    }

    @Test
    fun `execute filters by category`() = runTest {
        // GIVEN
        val stats = listOf(
            QuestionStats("q1", correctCount = 0, incorrectCount = 1),
            QuestionStats("q3", correctCount = 0, incorrectCount = 1)
        )
        whenever(questionRepo.loadAll()).thenReturn(questions)
        whenever(wrongAnswerRepo.allStats).thenReturn(flowOf(stats))

        // WHEN
        val result = useCase.execute(categoryFilter = "cat2")

        // THEN
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("q3")
    }

    @Test
    fun `execute excludes questions with no incorrect answers`() = runTest {
        // GIVEN
        val stats = listOf(
            QuestionStats("q1", correctCount = 5, incorrectCount = 0), // positive only
            QuestionStats("q2", correctCount = 0, incorrectCount = 1)  // has error
        )
        whenever(questionRepo.loadAll()).thenReturn(questions)
        whenever(wrongAnswerRepo.allStats).thenReturn(flowOf(stats))

        // WHEN
        val result = useCase.execute()

        // THEN
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("q2")
    }
}
