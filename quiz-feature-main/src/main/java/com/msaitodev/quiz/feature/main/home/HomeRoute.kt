package com.msaitodev.quiz.feature.main.home

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msaitodev.quiz.core.notifications.ReminderScheduler

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
    var showRationale by remember { mutableStateOf(false) }
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

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ReminderScheduler.scheduleDaily(context, 20, 0)
        }
    }

    HomeScreen(
        showOfferDialog = showOfferDialog,
        showRationale = showRationale,
        onStartQuiz = { vm.onStartQuizClicked() },
        onUpgrade = onUpgrade,
        onSetReminder = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showRationale = true
            } else {
                ReminderScheduler.scheduleDaily(context, 20, 0)
            }
        },
        onOpenSettings = onOpenSettings,
        onOfferConfirm = {
            showOfferDialog = false
            onShowRewardedAd()
        },
        onOfferDismiss = { showOfferDialog = false },
        onRationaleConfirm = {
            showRationale = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onRationaleDismiss = { showRationale = false }
    )
}