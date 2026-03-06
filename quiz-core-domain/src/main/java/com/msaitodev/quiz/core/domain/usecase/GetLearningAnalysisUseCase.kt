package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.model.LearningAnalysis
import com.msaitodev.quiz.core.domain.model.TrendPeriod
import com.msaitodev.quiz.core.domain.repository.CategoryNameProvider
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.repository.WrongAnswerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 学習状況を多角的に分析し、サマリを提供するユースケース。
 */
class GetLearningAnalysisUseCase @Inject constructor(
    private val questionRepo: QuestionRepository,
    private val wrongAnswerRepo: WrongAnswerRepository,
    private val scoreRepo: ScoreRepository,
    private val categoryNameProvider: CategoryNameProvider
) {
    /**
     * 指定された期間に基づいた分析結果を取得する。
     */
    operator fun invoke(period: TrendPeriod): Flow<LearningAnalysis> {
        return combine(
            wrongAnswerRepo.allStats,
            scoreRepo.history()
        ) { stats, history ->
            val allQuestions = questionRepo.loadAll()
            
            // 1. 総合進捗の計算
            val solvedIds = stats.filter { it.correctCount + it.incorrectCount > 0 }.map { it.questionId }.toSet()
            val totalProgress = if (allQuestions.isEmpty()) 0f else solvedIds.size.toFloat() / allQuestions.size

            // 2. 分野別サマリの計算
            val statsMap = stats.associateBy { it.questionId }
            val categoryGroups = allQuestions.groupBy { it.category }
            
            val categorySummaries = categoryGroups.map { (catId, questions) ->
                val catStats = questions.mapNotNull { statsMap[it.id] }
                val solved = catStats.sumOf { it.correctCount + it.incorrectCount }
                val correct = catStats.sumOf { it.correctCount }
                
                LearningAnalysis.CategorySummary(
                    categoryId = catId,
                    categoryName = categoryNameProvider.getDisplayName(catId),
                    accuracyRate = if (solved == 0) 0f else correct.toFloat() / solved,
                    solvedCount = solved,
                    totalInOffset = questions.size
                )
            }.sortedByDescending { it.accuracyRate }

            // 3. トレンドの計算 (日・週・月)
            val trendData = calculateTrend(history, period)

            // 4. 学習継続カレンダー用の日付抽出 (00:00:00 Unix Timestamp のリスト)
            val studiedDays = history.map { entry ->
                Calendar.getInstance().apply {
                    timeInMillis = entry.timestamp
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }.distinct().sorted()

            // 5. 連続学習日数（ストリーク）の計算
            val currentStreak = calculateStreak(studiedDays)

            // 6. 総評の生成
            val overallComment = generateComment(totalProgress, categorySummaries)

            LearningAnalysis(
                totalProgress = totalProgress,
                categorySummaries = categorySummaries,
                dailyTrend = trendData,
                overallComment = overallComment,
                studiedDays = studiedDays,
                currentStreak = currentStreak
            )
        }
    }

    private fun calculateStreak(studiedDays: List<Long>): Int {
        if (studiedDays.isEmpty()) return 0
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        // 最後に学習した日が今日か昨日でない場合はストリーク終了
        val lastStudiedDay = studiedDays.last()
        if (lastStudiedDay != today && lastStudiedDay != yesterday) return 0

        var streak = 0
        val calendar = Calendar.getInstance().apply { timeInMillis = lastStudiedDay }
        val studiedSet = studiedDays.toSet()

        while (studiedSet.contains(calendar.timeInMillis)) {
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun calculateTrend(history: List<com.msaitodev.quiz.core.domain.model.ScoreEntry>, period: TrendPeriod): List<LearningAnalysis.DailyScore> {
        val dateFormat = when (period) {
            TrendPeriod.DAILY -> SimpleDateFormat("MM/dd", Locale.US)
            TrendPeriod.WEEKLY -> SimpleDateFormat("W'週目'", Locale.US)
            TrendPeriod.MONTHLY -> SimpleDateFormat("M'月'", Locale.US)
        }

        val grouped = history.groupBy { entry ->
            val cal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
            when (period) {
                TrendPeriod.DAILY -> dateFormat.format(Date(entry.timestamp))
                TrendPeriod.WEEKLY -> "${cal.get(Calendar.YEAR)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.WEEK_OF_MONTH)}"
                TrendPeriod.MONTHLY -> "${cal.get(Calendar.YEAR)}/${cal.get(Calendar.MONTH) + 1}"
            }
        }

        return grouped.map { (key, entries) ->
            val label = when (period) {
                TrendPeriod.DAILY -> key
                TrendPeriod.WEEKLY -> {
                    val parts = key.split("/")
                    "${parts[1]}月${parts[2]}週"
                }
                TrendPeriod.MONTHLY -> {
                    val parts = key.split("/")
                    "${parts[1]}月"
                }
            }
            LearningAnalysis.DailyScore(
                dateLabel = label,
                averageAccuracy = entries.map { it.percent / 100f }.average().toFloat()
            )
        }.let {
            when (period) {
                TrendPeriod.DAILY -> it.takeLast(7)
                TrendPeriod.WEEKLY -> it.takeLast(4)
                TrendPeriod.MONTHLY -> it.takeLast(6)
            }
        }
    }

    private fun generateComment(progress: Float, summaries: List<LearningAnalysis.CategorySummary>): String {
        if (progress == 0f) return "まずはクイズを開始して、学習の第一歩を踏み出しましょう！"
        
        val worstCategory = summaries.filter { it.solvedCount > 0 }.minByOrNull { it.accuracyRate }
        
        return if (worstCategory != null) {
            "${worstCategory.categoryName}の正解率が低めです。「弱点特訓」で間違えた問題を優先的に克服しましょう。"
        } else {
            "順調に学習が進んでいます！この調子で全問制覇を目指しましょう。"
        }
    }
}
