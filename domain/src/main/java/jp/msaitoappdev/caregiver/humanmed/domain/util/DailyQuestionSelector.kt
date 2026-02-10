package jp.msaitoappdev.caregiver.humanmed.domain.util

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import kotlin.random.Random

class DailyQuestionSelector {
    fun select(
        all: List<Question>,
        count: Int = 3
    ): List<Question> {
        if (all.isEmpty() || count <= 0) return emptyList()
        val seed = System.currentTimeMillis()
        return all.shuffled(Random(seed)).take(minOf(count, all.size))
    }
}
