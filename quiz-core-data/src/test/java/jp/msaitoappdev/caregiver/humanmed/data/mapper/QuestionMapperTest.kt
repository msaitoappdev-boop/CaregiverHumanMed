package jp.msaitoappdev.caregiver.humanmed.data.mapper

import com.google.common.truth.Truth.assertThat
import jp.msaitoappdev.caregiver.humanmed.data.local.question.dto.QuestionDto
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import org.junit.Test

class QuestionMapperTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        // GIVEN: 変換元のQuestionDtoを作成
        val dto = QuestionDto(
            id = "q1",
            text = "test text",
            options = listOf("A", "B", "C"),
            correctIndex = 1,
            explanation = "test explanation"
        )

        // WHEN: toDomain()拡張関数を呼び出す
        val question = dto.toDomain()

        // THEN: 全てのフィールドが正しくマッピングされていることを確認
        assertThat(question.id).isEqualTo("q1")
        assertThat(question.text).isEqualTo("test text")
        assertThat(question.options).containsExactly("A", "B", "C").inOrder()
        assertThat(question.correctIndex).isEqualTo(1)
        assertThat(question.explanation).isEqualTo("test explanation")
    }
}
