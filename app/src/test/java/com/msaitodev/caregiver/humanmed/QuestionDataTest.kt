package com.msaitodev.caregiver.humanmed

import com.msaitodev.quiz.core.data.local.dto.QuestionDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * リリース前の問題データ整合性チェック。
 * assets 内の questions.json が全件正常にロード・パースできるかを検証する。
 */
class QuestionDataTest {

    @Test
    fun verifyAllQuestionsIntegrity() {
        // ユニットテスト実行時のカレントディレクトリからの assets パスを指定
        // 通常、モジュールルートからの相対パスになる
        val jsonFile = File("src/main/assets/questions.json")
        
        assertTrue("questions.json が見つかりません: ${jsonFile.absolutePath}", jsonFile.exists())

        val jsonText = jsonFile.readText()
        val json = Json { ignoreUnknownKeys = true }
        
        // 1. 全件パースチェック
        val questions = try {
            json.decodeFromString(ListSerializer(QuestionDto.serializer()), jsonText)
        } catch (e: Exception) {
            throw AssertionError("JSON のパースに失敗しました: ${e.message}")
        }

        assertFalse("問題データが空です", questions.isEmpty())
        println("検証開始: 合計 ${questions.size} 問")

        // 2. 各問題の整合性詳細チェック
        questions.forEachIndexed { index, q ->
            val context = "問題目 index:$index (ID:${q.id})"
            
            assertTrue("$context: IDが空です", q.id.isNotBlank())
            assertTrue("$context: 問題文が空です", q.text.isNotBlank())
            
            // 選択肢の数チェック（通常3〜5択を想定）
            assertTrue("$context: 選択肢が2つ未満です", q.options.size >= 2)
            
            // 正解インデックスの範囲チェック
            assertTrue(
                "$context: 正解インデックス (${q.correctIndex}) が範囲外です (options size: ${q.options.size})",
                q.correctIndex in 0 until q.options.size
            )
            
            // 選択肢のテキスト空チェック
            q.options.forEachIndexed { optIndex, opt ->
                assertTrue("$context: 選択肢 $optIndex が空です", opt.isNotBlank())
            }
        }
        
        println("検証完了: すべての問題データに異常はありませんでした。")
    }
}
