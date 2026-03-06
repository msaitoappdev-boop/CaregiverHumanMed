package com.msaitodev.quiz.feature.main.quiz

import com.msaitodev.quiz.core.domain.model.Question

sealed interface QuizMode {
    data object Daily : QuizMode
    data object WeaknessAll : QuizMode
    data class WeaknessCategory(val categoryName: String) : QuizMode
    data object Review : QuizMode
}

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val total: Int = 0,
    val currentIndex: Int = 0,
    val currentCategoryName: String = "",
    val selectedIndex: Int? = null,
    val isAnswered: Boolean = false,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val canShowFullExplanation: Boolean = false,
    val mode: QuizMode = QuizMode.Daily
)
