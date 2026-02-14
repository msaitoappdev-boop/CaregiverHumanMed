package jp.msaitoappdev.caregiver.humanmed.data.local.question.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.OptIn

@OptIn(InternalSerializationApi::class)
@Serializable
data class QuestionDto(
    val id: String,
    val text: String,
    val options: List<String>,
    @SerialName("correct_index")
    val correctIndex: Int,
    val explanation: String? = null
)
