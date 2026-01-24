package jp.msaitoappdev.caregiver.humanmed.data.mapper

import jp.msaitoappdev.caregiver.humanmed.data.dto.QuestionDto
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question

fun QuestionDto.toDomain(): Question =
    Question(
        id = id,
        text = text,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation
    )
