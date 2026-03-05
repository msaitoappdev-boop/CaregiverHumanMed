package com.msaitodev.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msaitodev.core.notifications.ReminderScheduler
import com.msaitodev.feature.settings.R

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.RestoreResult -> {
                    val messageResId = when (event.status) {
                        SettingsEvent.RestoreStatus.SUCCESS -> R.string.settings_restore_success
                        SettingsEvent.RestoreStatus.NO_PURCHASE -> R.string.settings_restore_no_purchase
                        SettingsEvent.RestoreStatus.ERROR -> R.string.settings_restore_error
                    }
                    Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
            ReminderScheduler.scheduleDaily(context, uiState.hour, uiState.minute)
        }
    }

    SettingsScreen(
        state = uiState,
        onBack = onBack,
        onReminderEnabledChange = { enabled ->
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.setReminderEnabled(true)
                    ReminderScheduler.scheduleDaily(context, uiState.hour, uiState.minute)
                }
            } else {
                viewModel.setReminderEnabled(false)
                ReminderScheduler.cancel(context)
            }
        },
        onTimeChange = { h, m ->
            viewModel.setReminderTime(h, m)
            if (uiState.reminderEnabled) {
                ReminderScheduler.scheduleDaily(context, h, m)
            }
        },
        onWeaknessModeChange = { enabled, categoryId ->
            viewModel.setWeaknessModeEnabled(enabled, categoryId)
        },
        onRestorePurchases = {
            viewModel.restorePurchases()
        },
        onManageSubscription = {
            val url = viewModel.subscriptionManagementUrl
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        },
        onOpenPrivacyPolicy = {
            val url = viewModel.privacyPolicyUrl
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    )
}
