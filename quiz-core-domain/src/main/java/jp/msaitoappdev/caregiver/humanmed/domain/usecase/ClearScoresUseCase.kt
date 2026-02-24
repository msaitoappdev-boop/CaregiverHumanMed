package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository

class ClearScoresUseCase(private val repo: ScoreRepository) {
    suspend operator fun invoke() = repo.clear()
}
