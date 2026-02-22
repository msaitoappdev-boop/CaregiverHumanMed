package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val total: Int = 0,
    val currentIndex: Int = 0,
    val selectedIndex: Int? = null,
    val isAnswered: Boolean = false,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val canShowFullExplanation: Boolean = false
)
