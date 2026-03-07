package com.msaitodev.quiz.core.data.mapper

import com.msaitodev.quiz.core.data.local.dto.QuestionDto
import com.msaitodev.quiz.core.domain.model.Question

/**
 * QuestionDto から Domain モデルへの変換。
 * category はファイルパス（ディレクトリ名）から外部的に決定されるため引数で受け取る。
 */
fun QuestionDto.toDomain(category: String): Question =
    Question(
        id = id,
        category = category,
        text = text,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation
    )
