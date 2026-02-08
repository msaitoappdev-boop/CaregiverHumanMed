package jp.msaitoappdev.caregiver.humanmed.feature.quiz

data class ReviewItem(
    val number: Int,
    val question: String,
    val options: List<String>,
    val selectedIndex: Int?,
    val correctIndex: Int,
    val explanation: String?
)
