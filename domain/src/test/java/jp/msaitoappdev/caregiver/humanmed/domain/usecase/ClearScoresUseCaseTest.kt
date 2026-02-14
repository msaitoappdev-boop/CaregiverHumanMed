package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import com.google.common.truth.Truth.assertThat
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClearScoresUseCaseTest {

    // UseCaseが依存するRepositoryの、テスト用の偽物（Fake）を作成
    private class FakeScoreRepository : ScoreRepository {
        var clearCalledCount = 0

        override fun history(): Flow<List<ScoreEntry>> {
            // このテストでは使わないので、空のFlowを返す
            return flowOf(emptyList())
        }

        override suspend fun add(entry: ScoreEntry) {
            // このテストでは使わない
        }

        override suspend fun clear() {
            clearCalledCount++
        }
    }

    @Test
    fun `invoke calls repository clear`() = runTest {
        // GIVEN: 偽のRepositoryと、テスト対象のUseCaseを準備
        val fakeRepository = FakeScoreRepository()
        val clearScoresUseCase = ClearScoresUseCase(fakeRepository)

        // WHEN: UseCaseを実行する
        clearScoresUseCase()

        // THEN: Repositoryのclearメソッドが1回だけ呼ばれたことを確認
        assertThat(fakeRepository.clearCalledCount).isEqualTo(1)
    }
}
