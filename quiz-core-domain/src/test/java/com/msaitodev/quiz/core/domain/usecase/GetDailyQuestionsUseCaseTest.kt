package com.msaitodev.quiz.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import com.msaitodev.quiz.core.domain.ui.DailyQuestionSelector
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetDailyQuestionsUseCaseTest {

    // テスト用の偽Repository
    private class FakeQuestionRepository(private val questions: List<Question>) : QuestionRepository {
        override suspend fun loadAll(): List<Question> {
            return questions
        }

        override suspend fun getRandomUnseenQuestions(count: Int, excludingIds: Set<String>): List<Question> {
            // このテストでは使わない
            return emptyList()
        }
    }

    // テスト用の偽Selector
    private class FakeDailyQuestionSelector : DailyQuestionSelector() {
        var select_was_called = false
        // selectが呼ばれたら、渡されたリストをそのまま返す
        override fun select(all: List<Question>, count: Int): List<Question> {
            select_was_called = true
            return all.take(count)
        }
    }

    @Test
    fun `invoke calls dependencies and returns correctly`() = runTest {
        // GIVEN: 3つの質問を持つ偽Repositoryと、透過的な偽Selector、そしてUseCaseを準備
        val allQuestions = listOf(
            Question("q1", "text1", emptyList(), 0, ""),
            Question("q2", "text2", emptyList(), 0, ""),
            Question("q3", "text3", emptyList(), 0, "")
        )
        val fakeRepository = FakeQuestionRepository(allQuestions)
        val fakeSelector = FakeDailyQuestionSelector()
        val getDailyQuestionsUseCase = GetDailyQuestionsUseCase(fakeRepository, fakeSelector)

        // WHEN: UseCaseを実行する (2つの質問を要求)
        val result = getDailyQuestionsUseCase(count = 2)

        // THEN: Selectorのselectメソッドが呼ばれたことを確認
        assertThat(fakeSelector.select_was_called).isTrue()

        // THEN: 結果のリストサイズが2であることを確認
        assertThat(result).hasSize(2)
        
        // THEN: 結果が期待通りであることを確認
        assertThat(result.map { it.id }).containsExactly("q1", "q2").inOrder()
    }
}
