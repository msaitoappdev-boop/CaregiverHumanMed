package jp.msaitoappdev.caregiver.humanmed.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestionDto(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null
)
