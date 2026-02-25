package com.msaitodev.quiz.feature.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.domain.model.QuotaState
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.RemoteConfigRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.StudyQuotaRepository
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

// HomeViewModelが発行する「イベント」を定義。画面遷移の指令は含まない。
sealed interface HomeEvent {
    object RequestNavigateToQuiz : HomeEvent
    object RequestShowRewardedAdOffer : HomeEvent
    data class ShowMessage(val message: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val premiumRepo: PremiumRepository,
    private val remoteConfigRepo: RemoteConfigRepository
) : ViewModel() {

    private val isPremium: StateFlow<Boolean> = premiumRepo.isPremium

    private val quotaFlow: StateFlow<QuotaState?> = isPremium.flatMapLatest { isPremium ->
        val limitKey = if (isPremium) "premium_daily_sets" else "free_daily_sets"
        val limit = remoteConfigRepo.getLong(limitKey).toInt()
        quotaRepo.observe { limit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

    private val _event = MutableSharedFlow<HomeEvent>()
    val event: SharedFlow<HomeEvent> = _event.asSharedFlow()

    fun onStartQuizClicked() {
        viewModelScope.launch {
            val currentQuota = quotaFlow.first() ?: return@launch
            if (currentQuota.canStart) {
                // 「クイズ画面へ遷移して」ではなく「クイズ開始が要求された」というイベントを発行
                _event.emit(HomeEvent.RequestNavigateToQuiz)
            } else {
                handleQuotaExceeded(currentQuota)
            }
        }
    }

    private suspend fun handleQuotaExceeded(quota: QuotaState) {
        if (isPremium.value) {
            _event.emit(HomeEvent.ShowMessage("本日の学習上限に達しました。"))
        } else {
            if (quota.rewardedGranted < 1) {
                // 「リワード広告を見せて」ではなく「リワード広告の表示を要求する」イベントを発行
                _event.emit(HomeEvent.RequestShowRewardedAdOffer)
            } else {
                _event.emit(HomeEvent.ShowMessage("本日は動画視聴による付与は上限です。"))
            }
        }
    }

    fun onRewardedAdEarned() {
        viewModelScope.launch {
            val ok = try {
                quotaRepo.grantByReward()
                true
            } catch (e: Exception) {
                false
            }
            if (ok) {
                // 報酬が付与されたので、再度「クイズ開始が要求された」イベントを発行
                _event.emit(HomeEvent.RequestNavigateToQuiz)
            }
        }
    }
}
