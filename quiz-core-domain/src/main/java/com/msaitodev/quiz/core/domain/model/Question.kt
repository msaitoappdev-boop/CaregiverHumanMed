package com.msaitodev.quiz.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val category: String, // 追加: 分野名
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String?
)
