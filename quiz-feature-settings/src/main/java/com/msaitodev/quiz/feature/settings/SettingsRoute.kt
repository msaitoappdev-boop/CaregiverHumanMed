package com.msaitodev.quiz.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.msaitodev.quiz.core.notifications.DailyReminderWorker
import com.msaitodev.quiz.core.notifications.ReminderScheduler
import java.util.concurrent.TimeUnit

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.settings.collectAsStateWithLifecycle()

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
            ReminderScheduler.scheduleDaily(context, state.hour, state.minute)
        }
    }

    SettingsScreen(
        state = state,
        onBack = onBack,
        onReminderEnabledChange = { enabled ->
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.setReminderEnabled(true)
                    ReminderScheduler.scheduleDaily(context, state.hour, state.minute)
                }
            } else {
                viewModel.setReminderEnabled(false)
                ReminderScheduler.cancel(context)
            }
        },
        onTimeChange = { h, m ->
            viewModel.setReminderTime(h, m)
            if (state.enabled) {
                ReminderScheduler.scheduleDaily(context, h, m)
            }
        },
        onTestReminder = {
            val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        },
        onRestorePurchases = {
            viewModel.restorePurchases()
        },
        onManageSubscription = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
            context.startActivity(intent)
        },
        onOpenPrivacyPolicy = {
            val url = context.getString(R.string.privacy_policy_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    )
}
