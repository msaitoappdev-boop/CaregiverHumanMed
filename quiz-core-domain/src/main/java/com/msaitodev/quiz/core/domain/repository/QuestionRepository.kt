package com.msaitodev.quiz.core.domain.repository

import com.msaitodev.quiz.core.domain.model.Question

interface QuestionRepository {
    suspend fun loadAll(): List<Question>
    suspend fun getRandomUnseenQuestions(count: Int, excludingIds: Set<String>): List<Question>
}
