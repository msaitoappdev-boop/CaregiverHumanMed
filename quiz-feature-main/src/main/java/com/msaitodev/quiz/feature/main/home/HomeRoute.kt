package com.msaitodev.quiz.feature.main.home

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msaitodev.core.ads.ConsentManager
import com.msaitodev.core.ads.RewardedHelper
import com.msaitodev.quiz.feature.main.R
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    rewardedHelper: RewardedHelper,
    onStartQuiz: () -> Unit,
    onViewHistory: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showOfferDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.event) {
        viewModel.event.collect { event ->
            when (event) {
                is HomeEvent.RequestNavigateToQuiz -> onStartQuiz()
                is HomeEvent.RequestShowRewardedAdOffer -> showOfferDialog = true
                is HomeEvent.QuotaExceeded -> {
                    Toast.makeText(context, R.string.home_quota_exceeded, Toast.LENGTH_SHORT).show()
                }
                is HomeEvent.RewardLimitReached -> {
                    Toast.makeText(context, R.string.home_reward_limit_reached, Toast.LENGTH_SHORT).show()
                }
                is HomeEvent.RewardGranted -> {
                    Toast.makeText(context, R.string.home_reward_granted, Toast.LENGTH_LONG).show()
                }
                is HomeEvent.RewardGrantFailed -> {
                    Toast.makeText(context, R.string.home_reward_grant_failed, Toast.LENGTH_SHORT).show()
                }
                is HomeEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        showOfferDialog = showOfferDialog,
        onStartQuiz = {
            // 広告リクエストが可能か（UMP同意済みか）を判定して ViewModel に渡す
            val canRequestAds = ConsentManager.canRequestAds(context)
            viewModel.onStartQuizClicked(canRequestAds)
        },
        onViewHistory = onViewHistory,
        onUpgrade = onUpgrade,
        onOpenSettings = onOpenSettings,
        onOfferConfirm = {
            showOfferDialog = false
            scope.launch {
                // RewardedHelper.tryShow に購読状態を渡す
                // uiState.canShowFullExplanation は isPremium と同等
                val success = rewardedHelper.tryShow(activity, isPremium = uiState.canShowFullExplanation)
                if (success) {
                    viewModel.onRewardGranted()
                } else {
                    Toast.makeText(context, R.string.home_ad_load_failed, Toast.LENGTH_SHORT).show()
                }
            }
        },
        onOfferDismiss = { showOfferDialog = false }
    )
}
