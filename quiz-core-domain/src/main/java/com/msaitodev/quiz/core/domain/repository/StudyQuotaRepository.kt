package com.msaitodev.quiz.core.domain.repository

import com.msaitodev.quiz.core.domain.model.QuotaState
import kotlinx.coroutines.flow.Flow

/**
 * 学習ノルマ（Quota）を管理するリポジトリ。
 * 1日の完了セット数などの「学習進捗」のみを責務とする。
 */
interface StudyQuotaRepository {
    /** 学習ノルマの状態を監視する */
    fun observe(freeDailySetsProvider: () -> Int): Flow<QuotaState>

    /** クイズセットが完了したことを記録する */
    suspend fun markSetFinished()
}
