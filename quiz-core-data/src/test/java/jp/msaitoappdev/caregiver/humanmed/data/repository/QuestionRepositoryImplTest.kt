package jp.msaitoappdev.caregiver.humanmed.data.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.ByteArrayInputStream

class QuestionRepositoryImplTest {

    private val testJson = """
    [
      {
        "id": "test_q1",
        "text": "This is a test question.",
        "options": ["A", "B", "C"],
        "correctIndex": 0,
        "explanation": "This is an explanation."
      }
    ]
    """

    @Test
    fun `loadAll parses json and returns questions`() = runTest {
        // GIVEN: テスト用のJSON文字列を返す、偽のContextとAssetManagerを準備
        val fakeInputStream = ByteArrayInputStream(testJson.toByteArray())
        val fakeAssetManager = mock<android.content.res.AssetManager> {
            on { open(org.mockito.kotlin.any()) } doReturn fakeInputStream
        }
        val fakeContext = mock<Context> {
            on { assets } doReturn fakeAssetManager
        }

        // WHEN: Repositoryのインスタンスを作成し、loadAllを呼び出す
        val repository = QuestionRepositoryImpl(fakeContext)
        val questions = repository.loadAll()

        // THEN: JSONが正しくパースされ、Questionオブジェクトのリストが返されることを確認
        assertThat(questions).hasSize(1)
        val question = questions[0]
        assertThat(question.id).isEqualTo("test_q1")
        assertThat(question.text).isEqualTo("This is a test question.")
        assertThat(question.options).containsExactly("A", "B", "C").inOrder()
    }
}
