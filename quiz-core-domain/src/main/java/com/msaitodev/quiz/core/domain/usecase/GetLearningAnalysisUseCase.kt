package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.core.common.config.AppAssetConfig
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
import kotlin.math.roundToInt

/**
 * 学習状況を多角的に分析し、サマリを提供するユースケース。
 */
class GetLearningAnalysisUseCase @Inject constructor(
    private val questionRepo: QuestionRepository,
    private val wrongAnswerRepo: WrongAnswerRepository,
    private val scoreRepo: ScoreRepository,
    private val categoryNameProvider: CategoryNameProvider,
    private val appAssetConfig: AppAssetConfig
) {
    /**
     * 指定された期間に基づいた分析結果を取得する。
     */
    operator fun invoke(period: TrendPeriod): Flow<LearningAnalysis> {
        return combine(
            wrongAnswerRepo.allStats,
            scoreRepo.history()
        ) { stats, history ->
            // JSONから動的に全問題をロード（問題数が増えても自動追従）
            val allQuestions = questionRepo.loadAll()
            val poolSize = allQuestions.size
            
            // 1. 総合進捗の計算 (動的な全問題数を使用)
            val solvedIds = stats.filter { it.correctCount + it.incorrectCount > 0 }.map { it.questionId }.toSet()
            val totalProgress = if (poolSize == 0) 0f else solvedIds.size.toFloat() / poolSize

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

            // 4. 学習継続カレンダー用の日付抽出
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

            // 6. 本試験推定スコアの計算 (本試験形式の定数を使用)
            // 設定がない場合は現在の問題数を分母とする
            val targetExamQuestions = if (appAssetConfig.totalExamQuestions > 0) {
                appAssetConfig.totalExamQuestions
            } else {
                poolSize
            }
            val predictedScore = calculatePredictedScore(categorySummaries, totalProgress, targetExamQuestions)

            // 7. 総評の生成
            val overallComment = generateComment(totalProgress, categorySummaries, predictedScore, targetExamQuestions)

            LearningAnalysis(
                totalProgress = totalProgress,
                categorySummaries = categorySummaries,
                dailyTrend = trendData,
                overallComment = overallComment,
                studiedDays = studiedDays,
                currentStreak = currentStreak,
                predictedScore = predictedScore,
                totalExamQuestions = targetExamQuestions
            )
        }
    }

    private fun calculatePredictedScore(
        summaries: List<LearningAnalysis.CategorySummary>, 
        progress: Float,
        totalQuestions: Int
    ): Int {
        if (summaries.isEmpty() || progress == 0f || totalQuestions == 0) return 0
        
        val solvedSummaries = summaries.filter { it.solvedCount > 0 }
        if (solvedSummaries.isEmpty()) return 0
        
        val avgAccuracy = solvedSummaries.map { it.accuracyRate }.average().toFloat()
        
        // 保守的見積もりロジック: 未学習分野は現在の実力の70%程度と仮定
        val scoreWeight = (avgAccuracy * progress) + (avgAccuracy * 0.7f * (1f - progress))
        
        return (scoreWeight * totalQuestions).roundToInt().coerceIn(0, totalQuestions)
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

    private fun generateComment(
        progress: Float, 
        summaries: List<LearningAnalysis.CategorySummary>, 
        predictedScore: Int,
        totalQuestions: Int
    ): String {
        if (progress == 0f) return "まずはクイズを開始して、学習の第一歩を踏み出しましょう！"
        
        val passingThreshold = appAssetConfig.passingScoreThreshold
        val passingScore = (totalQuestions * passingThreshold).roundToInt()
        
        if (predictedScore >= passingScore) {
            return "素晴らしい！現在の推定スコアは合格圏内です。この調子で苦手分野をゼロにしていきましょう。"
        }
        
        val worstCategory = summaries.filter { it.solvedCount > 0 }.minByOrNull { it.accuracyRate }
        
        return if (worstCategory != null && worstCategory.accuracyRate < passingThreshold) {
            "${worstCategory.categoryName}の克服が合格への近道です。「弱点特訓」を活用してみましょう。"
        } else if (progress < 0.5f) {
            "学習範囲を広げることで、さらに推定スコアが向上します。全問制覇を目指しましょう！"
        } else {
            "順調に学習が進んでいます。あと少しで合格圏内です！"
        }
    }
}
