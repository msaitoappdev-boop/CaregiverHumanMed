package com.msaitodev.quiz.feature.result

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.core.ads.ConsentManager
import com.msaitodev.core.ads.InterstitialHelper
import com.msaitodev.core.ads.RewardedHelper
import com.msaitodev.quiz.core.navigation.ResultDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import com.msaitodev.quiz.core.domain.model.Question
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import com.msaitodev.quiz.core.domain.repository.WrongAnswerRepository
import com.msaitodev.quiz.core.domain.usecase.StartNextQuizUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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
    private val wrongAnswerRepo: WrongAnswerRepository, // 追加
    private val interstitialHelper: InterstitialHelper,
    private val rewardedHelper: RewardedHelper,
    private val premiumRepository: PremiumRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _effect = MutableSharedFlow<ResultEffect>()
    val effect = _effect.asSharedFlow()

    /** 購読状態を UI 側に公開 */
    val isPremium: StateFlow<Boolean> = premiumRepository.isPremium

    private val questionsJson: String? = savedStateHandle[ResultDestination.ARG_QUESTIONS_JSON]
    private val answersJson: String? = savedStateHandle[ResultDestination.ARG_ANSWERS_JSON]
    private val isReview: Boolean = savedStateHandle[ResultDestination.ARG_IS_REVIEW] ?: false
    private var hasProcessedResult = false

    private val json = Json { ignoreUnknownKeys = true }

    fun onScreenShown(activity: Activity, score: Int, total: Int, pct: Int) {
        if (hasProcessedResult) return
        viewModelScope.launch {
            // 1. スコア履歴の保存
            scoreRepo.add(ScoreEntry(
                timestamp = System.currentTimeMillis(),
                score = score,
                total = total,
                percent = pct
            ))

            // 2. 復習でない場合のみ学習上限カウントを進め、各問題の正誤を記録
            if (!isReview) {
                studyQuotaRepository.markSetFinished()
                recordQuestionStats()
            }

            hasProcessedResult = true

            // 3. インタースティシャル広告の試行
            showInterstitial(activity)
        }
    }

    /**
     * 各問題の正解・不正解を WrongAnswerRepository に記録する
     */
    private suspend fun recordQuestionStats() {
        if (questionsJson == null || answersJson == null) return
        
        try {
            val questions = json.decodeFromString<List<Question>>(questionsJson)
            val answers = json.decodeFromString<List<Int>>(answersJson)

            questions.forEachIndexed { index, question ->
                val userAnswer = answers.getOrNull(index)
                if (userAnswer != null) {
                    if (userAnswer == question.correctIndex) {
                        wrongAnswerRepo.recordCorrect(question.id)
                    } else {
                        wrongAnswerRepo.recordIncorrect(question.id)
                    }
                }
            }
        } catch (e: Exception) {
            // 解析エラー時はログを出力して続行（致命的なエラーにはしない）
            android.util.Log.e("ResultViewModel", "Failed to record question stats", e)
        }
    }

    private suspend fun showInterstitial(activity: Activity) {
        if (!ConsentManager.canRequestAds(activity)) return

        val isPremiumValue = premiumRepository.isPremium.first()

        // リワード広告を既に視聴済みの場合は、広告の連続表示を避けるためインタースティシャルをスキップする
        val canShowReward = rewardedHelper.canShowToday.first()
        if (!canShowReward) return
        
        interstitialHelper.tryShow(activity, isPremium = isPremiumValue)
    }

    /**
     * 「次のセットへ」がクリックされた時の処理。
     */
    fun onNextSetClicked(canRequestAds: Boolean) {
        viewModelScope.launch {
            when (startNextQuiz()) {
                StartNextQuizUseCase.Result.CanStart -> {
                    _effect.emit(ResultEffect.StartNewQuiz)
                }
                StartNextQuizUseCase.Result.ShowRewardOffer -> {
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
            _effect.emit(ResultEffect.RewardGranted)
            _effect.emit(ResultEffect.StartNewQuiz)
        }
    }
}
