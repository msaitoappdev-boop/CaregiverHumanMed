package com.msaitodev.quiz.feature.result

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.quiz.core.ads.InterstitialHelper
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

    /** トーストメッセージを表示する */
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
        val isPremium = premiumRepository.isPremium.first()
        val interstitialEnabled = remoteConfigRepo.getBoolean(RemoteConfigKeys.INTERSTITIAL_ENABLED) && !isPremium
        if (interstitialEnabled) {
            val cap = remoteConfigRepo.getLong(RemoteConfigKeys.INTERSTITIAL_CAP_PER_SESSION).toInt()
            val intervalSec = remoteConfigRepo.getLong(RemoteConfigKeys.INTER_SESSION_INTERVAL_SEC)
            interstitialHelper.tryShow(activity, true, cap, intervalSec)
        }
    }

    fun onNextSetClicked() {
        viewModelScope.launch {
            when (startNextQuiz()) {
                StartNextQuizUseCase.Result.CanStart -> {
                    _effect.emit(ResultEffect.StartNewQuiz)
                }
                StartNextQuizUseCase.Result.ShowRewardOffer -> {
                    _effect.emit(ResultEffect.ShowRewardOffer)
                }
                StartNextQuizUseCase.Result.QuotaExceeded -> {
                    _effect.emit(ResultEffect.ShowMessage("本日の学習上限に達しました。"))
                }
                StartNextQuizUseCase.Result.QuotaExceededAndRewardUsed -> {
                    _effect.emit(ResultEffect.ShowMessage("本日は動画視聴による付与は上限です。"))
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
                _effect.emit(ResultEffect.StartNewQuiz)
            } catch (e: Exception) {
                _effect.emit(ResultEffect.ShowMessage("エラーが発生しました"))
            }
        }
    }
}
