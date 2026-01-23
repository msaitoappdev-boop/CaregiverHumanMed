package jp.msaitoappdev.caregiver.humanmed.data

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null
)