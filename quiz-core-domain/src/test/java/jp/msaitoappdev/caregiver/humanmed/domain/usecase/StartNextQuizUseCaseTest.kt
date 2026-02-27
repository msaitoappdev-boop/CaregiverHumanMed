package jp.msaitoappdev.caregiver.humanmed.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.domain.model.QuotaState
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import com.msaitodev.quiz.core.domain.usecase.StartNextQuizUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StartNextQuizUseCaseTest {

    // --- Fake Repositories --- //

    private class FakeStudyQuotaRepository : StudyQuotaRepository {
        private var quotaStateToReturn: QuotaState? = null

        fun setQuotaState(quotaState: QuotaState) {
            quotaStateToReturn = quotaState
        }

        override fun observe(freeDailySetsProvider: () -> Int): Flow<QuotaState> {
            return flowOf(quotaStateToReturn ?: error("FakeStudyQuotaRepository.setQuotaState was not called"))
        }

        override suspend fun markSetFinished() { /* no-op */ }
        override suspend fun grantByReward() { /* no-op */ }
    }

    private class FakePremiumRepository : PremiumRepository {
        private val _isPremium = MutableStateFlow(false)
        override val isPremium: StateFlow<Boolean> get() = _isPremium

        fun setIsPremium(isPremium: Boolean) {
            _isPremium.value = isPremium
        }

        override suspend fun refreshFromBilling() { /* no-op */ }
        override suspend fun savePremiumStatus(isPremium: Boolean) { /* no-op */ }
        override suspend fun setPremiumForDebug(enabled: Boolean) { /* no-op */ }
    }

    private class FakeRemoteConfigRepository : RemoteConfigRepository {
        private val config = mutableMapOf<String, Any>()

        fun setConfig(key: String, value: Any) {
            config[key] = value
        }

        override fun getLong(key: String): Long {
            return config[key] as? Long ?: 0L
        }

        override fun getBoolean(key: String): Boolean {
            return config[key] as? Boolean ?: false
        }
    }

    // --- Test Cases --- //

    @Test
    fun `invoke - when quota is not exceeded - returns CanStart`() = runTest {
        // Arrange
        val quotaRepo = FakeStudyQuotaRepository().apply {
            setQuotaState(QuotaState(todayKey = "", usedSets = 4, rewardedGranted = 0, freeDailySets = 5))
        }
        val premiumRepo = FakePremiumRepository().apply { setIsPremium(false) }
        val remoteConfigRepo = FakeRemoteConfigRepository().apply { setConfig("free_daily_sets", 5L) }
        val useCase = StartNextQuizUseCase(quotaRepo, premiumRepo, remoteConfigRepo)

        // Act
        val result = useCase.invoke()

        // Assert
        assertThat(result).isInstanceOf(StartNextQuizUseCase.Result.CanStart::class.java)
    }

    @Test
    fun `invoke - when free user quota exceeded and reward not used - returns ShowRewardOffer`() = runTest {
        // Arrange
        val quotaRepo = FakeStudyQuotaRepository().apply {
            setQuotaState(QuotaState(todayKey = "", usedSets = 5, rewardedGranted = 0, freeDailySets = 5))
        }
        val premiumRepo = FakePremiumRepository().apply { setIsPremium(false) }
        val remoteConfigRepo = FakeRemoteConfigRepository().apply { setConfig("free_daily_sets", 5L) }
        val useCase = StartNextQuizUseCase(quotaRepo, premiumRepo, remoteConfigRepo)

        // Act
        val result = useCase.invoke()

        // Assert
        assertThat(result).isInstanceOf(StartNextQuizUseCase.Result.ShowRewardOffer::class.java)
    }

    @Test
    fun `invoke - when free user quota exceeded and reward used - returns QuotaExceededAndRewardUsed`() = runTest {
        // Arrange
        val quotaRepo = FakeStudyQuotaRepository().apply {
            setQuotaState(QuotaState(todayKey = "", usedSets = 6, rewardedGranted = 1, freeDailySets = 5))
        }
        val premiumRepo = FakePremiumRepository().apply { setIsPremium(false) }
        val remoteConfigRepo = FakeRemoteConfigRepository().apply { setConfig("free_daily_sets", 5L) }
        val useCase = StartNextQuizUseCase(quotaRepo, premiumRepo, remoteConfigRepo)

        // Act
        val result = useCase.invoke()

        // Assert
        assertThat(result).isInstanceOf(StartNextQuizUseCase.Result.QuotaExceededAndRewardUsed::class.java)
    }

    @Test
    fun `invoke - when premium user quota exceeded - returns QuotaExceeded`() = runTest {
        // Arrange
        val quotaRepo = FakeStudyQuotaRepository().apply {
            setQuotaState(QuotaState(todayKey = "", usedSets = 20, rewardedGranted = 0, freeDailySets = 20))
        }
        val premiumRepo = FakePremiumRepository().apply { setIsPremium(true) }
        val remoteConfigRepo = FakeRemoteConfigRepository().apply { setConfig("premium_daily_sets", 20L) }
        val useCase = StartNextQuizUseCase(quotaRepo, premiumRepo, remoteConfigRepo)

        // Act
        val result = useCase.invoke()

        // Assert
        assertThat(result).isInstanceOf(StartNextQuizUseCase.Result.QuotaExceeded::class.java)
    }
}
