package jp.msaitoappdev.caregiver.humanmed.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.settings.collectAsState(
        initial = ReminderSettings(enabled = false, hour = 20, minute = 0)
    )

    // POST_NOTIFICATIONS ランチャ（Android 13+）
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                viewModel.setEnabled(context, true, state.hour, state.minute)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("リマインド通知", style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("毎日リマインドを受け取る")
                        Switch(
                            checked = state.enabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        // 許可ダイアログ（事前説明は別途ダイアログ化も可）
                                        requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        scope.launch { viewModel.setEnabled(context, true, state.hour, state.minute) }
                                    }
                                } else {
                                    scope.launch { viewModel.setEnabled(context, false, state.hour, state.minute) }
                                }
                            }
                        )
                    }

                    // 時刻設定（簡易：Hour [0..23], Minute [0,15,30,45]）
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeDropdown(
                            label = "時",
                            options = (0..23).toList(),
                            selected = state.hour,
                            onSelected = { h -> scope.launch { viewModel.setTime(context, h, state.minute) } }
                        )
                        TimeDropdown(
                            label = "分",
                            options = listOf(0, 15, 30, 45),
                            selected = state.minute,
                            onSelected = { m -> scope.launch { viewModel.setTime(context, state.hour, m) } }
                        )
                    }

                    Text(
                        "指定時刻（既定 20:00）に1日1回通知します。Doze等の省電力により前後する場合があります。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 定期購入の管理（Google Play）
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("定期購入を管理（Google Play）") }

            // プライバシーポリシー
            Button(
                onClick = {
                    val url = "https://doc-hosting.flycricket.io/jie-hu-fu-zhi-shi-mei-ri-3wen-torena-privacy-policy/c742ebfd-1960-4204-9f26-898770c8be48/privacy"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("プライバシーポリシーを開く") }
        }
    }
}

@Composable
private fun <T> TimeDropdown(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
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
