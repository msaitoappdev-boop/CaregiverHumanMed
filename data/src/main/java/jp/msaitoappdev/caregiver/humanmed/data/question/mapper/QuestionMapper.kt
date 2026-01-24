package jp.msaitoappdev.caregiver.humanmed.data.question.mapper

import jp.msaitoappdev.caregiver.humanmed.data.question.dto.QuestionDto
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question

fun QuestionDto.toDomain(): Question =
    Question(
        id = id,
        text = text,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation
    )
