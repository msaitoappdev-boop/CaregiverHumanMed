package com.msaitodev.quiz.core.data.local.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestionDto(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null,
    val category: String? = null // JSON側には存在しない可能性があるためnull許容
)
