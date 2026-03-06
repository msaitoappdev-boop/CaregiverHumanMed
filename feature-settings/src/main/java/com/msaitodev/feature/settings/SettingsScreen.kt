package com.msaitodev.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.TextButton
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
import com.msaitodev.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onWeaknessModeChange: (Boolean, String?) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageSubscription: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    val scrollState = rememberScrollState()

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
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 学習モード設定
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.weaknessModeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (!state.isPremium) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Premium Only",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.weaknessModeDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isPremium) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = state.isWeaknessMode,
                            onCheckedChange = { onWeaknessModeChange(it, null) },
                            enabled = state.isPremium
                        )
                    }

                    // 分野指定が有効な場合の解除ボタン
                    if (state.isWeaknessMode && state.weaknessCategoryName != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "特訓中: ${state.weaknessCategoryName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = { onWeaknessModeChange(true, null) }) {
                                Text("全分野へ戻す")
                            }
                        }
                    }
                }
            }

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

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onRestorePurchases,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isPremium
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

            // プライバシーポリシーへのリンク（最下部に配置）
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
