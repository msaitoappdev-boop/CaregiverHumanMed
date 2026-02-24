package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import javax.inject.Inject

class GetNextQuestionsUseCase @Inject constructor(
    private val repo: QuestionRepository
) {
    suspend operator fun invoke(count: Int, excludingIds: Set<String>): List<Question> {
        return repo.getRandomUnseenQuestions(count, excludingIds)
    }
}
