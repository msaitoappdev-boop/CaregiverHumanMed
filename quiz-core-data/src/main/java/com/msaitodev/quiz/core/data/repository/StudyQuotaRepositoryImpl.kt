package com.msaitodev.quiz.core.data.repository

import com.msaitodev.quiz.core.domain.model.QuotaState
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyQuotaRepositoryImpl @Inject constructor(
    private val scoreRepository: ScoreRepository
) : StudyQuotaRepository {

    private fun todayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    override fun observe(freeDailySetsProvider: () -> Int): Flow<QuotaState> =
        scoreRepository.history().map { history ->
            val todayStr = todayKey()
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            
            // 今日の履歴件数をカウント
            val used = history.count { entry ->
                sdf.format(Date(entry.timestamp)) == todayStr
            }

            QuotaState(
                todayKey = todayStr,
                usedSets = used,
                freeDailySets = freeDailySetsProvider()
            )
        }

    override suspend fun markSetFinished() {
        // スコア履歴（ScoreRepository）への保存が別経路で行われるため、
        // ここでの直接的な DataStore 操作は不要になりました。
        // UI側の整合性を保つため、空の実装として残します。
    }
}
