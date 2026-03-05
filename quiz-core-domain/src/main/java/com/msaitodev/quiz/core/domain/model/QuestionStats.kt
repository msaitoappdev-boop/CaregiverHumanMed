package com.msaitodev.quiz.core.domain.model

/**
 * 個々の問題に対する学習統計データ。
 */
data class QuestionStats(
    val questionId: String,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val lastAnsweredTimestamp: Long = 0L
) {
    /** 誤答率（0.0 〜 1.0） */
    val errorRate: Float
        get() {
            val total = correctCount + incorrectCount
            if (total == 0) return 0f
            return incorrectCount.toFloat() / total
        }

    /** まだ一度も解答していないか */
    val isUnanswered: Boolean get() = (correctCount + incorrectCount) == 0
}
