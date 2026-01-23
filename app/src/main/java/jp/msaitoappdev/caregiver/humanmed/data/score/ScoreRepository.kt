package jp.msaitoappdev.caregiver.humanmed.data.score

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val dao: ScoreDao) {
    val history: Flow<List<ScoreRecord>> = dao.observeAll()
    suspend fun add(record: ScoreRecord) = dao.insert(record)
    suspend fun clear() = dao.clear()
}
