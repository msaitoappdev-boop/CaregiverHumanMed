package com.msaitodev.quiz.feature.result

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msaitodev.core.ads.ConsentManager
import com.msaitodev.core.ads.RewardedHelper
import kotlinx.coroutines.launch

@Composable
fun ResultRoute(
    score: Int,
    total: Int,
    rewardedHelper: RewardedHelper,
    onNextSet: () -> Unit,
    onReview: (questionsJson: String, answersJson: String) -> Unit,
    onReviewSameOrder: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val pct: Int = if (total == 0) 0 else ((score.toFloat() / total) * 100).toInt()
    val message = when {
        pct >= 90 -> stringResource(id = R.string.result_excellent)
        pct >= 70 -> stringResource(id = R.string.result_good)
        pct >= 50 -> stringResource(id = R.string.result_average)
        else -> stringResource(id = R.string.result_poor)
    }

    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onScreenShown(activity, score, total, pct)
    }

    val context = LocalContext.current
    var showOffer by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ResultEffect.StartNewQuiz -> onNextSet()
                is ResultEffect.NavigateToReview -> onReview(effect.questionsJson, effect.answersJson)
                is ResultEffect.ShowRewardOffer -> showOffer = true
                is ResultEffect.QuotaExceeded -> {
                    Toast.makeText(context, R.string.result_quota_exceeded, Toast.LENGTH_SHORT).show()
                }
                is ResultEffect.RewardLimitReached -> {
                    Toast.makeText(context, R.string.result_reward_limit_reached, Toast.LENGTH_SHORT).show()
                }
                is ResultEffect.RewardGranted -> {
                    Toast.makeText(context, R.string.result_reward_granted, Toast.LENGTH_LONG).show()
                }
                is ResultEffect.RewardGrantFailed -> {
                    Toast.makeText(context, R.string.result_reward_grant_failed, Toast.LENGTH_SHORT).show()
                }
                is ResultEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ResultScreen(
        score = score,
        total = total,
        message = message,
        showOfferDialog = showOffer,
        onOfferConfirm = {
            showOffer = false
            scope.launch {
                val success = rewardedHelper.tryShow(activity, isPremium = isPremium)
                if (success) {
                    viewModel.onRewardGranted()
                } else {
                    Toast.makeText(context, R.string.result_ad_load_failed, Toast.LENGTH_SHORT).show()
                }
            }
        },
        onOfferDismiss = { showOffer = false },
        onNextSet = {
            val canRequestAds = ConsentManager.canRequestAds(context)
            viewModel.onNextSetClicked(canRequestAds)
        },
        onReviewSameOrder = onReviewSameOrder,
        onReviewList = { viewModel.onReviewClicked() },
        onBackToHome = onBackToHome,
        onNavUp = onBackToHome
    )
}
