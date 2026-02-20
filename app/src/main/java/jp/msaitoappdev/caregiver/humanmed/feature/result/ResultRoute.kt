package jp.msaitoappdev.caregiver.humanmed.feature.result

import android.app.Activity
import android.util.Log
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
import androidx.navigation.NavController
import jp.msaitoappdev.caregiver.humanmed.R
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeEffect
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ResultRoute(
    navController: NavController,
    score: Int,
    total: Int
) {
    val TAG = "ResultRoute"
    val pct: Int = if (total == 0) 0 else ((score.toFloat() / total) * 100).toInt()
    val message = when {
        pct >= 90 -> stringResource(id = R.string.result_excellent)
        pct >= 70 -> stringResource(id = R.string.result_good)
        pct >= 50 -> stringResource(id = R.string.result_average)
        else -> stringResource(id = R.string.result_poor)
    }

    val saver: ScoreSaverViewModel = hiltViewModel()
    val homeVm: HomeViewModel = hiltViewModel()

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

    val activity = LocalContext.current as Activity
    val context = LocalContext.current
    var showOffer by remember { mutableStateOf(false) }
    val rewardedAdError = stringResource(id = R.string.common_error_rewarded_ad)

    LaunchedEffect(homeVm.effect) {
        homeVm.effect.collectLatest {
            Log.d(TAG, "Effect received: $it")
            when (it) {
                is HomeEffect.LoadNextQuizSet, is HomeEffect.RewardGrantedAndNavigate -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set("action", "loadNext")
                    navController.previousBackStackEntry?.savedStateHandle?.set("action_tick", System.currentTimeMillis())
                    navController.popBackStack()
                }
                is HomeEffect.ShowRewardedAdOffer -> {
                    showOffer = true
                }
                is HomeEffect.ShowMessage -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    val onReviewSameOrder: () -> Unit = {
        val quizEntryBack = navController.previousBackStackEntry
        quizEntryBack?.savedStateHandle?.set("reshuffle", false)
        quizEntryBack?.savedStateHandle?.set("is_review", true)
        quizEntryBack?.savedStateHandle?.set("reshuffleTick", System.currentTimeMillis())
        navController.popBackStack()
    }
    val onBackToHome: () -> Unit = {
        navController.popBackStack(NavRoutes.HOME, inclusive = false)
    }
    val onNavUp: () -> Unit = {
        navController.popBackStack()
    }

    val onOfferConfirm = {
        showOffer = false
        jp.msaitoappdev.caregiver.humanmed.ads.RewardedHelper.show(
            activity = activity,
            canShowToday = { true },
            onEarned = { _ ->
                homeVm.onRewardedAdEarned()
            },
            onFail = {
                Toast.makeText(activity, rewardedAdError, Toast.LENGTH_SHORT).show()
            }
        )
    }

    ResultScreen(
        score = score,
        total = total,
        message = message,
        showOfferDialog = showOffer,
        onOfferConfirm = onOfferConfirm,
        onOfferDismiss = { showOffer = false },
        onNextSet = { homeVm.onNextSetClicked(activity) },
        onReviewSameOrder = onReviewSameOrder,
        onReviewList = { navController.navigate(NavRoutes.REVIEW) },
        onScoreHistory = { navController.navigate(NavRoutes.HISTORY) },
        onBackToHome = onBackToHome,
        onNavUp = onNavUp
    )
}
