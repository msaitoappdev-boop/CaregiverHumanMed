package com.msaitodev.quiz.core.data.repository

import com.msaitodev.quiz.core.data.local.db.ScoreDao
import com.msaitodev.quiz.core.data.mapper.toDomain
import com.msaitodev.quiz.core.data.mapper.toEntity
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.repository.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ScoreRepositoryImpl @Inject constructor(
    private val dao: ScoreDao,
    private val syncRepositoryProvider: Provider<SyncRepository>
) : ScoreRepository {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun history(): Flow<List<ScoreEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(entry: ScoreEntry) {
        dao.insert(entry.toEntity())
        triggerSync()
    }

    override suspend fun clear() {
        dao.clear()
        triggerSync()
    }

    private fun triggerSync() {
        scope.launch {
            syncRepositoryProvider.get().uploadToCloud()
        }
    }
}
