package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.repository.ScoreRepository

class ClearScoresUseCase(private val repo: ScoreRepository) {
    suspend operator fun invoke() = repo.clear()
}
