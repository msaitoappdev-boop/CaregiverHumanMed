package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import com.google.common.truth.Truth.assertThat
import jp.msaitoappdev.caregiver.humanmed.domain.model.Question
import jp.msaitoappdev.caregiver.humanmed.domain.repository.QuestionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetNextQuestionsUseCaseTest {

    // テスト用の偽Repositoryを作成
    private class FakeQuestionRepository(private val questions: List<Question>) : QuestionRepository {
        var getRandomUnseenQuestions_called_with_excludingIds: Set<String>? = null

        override suspend fun loadAll(): List<Question> {
            return questions
        }

        override suspend fun getRandomUnseenQuestions(count: Int, excludingIds: Set<String>): List<Question> {
            getRandomUnseenQuestions_called_with_excludingIds = excludingIds
            return questions.filterNot { it.id in excludingIds }.take(count)
        }
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        // GIVEN: 5つの質問リストを持つ偽RepositoryとUseCaseを準備
        val allQuestions = listOf(
            Question("q1", "text1", emptyList(), 0, ""),
            Question("q2", "text2", emptyList(), 0, ""),
            Question("q3", "text3", emptyList(), 0, ""),
            Question("q4", "text4", emptyList(), 0, ""),
            Question("q5", "text5", emptyList(), 0, "")
        )
        val fakeRepository = FakeQuestionRepository(allQuestions)
        val getNextQuestionsUseCase = GetNextQuestionsUseCase(fakeRepository)
        val excludingIds = setOf("q2", "q4")

        // WHEN: UseCaseを実行する (3つの質問を要求)
        val result = getNextQuestionsUseCase(count = 3, excludingIds = excludingIds)

        // THEN: Repositoryのメソッドが正しいexcludingIdsで呼ばれたことを確認
        assertThat(fakeRepository.getRandomUnseenQuestions_called_with_excludingIds).isEqualTo(excludingIds)
        
        // THEN: 結果のリストサイズが3であることを確認
        assertThat(result).hasSize(3)

        // THEN: 結果に除外対象のIDが含まれていないことを確認
        assertThat(result.any { it.id == "q2" }).isFalse()
        assertThat(result.any { it.id == "q4" }).isFalse()
    }
}
