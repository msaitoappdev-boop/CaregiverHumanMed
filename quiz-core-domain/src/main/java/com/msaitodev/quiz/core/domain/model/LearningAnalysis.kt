package com.msaitodev.quiz.core.domain.model

/**
 * 学習分析の集計期間。
 */
enum class TrendPeriod {
    DAILY,   // 日別 (直近7日)
    WEEKLY,  // 週別 (直近4週)
    MONTHLY  // 月別 (直近6ヶ月)
}

/**
 * 学習分析の結果を保持するドメインモデル。
 */
data class LearningAnalysis(
    val totalProgress: Float,
    val categorySummaries: List<CategorySummary>,
    val dailyTrend: List<DailyScore>,
    val overallComment: String,
    val studiedDays: List<Long>, // 学習した日の 00:00:00 Unix タイムスタンプ
    val currentStreak: Int,      // 現在の連続学習日数
    val predictedScore: Int,     // 本試験推定スコア (0-125)
    val totalExamQuestions: Int = 125 // 本試験の総設問数
) {
    data class CategorySummary(
        val categoryId: String,
        val categoryName: String,
        val accuracyRate: Float,
        val solvedCount: Int,
        val totalInOffset: Int
    )

    data class DailyScore(
        val dateLabel: String,
        val averageAccuracy: Float
    )

    /**
     * 推定正解率を計算する (0.0 - 1.0)
     */
    val predictedAccuracyRate: Float = if (totalExamQuestions > 0) predictedScore.toFloat() / totalExamQuestions else 0f
}
