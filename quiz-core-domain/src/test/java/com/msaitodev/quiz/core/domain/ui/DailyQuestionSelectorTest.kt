package com.msaitodev.quiz.core.domain.ui

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.domain.model.Question
import org.junit.Test

class DailyQuestionSelectorTest {

    private val selector = DailyQuestionSelector()

    private val testQuestions = listOf(
        Question("1", "cat", "Q1", emptyList(), 0, null),
        Question("2", "cat", "Q2", emptyList(), 0, null),
        Question("3", "cat", "Q3", emptyList(), 0, null),
        Question("4", "cat", "Q4", emptyList(), 0, null),
        Question("5", "cat", "Q5", emptyList(), 0, null)
    )

    @Test
    fun `select - returns correct number of questions`() {
        val result = selector.select(testQuestions, 3)
        assertThat(result).hasSize(3)
    }

    @Test
    fun `select - returns unique questions`() {
        val result = selector.select(testQuestions, 3)
        assertThat(result.distinctBy { it.id }).hasSize(3)
    }

    @Test
    fun `select - handles count larger than list`() {
        val result = selector.select(testQuestions, 10)
        assertThat(result).hasSize(5)
    }
}
