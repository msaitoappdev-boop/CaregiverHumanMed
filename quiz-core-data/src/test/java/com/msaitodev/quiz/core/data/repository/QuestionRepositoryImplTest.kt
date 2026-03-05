package com.msaitodev.quiz.core.data.repository

import android.content.Context
import android.content.res.AssetManager
import com.google.common.truth.Truth.assertThat
import com.msaitodev.core.common.config.AppAssetConfig
import com.msaitodev.core.common.util.CryptoUtils
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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
    fun `loadAll parses encrypted bin and returns questions`() = runTest {
        // GIVEN: テスト用のJSONを暗号化したバイナリデータを作成
        val encryptedData = ByteArrayOutputStream().use { output ->
            CryptoUtils.encryptStream(ByteArrayInputStream(testJson.toByteArray()), output)
            output.toByteArray()
        }

        val fakeAssetManager = mock<AssetManager> {
            // 指定ディレクトリ配下に "test.bin" があるように見せる
            on { list("data") } doReturn arrayOf("test.bin")
            // ファイルがディレクトリでないことを示すために空配列を返す
            on { list("data/test.bin") } doReturn emptyArray()
            // 暗号化されたストリームを返す
            on { open("data/test.bin") } doReturn ByteArrayInputStream(encryptedData)
        }
        val fakeContext = mock<Context> {
            on { assets } doReturn fakeAssetManager
        }
        val fakeConfig = mock<AppAssetConfig> {
            on { assetDataDirectory } doReturn "data"
        }

        // WHEN: Repositoryのインスタンスを作成（新しいコンストラクタに対応）
        val repository = QuestionRepositoryImpl(fakeContext, fakeConfig)
        val questions = repository.loadAll()

        // THEN: 復号・パースされ、Questionオブジェクトが返されることを確認
        assertThat(questions).hasSize(1)
        val question = questions[0]
        assertThat(question.id).isEqualTo("test_q1")
        assertThat(question.text).isEqualTo("This is a test question.")
    }
}
