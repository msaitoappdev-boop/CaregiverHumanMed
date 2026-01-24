package jp.msaitoappdev.caregiver.humanmed.domain.repository

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question

interface QuestionRepository {
    suspend fun loadAll(): List<Question>
}
