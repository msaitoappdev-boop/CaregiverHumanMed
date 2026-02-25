package com.msaitodev.quiz.feature.main.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msaitodev.quiz.feature.main.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    showOfferDialog: Boolean,
    showRationale: Boolean,
    onStartQuiz: () -> Unit,
    onUpgrade: () -> Unit,
    onSetReminder: () -> Unit,
    onOpenSettings: () -> Unit,
    onOfferConfirm: () -> Unit,
    onOfferDismiss: () -> Unit,
    onRationaleConfirm: () -> Unit,
    onRationaleDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(onClick = onStartQuiz) {
                Text(stringResource(R.string.home_start_quiz))
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onUpgrade) { Text(stringResource(R.string.home_upgrade_to_premium)) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSetReminder) { Text(stringResource(R.string.home_set_reminder)) }
        }
    }

    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = onOfferDismiss,
            title = { Text(stringResource(R.string.dialog_rewarded_ad_title)) },
            text = { Text(stringResource(R.string.dialog_rewarded_ad_message)) },
            confirmButton = {
                TextButton(onClick = onOfferConfirm) { Text(stringResource(R.string.dialog_rewarded_ad_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onOfferDismiss) { Text(stringResource(R.string.dialog_rewarded_ad_dismiss)) }
            }
        )
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = onRationaleDismiss,
            title = { Text(stringResource(R.string.dialog_notification_permission_title)) },
            text = { Text(stringResource(R.string.dialog_notification_permission_message)) },
            confirmButton = {
                TextButton(onClick = onRationaleConfirm) { Text(stringResource(R.string.dialog_permission_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onRationaleDismiss) { Text(stringResource(R.string.dialog_permission_dismiss)) }
            }
        )
    }
}