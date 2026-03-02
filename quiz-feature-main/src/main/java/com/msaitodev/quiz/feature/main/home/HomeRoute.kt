package com.msaitodev.quiz.feature.main.home

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msaitodev.core.ads.RewardedHelper
import com.msaitodev.quiz.feature.main.R

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
        onStartQuiz = { viewModel.onStartQuizClicked() },
        onViewHistory = onViewHistory,
        onUpgrade = onUpgrade,
        onOpenSettings = onOpenSettings,
        onOfferConfirm = {
            showOfferDialog = false
            rewardedHelper.show(
                activity = activity,
                canShowToday = { true },
                onEarned = { viewModel.onRewardGranted() },
                onFail = {
                    Toast.makeText(context, R.string.home_ad_load_failed, Toast.LENGTH_SHORT).show()
                }
            )
        },
        onOfferDismiss = { showOfferDialog = false }
    )
}
