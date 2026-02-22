package com.msaitodev.quiz.feature.review

data class ReviewUiState(
    val items: List<ReviewItem> = emptyList(),
    val isLoading: Boolean = true
)