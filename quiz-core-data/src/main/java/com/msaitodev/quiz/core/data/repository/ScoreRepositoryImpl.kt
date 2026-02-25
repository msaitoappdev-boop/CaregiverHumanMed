package com.msaitodev.quiz.core.data.repository

import com.msaitodev.quiz.core.data.local.db.ScoreDao
import com.msaitodev.quiz.core.data.mapper.toDomain
import com.msaitodev.quiz.core.data.mapper.toEntity
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
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