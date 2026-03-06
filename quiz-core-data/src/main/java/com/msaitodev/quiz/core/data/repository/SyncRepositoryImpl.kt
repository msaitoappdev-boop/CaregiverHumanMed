package com.msaitodev.quiz.core.data.repository

import com.msaitodev.core.cloudsync.CloudSyncClient
import com.msaitodev.quiz.core.domain.model.QuestionStats
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.repository.SyncRepository
import com.msaitodev.quiz.core.domain.repository.WrongAnswerRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val cloudSyncClient: CloudSyncClient,
    private val premiumRepository: PremiumRepository,
    private val wrongAnswerRepository: WrongAnswerRepository,
    private val scoreRepository: ScoreRepository
) : SyncRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun uploadToCloud(): Boolean {
        if (!premiumRepository.isPremium.value) return false

        return try {
            val stats = wrongAnswerRepository.allStats.first()
            val statsJson = json.encodeToString(stats)
            
            val history = scoreRepository.history().first()
            val historyJson = json.encodeToString(history)

            val data = mapOf(
                "stats" to statsJson,
                "history" to historyJson,
                "updated_at" to System.currentTimeMillis()
            )
            
            cloudSyncClient.upload("quiz_sync", "latest", data)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun downloadFromCloud(): Boolean {
        if (!premiumRepository.isPremium.value) return false

        return try {
            val cloudData = cloudSyncClient.download("quiz_sync", "latest") ?: return false
            
            val cloudStatsJson = cloudData["stats"] as? String
            val cloudHistoryJson = cloudData["history"] as? String

            if (cloudStatsJson != null) {
                val cloudStats = json.decodeFromString<List<QuestionStats>>(cloudStatsJson)
                wrongAnswerRepository.syncStats(cloudStats)
            }

            if (cloudHistoryJson != null) {
                val cloudHistory = json.decodeFromString<List<ScoreEntry>>(cloudHistoryJson)
                mergeHistory(cloudHistory)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun mergeHistory(cloudHistory: List<ScoreEntry>) {
        val localHistory = scoreRepository.history().first().toSet()
        cloudHistory.forEach { cloud ->
            // スコア履歴は一意なIDがないため、オブジェクト全体の比較で重複を避けて追加
            if (!localHistory.contains(cloud)) {
                scoreRepository.add(cloud)
            }
        }
    }
}
