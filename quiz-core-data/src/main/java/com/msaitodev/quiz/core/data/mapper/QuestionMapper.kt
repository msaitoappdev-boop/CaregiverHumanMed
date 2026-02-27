package com.msaitodev.quiz.core.data.mapper

import com.msaitodev.quiz.core.data.local.dto.QuestionDto
import com.msaitodev.quiz.core.domain.model.Question

fun QuestionDto.toDomain(): Question =
    Question(
        id = id,
        text = text,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation // Now both can be null, so this should match
    )
