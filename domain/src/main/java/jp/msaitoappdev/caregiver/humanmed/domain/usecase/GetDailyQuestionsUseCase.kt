package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import jp.msaitoappdev.caregiver.humanmed.domain.util.DailyQuestionSelector

class GetDailyQuestionsUseCase(
    private val repo: QuestionRepository,
    private val selector: DailyQuestionSelector
) {
    suspend operator fun invoke(count: Int = 3): List<Question> {
        val all = repo.loadAll()
        return selector.select(all, count)
    }
}
