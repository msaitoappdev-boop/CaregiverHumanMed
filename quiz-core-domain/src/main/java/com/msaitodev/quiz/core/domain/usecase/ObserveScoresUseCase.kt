package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow

class ObserveScoresUseCase(private val repo: ScoreRepository) {
    operator fun invoke(): Flow<List<ScoreEntry>> = repo.history()
}
