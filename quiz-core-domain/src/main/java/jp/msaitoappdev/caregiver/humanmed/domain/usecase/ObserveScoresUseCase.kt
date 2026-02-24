package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow

class ObserveScoresUseCase(private val repo: ScoreRepository) {
    operator fun invoke(): Flow<List<ScoreEntry>> = repo.history()
}
