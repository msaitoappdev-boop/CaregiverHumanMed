package com.msaitodev.quiz.core.domain.repository

import com.msaitodev.quiz.core.domain.model.ScoreEntry
import kotlinx.coroutines.flow.Flow

interface ScoreRepository {
    fun history(): Flow<List<ScoreEntry>>
    suspend fun add(entry: ScoreEntry)
    suspend fun clear()
}
