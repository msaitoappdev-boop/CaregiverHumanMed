package com.msaitodev.quiz.feature.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.core.ads.RewardedHelper
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
    private val startNextQuiz: StartNextQuizUseCase
) : ViewModel() {

    private val isPremium: StateFlow<Boolean> = premiumRepo.isPremium

    @OptIn(ExperimentalCoroutinesApi::class)
    private val quotaFlow: StateFlow<QuotaState?> = isPremium.flatMapLatest { isPremiumValue ->
        val limitKey = if (isPremiumValue) RemoteConfigKeys.PREMIUM_DAILY_SETS else RemoteConfigKeys.FREE_DAILY_SETS
        val limit = remoteConfigRepo.getLong(limitKey).toInt()
        quotaRepo.observe { limit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    data class HomeUiState(
        val canStart: Boolean = false,
        val isLoading: Boolean = false,
        val canShowFullExplanation: Boolean = false,
        val isWeaknessTrainingLocked: Boolean = true
    )

    val uiState: StateFlow<HomeUiState> = combine(
        quotaFlow, 
        isPremium
    ) { quota, isPremiumValue ->
        if (quota == null) {
            HomeUiState(isLoading = true)
        } else {
            HomeUiState(
                canStart = quota.canStart,
                canShowFullExplanation = isPremiumValue,
                isWeaknessTrainingLocked = !isPremiumValue
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
     * 弱点特訓ボタンがクリックされた時の処理。
     */
    fun onStartWeaknessTrainingClicked() {
        viewModelScope.launch {
            val state = uiState.value
            
            if (state.isWeaknessTrainingLocked) {
                _event.emit(HomeEvent.RequestShowPaywall)
                return@launch
            }

            // モードをONにして設定画面へ遷移（仕様4）
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
}
