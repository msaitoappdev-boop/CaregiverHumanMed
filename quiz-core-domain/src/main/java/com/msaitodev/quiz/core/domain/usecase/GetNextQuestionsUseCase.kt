package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import javax.inject.Inject

class GetNextQuestionsUseCase @Inject constructor(
    private val repo: QuestionRepository
) {
    suspend operator fun invoke(count: Int, excludingIds: Set<String>): List<Question> {
        return repo.getRandomUnseenQuestions(count, excludingIds)
    }
}
