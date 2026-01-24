package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository

class SaveScoreUseCase(private val repo: ScoreRepository) {
    suspend operator fun invoke(entry: ScoreEntry) = repo.add(entry)
}
