package com.msaitodev.quiz.core.domain.repository

import com.msaitodev.quiz.core.domain.model.QuestionStats
import kotlinx.coroutines.flow.Flow

/**
 * 間違えた問題の統計情報を管理するリポジトリ。
 */
interface WrongAnswerRepository {
    /** 全ての統計情報を取得 */
    val allStats: Flow<List<QuestionStats>>

    /** 特定の問題の統計情報を取得 */
    fun getStats(questionId: String): Flow<QuestionStats?>

    /** 正解を記録 */
    suspend fun recordCorrect(questionId: String)

    /** 不正解を記録 */
    suspend fun recordIncorrect(questionId: String)

    /** 全ての統計情報を削除 */
    suspend fun clearAllStats()

    /** クラウドデータとの同期（一括反映） */
    suspend fun syncStats(stats: List<QuestionStats>)
}
