package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.usecase.ObserveScoresUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveScoresUseCaseTest {

    // テスト用の偽Repositoryを作成
    private class FakeScoreRepository(private val historyFlow: Flow<List<ScoreEntry>>) : ScoreRepository {
        override fun history(): Flow<List<ScoreEntry>> {
            return historyFlow
        }

        override suspend fun add(entry: ScoreEntry) {
            // このテストでは使わない
        }

        override suspend fun clear() {
            // このテストでは使わない
        }
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        // GIVEN: 特定のテストデータを流すFlowと、それを使う偽Repository、そしてUseCaseを準備
        val testData = listOf(ScoreEntry(score = 1, total = 3, timestamp = 1L, percent = 33))
        val testFlow = flowOf(testData)
        val fakeRepository = FakeScoreRepository(testFlow)
        val observeScoresUseCase = ObserveScoresUseCase(fakeRepository)

        // WHEN: UseCaseを実行し、返されたFlowをTurbineでテストする
        observeScoresUseCase().test {
            // THEN: Repositoryが流したテストデータと全く同じものが流れてくることを確認
            val emission = awaitItem()
            assertThat(emission).hasSize(1)
            assertThat(emission[0].score).isEqualTo(1)

            // Flowが完了することを確認
            awaitComplete()
        }
    }
}
