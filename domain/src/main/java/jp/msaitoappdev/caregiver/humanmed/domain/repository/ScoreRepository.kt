package jp.msaitoappdev.caregiver.humanmed.domain.repository

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import kotlinx.coroutines.flow.Flow

interface ScoreRepository {
    fun history(): Flow<List<ScoreEntry>>
    suspend fun add(entry: ScoreEntry)
    suspend fun clear()
}
