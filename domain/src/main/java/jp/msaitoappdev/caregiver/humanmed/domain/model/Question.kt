package jp.msaitoappdev.caregiver.humanmed.domain.model

data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null
)
