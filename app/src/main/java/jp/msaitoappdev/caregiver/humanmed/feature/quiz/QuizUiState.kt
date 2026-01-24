package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedIndex: Int? = null,
    val isAnswered: Boolean = false,
    val correctCount: Int = 0,
    val finished: Boolean = false
) {
    val total: Int get() = questions.size
    val current: Question? get() = questions.getOrNull(currentIndex)
    val progressText: String get() = "${currentIndex + 1} / $total"
}
