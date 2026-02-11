package jp.msaitoappdev.caregiver.humanmed.domain.util

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyQuestionSelectorTest {

    private val selector = DailyQuestionSelector()

    @Test
    fun `select returns correct number of questions`() {
        // Arrange
        val questions = listOf(
            Question("id1", "text1", listOf("o1"), 0, "exp1"),
            Question("id2", "text2", listOf("o1"), 0, "exp2"),
            Question("id3", "text3", listOf("o1"), 0, "exp3"),
            Question("id4", "text4", listOf("o1"), 0, "exp4"),
            Question("id5", "text5", listOf("o1"), 0, "exp5")
        )
        val count = 3

        // Act
        val result = selector.select(questions, count)

        // Assert
        assertEquals(count, result.size)
        assertTrue(questions.containsAll(result))
    }

    @Test
    fun `select returns all questions when count is larger than list size`() {
        // Arrange
        val questions = listOf(
            Question("id1", "text1", listOf("o1"), 0, "exp1"),
            Question("id2", "text2", listOf("o1"), 0, "exp2")
        )
        val count = 5

        // Act
        val result = selector.select(questions, count)

        // Assert
        assertEquals(questions.size, result.size)
        assertEquals(questions.toSet(), result.toSet())
    }

    @Test
    fun `select returns empty list when input list is empty`() {
        // Arrange
        val questions = emptyList<Question>()
        val count = 3

        // Act
        val result = selector.select(questions, count)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `select returns empty list when count is zero`() {
        // Arrange
        val questions = listOf(
            Question("id1", "text1", listOf("o1"), 0, "exp1")
        )
        val count = 0

        // Act
        val result = selector.select(questions, count)

        // Assert
        assertTrue(result.isEmpty())
    }
}
