package jp.msaitoappdev.caregiver.humanmed.data.repository

import jp.msaitoappdev.caregiver.humanmed.data.local.db.ScoreDao
import jp.msaitoappdev.caregiver.humanmed.data.mapper.toDomain
import jp.msaitoappdev.caregiver.humanmed.data.mapper.toEntity
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreRepositoryImpl @Inject constructor(
    private val dao: ScoreDao
) : ScoreRepository {

    override fun history(): Flow<List<ScoreEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(entry: ScoreEntry) {
        dao.insert(entry.toEntity())
    }

    override suspend fun clear() {
        dao.clear()
    }
}