package com.msaitodev.quiz.feature.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.core.ads.RewardedHelper
import com.msaitodev.core.notifications.ReminderRepository
import com.msaitodev.feature.settings.SettingsProvider
import com.msaitodev.quiz.core.domain.config.RemoteConfigKeys
import com.msaitodev.quiz.core.domain.model.QuotaState
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import com.msaitodev.quiz.core.domain.usecase.StartNextQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// HomeViewModelが発行する「イベント」を定義。
sealed interface HomeEvent {
    object RequestNavigateToQuiz : HomeEvent
    object RequestNavigateToAnalysis : HomeEvent
    object RequestShowRewardedAdOffer : HomeEvent
    object RequestShowPaywall : HomeEvent
    object RequestNavigateToSettings : HomeEvent
    object QuotaExceeded : HomeEvent
    object RewardLimitReached : HomeEvent
    object RewardGranted : HomeEvent
    object RewardGrantFailed : HomeEvent
    data class ShowMessage(val message: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val rewardedHelper: RewardedHelper,
    private val settingsProvider: SettingsProvider,
    premiumRepo: PremiumRepository,
    private val remoteConfigRepo: RemoteConfigRepository,
    private val startNextQuiz: StartNextQuizUseCase,
    private val reminderRepo: ReminderRepository
) : ViewModel() {

    private val isPremium: StateFlow<Boolean> = premiumRepo.isPremium

    // ユーザーがそのセッションで誘導を閉じたかどうかを管理
    private val _isReminderInvitationDismissed = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val quotaFlow: StateFlow<QuotaState?> = isPremium.flatMapLatest { isPremiumValue ->
        val limitKey = if (isPremiumValue) RemoteConfigKeys.PREMIUM_DAILY_SETS else RemoteConfigKeys.FREE_DAILY_SETS
        val limit = remoteConfigRepo.getLong(limitKey).toInt()
        quotaRepo.observe { limit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    data class HomeUiState(
        val canStart: Boolean = false,
        val isLoading: Boolean = false,
        val isPremium: Boolean = false,
        val showReminderInvitation: Boolean = false
    )

    val uiState: StateFlow<HomeUiState> = combine(
        quotaFlow, 
        isPremium,
        reminderRepo.isReminderEnabled,
        _isReminderInvitationDismissed
    ) { quota, isPremiumValue, isReminderEnabled, isDismissed ->
        if (quota == null) {
            HomeUiState(isLoading = true)
        } else {
            HomeUiState(
                canStart = quota.canStart,
                isPremium = isPremiumValue,
                // リマインド未設定かつ、まだ閉じていない場合に表示
                showReminderInvitation = !isReminderEnabled && !isDismissed
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    private val _event = MutableSharedFlow<HomeEvent>()
    val event: SharedFlow<HomeEvent> = _event.asSharedFlow()

    fun onStartQuizClicked(canRequestAds: Boolean) {
        viewModelScope.launch {
            when (startNextQuiz()) {
                StartNextQuizUseCase.Result.CanStart -> {
                    _event.emit(HomeEvent.RequestNavigateToQuiz)
                }
                StartNextQuizUseCase.Result.ShowRewardOffer -> {
                    if (canRequestAds) {
                        _event.emit(HomeEvent.RequestShowRewardedAdOffer)
                    } else {
                        _event.emit(HomeEvent.QuotaExceeded)
                    }
                }
                StartNextQuizUseCase.Result.QuotaExceeded -> {
                    _event.emit(HomeEvent.QuotaExceeded)
                }
                StartNextQuizUseCase.Result.QuotaExceededAndRewardUsed -> {
                    _event.emit(HomeEvent.RewardLimitReached)
                }
            }
        }
    }

    /**
     * 学習分析ボタンがクリックされた時の処理。
     */
    fun onAnalysisClicked() {
        viewModelScope.launch {
            if (uiState.value.isPremium) {
                _event.emit(HomeEvent.RequestNavigateToAnalysis)
            } else {
                _event.emit(HomeEvent.RequestShowPaywall)
            }
        }
    }

    fun onStartWeaknessTrainingClicked() {
        viewModelScope.launch {
            if (!uiState.value.isPremium) {
                _event.emit(HomeEvent.RequestShowPaywall)
                return@launch
            }

            settingsProvider.updateWeaknessMode(true)
            _event.emit(HomeEvent.RequestNavigateToSettings)
        }
    }

    fun onRewardGranted() {
        viewModelScope.launch {
            _event.emit(HomeEvent.RewardGranted)
            _event.emit(HomeEvent.RequestNavigateToQuiz)
        }
    }

    fun onReminderInvitationDismissed() {
        _isReminderInvitationDismissed.value = true
    }

    fun onReminderInvitationClicked() {
        viewModelScope.launch {
            _event.emit(HomeEvent.RequestNavigateToSettings)
        }
    }
}
