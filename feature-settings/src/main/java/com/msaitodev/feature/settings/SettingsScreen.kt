package com.msaitodev.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageSubscription: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // リマインド設定セクション
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.settings_reminder_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.settings_reminder_switch_label))
                        Switch(
                            checked = state.reminderEnabled,
                            onCheckedChange = onReminderEnabledChange
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeDropdown(
                            stringResource(R.string.settings_reminder_hour_label),
                            (0..23).toList(),
                            state.hour
                        ) { h ->
                            onTimeChange(h, state.minute)
                        }
                        TimeDropdown(
                            stringResource(R.string.settings_reminder_minute_label),
                            (0..55 step 5).toList(),
                            state.minute
                        ) { m ->
                            onTimeChange(state.hour, m)
                        }
                    }
                    Text(
                        stringResource(R.string.settings_reminder_description),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // プランと購入管理セクション
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        stringResource(R.string.settings_premium_status_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 現在のステータスを表示
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.settings_current_plan_label),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (state.isPremium) {
                                stringResource(R.string.settings_premium_status_active)
                            } else {
                                stringResource(R.string.settings_premium_status_free)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = if (state.isPremium) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Text(
                        stringResource(R.string.settings_restore_description),
                        style = MaterialTheme.typography.bodySmall
                    )

                    // ボタン類
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onRestorePurchases,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isPremium // プレミアムなら無効化
                        ) {
                            Text(
                                if (state.isPremium) {
                                    stringResource(R.string.settings_restore_button_done)
                                } else {
                                    stringResource(R.string.settings_restore_button)
                                }
                            )
                        }
                        OutlinedButton(
                            onClick = onManageSubscription,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_manage_subscription))
                        }
                    }
                }
            }

            // その他
            OutlinedButton(
                onClick = onOpenPrivacyPolicy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_privacy_policy))
            }
        }
    }
}

@Composable
private fun <T> TimeDropdown(label: String, options: List<T>, selected: T, onSelected: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected.toString())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.toString()) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
