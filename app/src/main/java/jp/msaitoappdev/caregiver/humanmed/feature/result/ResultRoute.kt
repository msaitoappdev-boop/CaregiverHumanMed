package jp.msaitoappdev.caregiver.humanmed.feature.result

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import jp.msaitoappdev.caregiver.humanmed.R
import jp.msaitoappdev.caregiver.humanmed.ads.RewardedHelper
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry

@Composable
fun ResultRoute(
    score: Int,
    total: Int,
    onNextSet: () -> Unit,
    onReview: (questionsJson: String, answersJson: String) -> Unit,
    onReviewSameOrder: () -> Unit,
    onShowScoreHistory: () -> Unit,
    onBackToHome: () -> Unit
) {
    val pct: Int = if (total == 0) 0 else ((score.toFloat() / total) * 100).toInt()
    val message = when {
        pct >= 90 -> stringResource(id = R.string.result_excellent)
        pct >= 70 -> stringResource(id = R.string.result_good)
        pct >= 50 -> stringResource(id = R.string.result_average)
        else -> stringResource(id = R.string.result_poor)
    }

    val saver: ScoreSaverViewModel = hiltViewModel()
    val vm: ResultViewModel = hiltViewModel()

    var hasSavedScore by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasSavedScore) {
            saver.save(
                ScoreEntry(
                    timestamp = System.currentTimeMillis(),
                    score = score,
                    total = total,
                    percent = pct
                )
            )
            hasSavedScore = true
        }
    }

    val context = LocalContext.current
    var showOffer by remember { mutableStateOf(false) }

    LaunchedEffect(vm.effect) {
        vm.effect.collect {
            when (it) {
                is ResultEffect.StartNewQuiz -> onNextSet()
                is ResultEffect.NavigateToReview -> onReview(it.questionsJson, it.answersJson)
                is ResultEffect.ShowRewardOffer -> showOffer = true
                is ResultEffect.ShowMessage -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val activity = LocalContext.current as Activity
    ResultScreen(
        score = score,
        total = total,
        message = message,
        showOfferDialog = showOffer,
        onOfferConfirm = {
            showOffer = false
            RewardedHelper.show(
                activity = activity,
                canShowToday = { true },
                onEarned = { vm.onRewardGranted() },
                onFail = {
                    Toast.makeText(context, R.string.common_error_rewarded_ad, Toast.LENGTH_SHORT).show()
                }
            )
        },
        onOfferDismiss = { showOffer = false },
        onNextSet = { vm.onNextSetClicked() },
        onReviewSameOrder = onReviewSameOrder,
        onReviewList = { vm.onReviewClicked() },
        onScoreHistory = onShowScoreHistory,
        onBackToHome = onBackToHome,
        onNavUp = onBackToHome
    )
}
