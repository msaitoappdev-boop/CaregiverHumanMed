package com.msaitodev.quiz.core.domain.repository

import com.msaitodev.quiz.core.domain.model.QuestionStats
import kotlinx.coroutines.flow.Flow

/**
 * 問題ごとの学習統計（正誤記録）を管理するリポジトリ。
 * 弱点特訓機能の基盤データを提供する。
 */
interface WrongAnswerRepository {
    /** 全ての問題の統計データを取得する */
    val allStats: Flow<List<QuestionStats>>

    /** 特定の問題の統計データを取得する */
    fun getStats(questionId: String): Flow<QuestionStats?>

    /** 問題の正解を記録する */
    suspend fun recordCorrect(questionId: String)

    /** 問題の不正解を記録する */
    suspend fun recordIncorrect(questionId: String)

    /** 全ての統計データをリセットする */
    suspend fun clearAllStats()
}
