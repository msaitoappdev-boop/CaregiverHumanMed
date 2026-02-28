package com.msaitodev.quiz.feature.main.home

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

@Composable
fun HomeRoute(
    onStartQuiz: () -> Unit,
    onShowRewardedAd: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val vm: HomeViewModel = hiltViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()
    var showOfferDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(vm.event) {
        vm.event.collect {
            when (it) {
                is HomeEvent.RequestNavigateToQuiz -> onStartQuiz()
                is HomeEvent.RequestShowRewardedAdOffer -> showOfferDialog = true
                is HomeEvent.ShowMessage -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HomeScreen(
        showOfferDialog = showOfferDialog,
        onStartQuiz = { vm.onStartQuizClicked() },
        onUpgrade = onUpgrade,
        onOpenSettings = onOpenSettings,
        onOfferConfirm = {
            showOfferDialog = false
            onShowRewardedAd()
        },
        onOfferDismiss = { showOfferDialog = false }
    )
}
