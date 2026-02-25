package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.ScoreRepository

class SaveScoreUseCase(private val repo: ScoreRepository) {
    suspend operator fun invoke(entry: ScoreEntry) = repo.add(entry)
}
