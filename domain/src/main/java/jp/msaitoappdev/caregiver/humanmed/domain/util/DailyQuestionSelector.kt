package jp.msaitoappdev.caregiver.humanmed.domain.util

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import java.util.Calendar
import kotlin.random.Random

class DailyQuestionSelector {
    fun select(
        all: List<Question>,
        count: Int = 3,
        calendar: Calendar = Calendar.getInstance()
    ): List<Question> {
        if (all.isEmpty() || count <= 0) return emptyList()
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        val seed = y * 10000L + m * 100L + d
        return all.shuffled(Random(seed)).take(minOf(count, all.size))
    }
}
