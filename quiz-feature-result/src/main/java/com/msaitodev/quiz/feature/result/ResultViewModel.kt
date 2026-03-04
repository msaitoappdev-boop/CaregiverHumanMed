package com.msaitodev.quiz.feature.result

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.core.ads.ConsentManager
import com.msaitodev.core.ads.InterstitialHelper
import com.msaitodev.quiz.core.domain.config.RemoteConfigKeys
import com.msaitodev.quiz.core.navigation.ResultDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import com.msaitodev.quiz.core.domain.usecase.StartNextQuizUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ResultEffect {
    /** 新しいクイズを開始する */
    object StartNewQuiz : ResultEffect

    /** 復習画面に遷移する */
    data class NavigateToReview(val questionsJson: String, val answersJson: String) : ResultEffect

    /** リワード広告のオファーを表示する */
    object ShowRewardOffer : ResultEffect

    /** 学習上限に達したことを通知する */
    object QuotaExceeded : ResultEffect

    /** 動画視聴上限に達したことを通知する */
    object RewardLimitReached : ResultEffect

    /** 報酬が付与されたことを通知する */
    object RewardGranted : ResultEffect

    /** 報酬の付与に失敗したことを通知する */
    object RewardGrantFailed : ResultEffect

    /** 汎用メッセージを表示する */
    data class ShowMessage(val message: String) : ResultEffect
}

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val startNextQuiz: StartNextQuizUseCase,
    private val studyQuotaRepository: StudyQuotaRepository,
    private val scoreRepo: ScoreRepository,
    private val interstitialHelper: InterstitialHelper,
    private val remoteConfigRepo: RemoteConfigRepository,
    private val premiumRepository: PremiumRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _effect = MutableSharedFlow<ResultEffect>()
    val effect = _effect.asSharedFlow()

    private val questionsJson: String? = savedStateHandle[ResultDestination.ARG_QUESTIONS_JSON]
    private val answersJson: String? = savedStateHandle[ResultDestination.ARG_ANSWERS_JSON]
    private val isReview: Boolean = savedStateHandle[ResultDestination.ARG_IS_REVIEW] ?: false
    private var hasProcessedResult = false

    fun onScreenShown(activity: Activity, score: Int, total: Int, pct: Int) {
        if (hasProcessedResult) return
        viewModelScope.launch {
            scoreRepo.add(ScoreEntry(
                timestamp = System.currentTimeMillis(),
                score = score,
                total = total,
                percent = pct
            ))

            if (!isReview) {
                studyQuotaRepository.markSetFinished()
            }

            hasProcessedResult = true

            showInterstitial(activity)
        }
    }

    private suspend fun showInterstitial(activity: Activity) {
        // UMP 同意がない場合は表示処理自体をスキップ
        if (!ConsentManager.canRequestAds(activity)) return

        val isPremium = premiumRepository.isPremium.first()
        val interstitialEnabled = remoteConfigRepo.getBoolean(RemoteConfigKeys.INTERSTITIAL_ENABLED) && !isPremium
        if (interstitialEnabled) {
            val cap = remoteConfigRepo.getLong(RemoteConfigKeys.INTERSTITIAL_CAP_PER_SESSION).toInt()
            val intervalSec = remoteConfigRepo.getLong(RemoteConfigKeys.INTER_SESSION_INTERVAL_SEC)
            interstitialHelper.tryShow(activity, true, cap, intervalSec)
        }
    }

    /**
     * 「次のセットへ」がクリックされた時の処理。
     * @param canRequestAds 広告リクエストが可能か（UMP同意済みなど）
     */
    fun onNextSetClicked(canRequestAds: Boolean) {
        viewModelScope.launch {
            when (startNextQuiz()) {
                StartNextQuizUseCase.Result.CanStart -> {
                    _effect.emit(ResultEffect.StartNewQuiz)
                }
                StartNextQuizUseCase.Result.ShowRewardOffer -> {
                    // 同意がある場合のみリワードオファーを出す
                    if (canRequestAds) {
                        _effect.emit(ResultEffect.ShowRewardOffer)
                    } else {
                        _effect.emit(ResultEffect.QuotaExceeded)
                    }
                }
                StartNextQuizUseCase.Result.QuotaExceeded -> {
                    _effect.emit(ResultEffect.QuotaExceeded)
                }
                StartNextQuizUseCase.Result.QuotaExceededAndRewardUsed -> {
                    _effect.emit(ResultEffect.RewardLimitReached)
                }
            }
        }
    }

    fun onReviewClicked() {
        if (questionsJson != null && answersJson != null) {
            viewModelScope.launch {
                _effect.emit(ResultEffect.NavigateToReview(questionsJson, answersJson))
            }
        }
    }

    fun onRewardGranted() {
        viewModelScope.launch {
            try {
                studyQuotaRepository.grantByReward()
                _effect.emit(ResultEffect.RewardGranted)
                _effect.emit(ResultEffect.StartNewQuiz)
            } catch (e: Exception) {
                _effect.emit(ResultEffect.RewardGrantFailed)
            }
        }
    }
}
