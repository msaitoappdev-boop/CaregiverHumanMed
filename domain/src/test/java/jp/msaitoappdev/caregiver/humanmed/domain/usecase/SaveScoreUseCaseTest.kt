package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import com.google.common.truth.Truth.assertThat
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveScoreUseCaseTest {

    // テスト用の偽Repositoryを作成
    private class FakeScoreRepository : ScoreRepository {
        private var savedEntry: ScoreEntry? = null

        override fun history(): Flow<List<ScoreEntry>> {
            return flowOf(emptyList()) // このテストでは使わない
        }

        override suspend fun add(entry: ScoreEntry) {
            savedEntry = entry // 渡されたentryを記録する
        }

        override suspend fun clear() {
            // このテストでは使わない
        }

        fun getSavedEntry() = savedEntry
    }

    @Test
    fun `invoke calls repository add with correct entry`() = runTest {
        // GIVEN: 偽のRepositoryとUseCaseを準備
        val fakeRepository = FakeScoreRepository()
        val saveScoreUseCase = SaveScoreUseCase(fakeRepository)
        val scoreEntry = ScoreEntry(score = 8, total = 10, timestamp = 12345L, percent = 80)

        // WHEN: UseCaseを実行する
        saveScoreUseCase(scoreEntry)

        // THEN: Repositoryのaddメソッドが、正しいentryで呼ばれたことを確認
        val saved = fakeRepository.getSavedEntry()
        assertThat(saved).isNotNull()
        assertThat(saved?.score).isEqualTo(8)
        assertThat(saved?.total).isEqualTo(10)
        assertThat(saved?.timestamp).isEqualTo(12345L)
        assertThat(saved?.percent).isEqualTo(80)
    }
}
