package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 学習履歴を監視するユースケース。
 * @param dateKey フィルタリングする日付 (yyyyMMdd)。null の場合は全期間。
 */
class ObserveScoresUseCase(private val repo: ScoreRepository) {
    operator fun invoke(dateKey: String? = null): Flow<List<ScoreEntry>> {
        return repo.history().map { history ->
            if (dateKey == null) {
                history
            } else {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                history.filter { entry ->
                    sdf.format(Date(entry.timestamp)) == dateKey
                }
            }
        }
    }
}
