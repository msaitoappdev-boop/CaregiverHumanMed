package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import com.msaitodev.quiz.core.domain.repository.WrongAnswerRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 弱点特訓のための問題を抽出するユースケース。
 * 過去の正誤統計に基づき、誤答率の高い問題を優先的に取得する。
 * 弱点データがない場合は、通常のランダム出題にフォールバックする。
 */
class GetWeaknessQuestionsUseCase @Inject constructor(
    private val questionRepo: QuestionRepository,
    private val wrongAnswerRepo: WrongAnswerRepository
) {
    /**
     * @param count 抽出する問題数
     * @param categoryFilter 特定のカテゴリに絞る場合
     * @param excludingIds すでに出題済みの問題ID
     */
    suspend fun execute(
        count: Int = 3,
        categoryFilter: String? = null,
        excludingIds: Set<String> = emptySet()
    ): List<Question> {
        val allQuestions = questionRepo.loadAll()
        val allStats = wrongAnswerRepo.allStats.first()

        // 統計データを ID をキーにしたマップに変換
        val statsMap = allStats.associateBy { it.questionId }

        // 1. 弱点候補（1回以上間違えているもの）を抽出
        val weaknessCandidates = allQuestions
            .filter { categoryFilter == null || it.category == categoryFilter }
            .filterNot { it.id in excludingIds }
            .filter { question ->
                val stats = statsMap[question.id]
                stats != null && stats.incorrectCount > 0
            }
            .sortedWith(compareByDescending<Question> { question ->
                statsMap[question.id]?.errorRate ?: 0f
            }.thenByDescending { question ->
                statsMap[question.id]?.lastAnsweredTimestamp ?: 0L
            })
            .take(count)
            .toList()

        // 2. 弱点が足りない場合は、全問題からランダムに補充（仕様5：フラットに出題）
        return if (weaknessCandidates.size < count) {
            val needed = count - weaknessCandidates.size
            val excludeForFilling = excludingIds + weaknessCandidates.map { it.id }.toSet()
            
            // 補充用の問題をランダムに取得
            val fillingQuestions = questionRepo.getRandomUnseenQuestions(needed, excludeForFilling)
            weaknessCandidates + fillingQuestions
        } else {
            weaknessCandidates
        }
    }
}
