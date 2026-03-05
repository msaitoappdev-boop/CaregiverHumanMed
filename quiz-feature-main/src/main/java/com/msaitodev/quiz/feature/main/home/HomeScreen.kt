package com.msaitodev.quiz.feature.main.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msaitodev.quiz.feature.main.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeViewModel.HomeUiState,
    showOfferDialog: Boolean,
    onStartQuiz: () -> Unit,
    onStartWeaknessTraining: () -> Unit,
    onViewHistory: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenSettings: () -> Unit,
    onOfferConfirm: () -> Unit,
    onOfferDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    if (!uiState.canShowFullExplanation) {
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
            // メインアクション: 通常のクイズ
            Button(
                onClick = onStartQuiz,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(stringResource(R.string.home_start_quiz))
            }

            Spacer(Modifier.height(16.dp))

            // 弱点特訓 (Premium): プレミアムなら常に活性。データ不足時はクリック後に通知。
            OutlinedButton(
                onClick = onStartWeaknessTraining,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading // 常に活性化
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.home_weakness_training))
                    Text(
                        text = stringResource(R.string.home_premium_label),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    if (uiState.isWeaknessTrainingLocked) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
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
