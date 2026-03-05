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
    val dailyTrend: List<DailyScore>, // ここは「トレンドデータ」全般を指す
    val overallComment: String
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
}
