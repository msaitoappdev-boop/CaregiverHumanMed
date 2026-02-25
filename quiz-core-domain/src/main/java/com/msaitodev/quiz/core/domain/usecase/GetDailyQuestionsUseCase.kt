package com.msaitodev.quiz.core.domain.usecase

import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import com.msaitodev.quiz.core.domain.ui.DailyQuestionSelector

class GetDailyQuestionsUseCase(
    private val repo: QuestionRepository,
    private val selector: DailyQuestionSelector
) {
    suspend operator fun invoke(count: Int = 3): List<Question> {
        val all = repo.loadAll()
        return selector.select(all, count)
    }
}
