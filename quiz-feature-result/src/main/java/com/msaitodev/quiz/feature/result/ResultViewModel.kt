package com.msaitodev.quiz.feature.result

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.quiz.core.ads.RewardedHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.repository.ScoreRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.StudyQuotaRepository
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.StartNextQuizUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val quotaRepo: StudyQuotaRepository,
    private val scoreRepo: ScoreRepository,
    private val rewardedHelper: RewardedHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _effect = MutableSharedFlow<ResultEffect>()
    val effect = _effect.asSharedFlow()

    private val questionsJson: String? = savedStateHandle[NavRoutes.Result.ARG_QUESTIONS_JSON]
    private val answersJson: String? = savedStateHandle[NavRoutes.Result.ARG_ANSWERS_JSON]
    private var hasSavedScore = false

    fun onScreenShown(score: Int, total: Int, pct: Int) {
        if (hasSavedScore) return
        viewModelScope.launch {
            scoreRepo.add(ScoreEntry(
                timestamp = System.currentTimeMillis(),
                score = score,
                total = total,
                percent = pct
            ))
            hasSavedScore = true
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

    fun onOfferConfirmed(activity: Activity) {
        rewardedHelper.show(
            activity = activity,
            canShowToday = { true }, // ここはViewModelが本来持つべき状態
            onEarned = { onRewardGranted() },
            onFail = { viewModelScope.launch { _effect.emit(ResultEffect.ShowMessage("動画を読み込めませんでした")) } }
        )
    }

    private fun onRewardGranted() {
        viewModelScope.launch {
            try {
                quotaRepo.grantByReward()
                _effect.emit(ResultEffect.StartNewQuiz)
            } catch (e: Exception) {
                _effect.emit(ResultEffect.ShowMessage("エラーが発生しました"))
            }
        }
    }
}
