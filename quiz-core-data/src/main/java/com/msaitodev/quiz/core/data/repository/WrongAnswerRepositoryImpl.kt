package com.msaitodev.quiz.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.msaitodev.quiz.core.domain.model.QuestionStats
import com.msaitodev.quiz.core.domain.repository.WrongAnswerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WrongAnswerRepositoryImpl @Inject constructor(
    private val store: DataStore<Preferences>
) : WrongAnswerRepository {

    private fun correctKey(id: String) = intPreferencesKey("q_correct_$id")
    private fun incorrectKey(id: String) = intPreferencesKey("q_incorrect_$id")
    private fun lastTimeKey(id: String) = longPreferencesKey("q_last_time_$id")

    override val allStats: Flow<List<QuestionStats>> = store.data.map { pref ->
        // 全てのキーをスキャンして、問題統計情報を抽出
        val statsMap = mutableMapOf<String, MutableStats>()
        pref.asMap().forEach { (key, value) ->
            val name = key.name
            val id = when {
                name.startsWith("q_correct_") -> name.removePrefix("q_correct_")
                name.startsWith("q_incorrect_") -> name.removePrefix("q_incorrect_")
                name.startsWith("q_last_time_") -> name.removePrefix("q_last_time_")
                else -> null
            }
            if (id != null) {
                val stats = statsMap.getOrPut(id) { MutableStats(id) }
                when {
                    name.startsWith("q_correct_") -> stats.correct = value as? Int ?: 0
                    name.startsWith("q_incorrect_") -> stats.incorrect = value as? Int ?: 0
                    name.startsWith("q_last_time_") -> stats.lastTime = value as? Long ?: 0L
                }
            }
        }
        statsMap.values.map { it.toImmutable() }
    }

    override fun getStats(questionId: String): Flow<QuestionStats?> = store.data.map { pref ->
        val correct = pref[correctKey(questionId)] ?: 0
        val incorrect = pref[incorrectKey(questionId)] ?: 0
        val lastTime = pref[lastTimeKey(questionId)] ?: 0L
        if (correct == 0 && incorrect == 0) null
        else QuestionStats(questionId, correct, incorrect, lastTime)
    }

    override suspend fun recordCorrect(questionId: String) {
        store.edit { p ->
            val current = p[correctKey(questionId)] ?: 0
            p[correctKey(questionId)] = current + 1
            p[lastTimeKey(questionId)] = System.currentTimeMillis()
        }
    }

    override suspend fun recordIncorrect(questionId: String) {
        store.edit { p ->
            val current = p[incorrectKey(questionId)] ?: 0
            p[incorrectKey(questionId)] = current + 1
            p[lastTimeKey(questionId)] = System.currentTimeMillis()
        }
    }

    override suspend fun clearAllStats() {
        store.edit { p ->
            val keysToRemove = p.asMap().keys.filter { 
                it.name.startsWith("q_") 
            }
            keysToRemove.forEach { p.remove(it) }
        }
    }

    private class MutableStats(val id: String) {
        var correct: Int = 0
        var incorrect: Int = 0
        var lastTime: Long = 0L
        fun toImmutable() = QuestionStats(id, correct, incorrect, lastTime)
    }
}
