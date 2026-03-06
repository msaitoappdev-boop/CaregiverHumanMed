package com.msaitodev.quiz.feature.main.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msaitodev.quiz.feature.main.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeViewModel.HomeUiState,
    showOfferDialog: Boolean,
    onStartQuiz: () -> Unit,
    onStartWeaknessTraining: () -> Unit,
    onAnalysisClicked: () -> Unit,
    onViewHistory: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenSettings: () -> Unit,
    onOfferConfirm: () -> Unit,
    onOfferDismiss: () -> Unit,
    onReminderInvitationDismiss: () -> Unit,
    onReminderInvitationClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    if (!uiState.isPremium) {
                        IconButton(onClick = onUpgrade) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = stringResource(R.string.home_upgrade_to_premium),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            // リマインド誘導カード
            if (uiState.showReminderInvitation) {
                ReminderInvitationCard(
                    onDismiss = onReminderInvitationDismiss,
                    onClick = onReminderInvitationClick
                )
                Spacer(Modifier.height(24.dp))
            }

            // メインアクション: 通常のクイズ
            Button(
                onClick = onStartQuiz,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(stringResource(R.string.home_start_quiz))
            }

            Spacer(Modifier.height(16.dp))

            // 弱点特訓
            OutlinedButton(
                onClick = onStartWeaknessTraining,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.home_weakness_training))
                    Text(
                        text = stringResource(R.string.home_premium_label),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    if (!uiState.isPremium) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 学習分析
            OutlinedButton(
                onClick = onAnalysisClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("学習分析")
                    Text(
                        text = stringResource(R.string.home_premium_label),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    if (!uiState.isPremium) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onViewHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.home_view_history))
            }
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
}

@Composable
private fun ReminderInvitationCard(
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.home_reminder_invitation_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.home_reminder_invitation_dismiss),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.home_reminder_invitation_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.home_reminder_invitation_action))
            }
        }
    }
}
