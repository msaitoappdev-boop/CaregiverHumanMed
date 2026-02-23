package com.msaitodev.quiz.feature.result

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ResultRoute(
    score: Int,
    total: Int,
    onNextSet: () -> Unit,
    onReview: (questionsJson: String, answersJson: String) -> Unit,
    onReviewSameOrder: () -> Unit,
    onShowScoreHistory: () -> Unit,
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

    LaunchedEffect(Unit) {
        viewModel.onScreenShown(score, total, pct)
    }

    val context = LocalContext.current
    var showOffer by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
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
            viewModel.onOfferConfirmed(activity)
        },
        onOfferDismiss = { showOffer = false },
        onNextSet = { viewModel.onNextSetClicked() },
        onReviewSameOrder = onReviewSameOrder,
        onReviewList = { viewModel.onReviewClicked() },
        onScoreHistory = onShowScoreHistory,
        onBackToHome = onBackToHome,
        onNavUp = onBackToHome
    )
}
