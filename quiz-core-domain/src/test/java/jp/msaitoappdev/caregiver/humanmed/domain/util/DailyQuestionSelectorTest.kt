package jp.msaitoappdev.caregiver.humanmed.domain.util

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.ui.DailyQuestionSelector
import org.junit.Test

class DailyQuestionSelectorTest {

    private val selector = DailyQuestionSelector()
    private val dummyQuestions = listOf(
        Question("q1", "", emptyList(), 0, ""),
        Question("q2", "", emptyList(), 0, ""),
        Question("q3", "", emptyList(), 0, ""),
        Question("q4", "", emptyList(), 0, ""),
        Question("q5", "", emptyList(), 0, "")
    )

    @Test
    fun `select returns correct count`() {
        // GIVEN a list of 5 questions
        // WHEN selecting 3
        val result = selector.select(dummyQuestions, 3)

        // THEN the result size should be 3
        assertThat(result).hasSize(3)
    }

    @Test
    fun `select with count larger than list size returns all items`() {
        // GIVEN a list of 5 questions
        // WHEN selecting 10
        val result = selector.select(dummyQuestions, 10)

        // THEN the result size should be 5 (all items)
        assertThat(result).hasSize(5)
    }

    @Test
    fun `select from empty list returns empty list`() {
        // GIVEN an empty list
        // WHEN selecting 3
        val result = selector.select(emptyList(), 3)

        // THEN the result should be empty
        assertThat(result).isEmpty()
    }

    @Test
    fun `select with zero count returns empty list`() {
        // GIVEN a list of 5 questions
        // WHEN selecting 0
        val result = selector.select(dummyQuestions, 0)

        // THEN the result should be empty
        assertThat(result).isEmpty()
    }
}
