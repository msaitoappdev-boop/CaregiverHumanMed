package jp.msaitoappdev.caregiver.humanmed.feature.home

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.ads.InterstitialHelper
import jp.msaitoappdev.caregiver.humanmed.core.session.QuotaState
import jp.msaitoappdev.caregiver.humanmed.core.session.StudyQuotaRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeEffect {
    object NavigateToQuiz : HomeEffect
    data class NavigateToResult(val score: Int, val total: Int) : HomeEffect
    object LoadNextQuizSet : HomeEffect
    object ShowRewardedAdOffer : HomeEffect
    data class ShowMessage(val message: String) : HomeEffect
    object RewardGrantedAndNavigate : HomeEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val premiumRepo: PremiumRepository,
    private val interstitialHelper: InterstitialHelper,
    private val remoteConfigRepo: RemoteConfigRepository
) : ViewModel() {

    private val isPremium: StateFlow<Boolean> = premiumRepo.isPremium

    private val quotaFlow: StateFlow<QuotaState?> = isPremium.flatMapLatest { isPremium ->
        val limitKey = if (isPremium) "premium_daily_sets" else "free_daily_sets"
        val limit = remoteConfigRepo.getLong(limitKey).toInt()
        quotaRepo.observe { limit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private suspend fun fetchLatestQuota(): QuotaState? {
        val limitKey = if (isPremium.value) "premium_daily_sets" else "free_daily_sets"
        val limit = remoteConfigRepo.getLong(limitKey).toInt()
        return quotaRepo.observe { limit }.first()
    }

    data class HomeUiState(
        val canStart: Boolean = false,
        val isLoading: Boolean = false,
        val canShowFullExplanation: Boolean = false
    )

    val uiState: StateFlow<HomeUiState> = combine(quotaFlow, isPremium) { quota, isPremiumValue ->
        if (quota == null) {
            HomeUiState(isLoading = true)
        } else {
            HomeUiState(
                canStart = quota.canStart,
                canShowFullExplanation = isPremiumValue
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    private var isNextSetProcessing = false

    fun onStartQuizClicked() {
        viewModelScope.launch {
            val currentQuota = quotaFlow.first() ?: return@launch
            if (currentQuota.canStart) {
                _effect.emit(HomeEffect.NavigateToQuiz)
            } else {
                handleQuotaExceeded(currentQuota)
            }
        }
    }

    fun onQuizFinished(activity: Activity, score: Int, total: Int, isReview: Boolean) {
        viewModelScope.launch {
            if (!isReview) {
                try {
                    quotaRepo.markSetFinished()
                } catch (e: Exception) {
                }
            }

            val interstitialEnabled = remoteConfigRepo.getBoolean("interstitial_enabled") && !isPremium.value
            if (!interstitialEnabled) {
                _effect.emit(HomeEffect.NavigateToResult(score, total))
                return@launch
            }
            val cap = remoteConfigRepo.getLong("interstitial_cap_per_session").toInt()
            val intervalSec = remoteConfigRepo.getLong("inter_session_interval_sec")
            interstitialHelper.tryShow(activity, true, cap, intervalSec)
            _effect.emit(HomeEffect.NavigateToResult(score, total))
        }
    }

    fun onNextSetClicked(activity: Activity) {
        if (isNextSetProcessing) {
            return
        }
        isNextSetProcessing = true

        viewModelScope.launch {
            try {
                val newQuota = fetchLatestQuota()
                if (newQuota?.canStart == true) {
                    _effect.emit(HomeEffect.LoadNextQuizSet)
                } else {
                    if (newQuota != null) {
                        handleQuotaExceeded(newQuota)
                    }
                }
            } finally {
                isNextSetProcessing = false
            }
        }
    }

    suspend fun tryGrantDailyPlusOne(): Boolean {
        return try {
            val currentQuota = fetchLatestQuota()
            if (currentQuota == null) {
                return false
            }
            if (isPremium.value || currentQuota.rewardedGranted >= 1) {
                return false
            }
            quotaRepo.grantByReward()
            true
        } catch (t: Throwable) {
            false
        }
    }

    private suspend fun handleQuotaExceeded(quota: QuotaState) {
        if (isPremium.value) {
            _effect.emit(HomeEffect.ShowMessage("本日の学習上限に達しました。"))
        } else {
            if (quota.rewardedGranted < 1) {
                _effect.emit(HomeEffect.ShowRewardedAdOffer)
            } else {
                _effect.emit(HomeEffect.ShowMessage("本日は動画視聴による付与は上限です。"))
            }
        }
    }

    fun onRewardedAdEarned() {
        viewModelScope.launch {
            try {
                val ok = tryGrantDailyPlusOne()
                if (ok) {
                    _effect.emit(HomeEffect.RewardGrantedAndNavigate)
                }
            } catch (e: Exception) {
            }
        }
    }
}
