package com.msaitodev.quiz.feature.main.quiz

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import kotlinx.serialization.Serializable

@Serializable
data class QuizResult(
    val score: Int,
    val total: Int,
    val questions: List<Question>,
    val answers: List<Int?>,
    val isReview: Boolean
)
