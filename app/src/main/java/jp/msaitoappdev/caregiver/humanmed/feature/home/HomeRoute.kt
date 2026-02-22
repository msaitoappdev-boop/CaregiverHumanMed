package jp.msaitoappdev.caregiver.humanmed.feature.home

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.msaitoappdev.caregiver.humanmed.R
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    onStartQuiz: () -> Unit,
    onShowRewardedAd: () -> Unit, // リワード広告表示の責務を外部に委譲
    onUpgrade: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val vm: HomeViewModel = hiltViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()
    var showOfferDialog by remember { mutableStateOf(false) } // ダイアログの表示状態のみを管理
    val context = LocalContext.current

    // ViewModelからの一度きりのイベントを監視し、処理を外部のコールバックに委譲する
    LaunchedEffect(vm.event) {
        vm.event.collect {
            when (it) {
                is HomeEvent.RequestNavigateToQuiz -> onStartQuiz()
                is HomeEvent.RequestShowRewardedAdOffer -> showOfferDialog = true
                is HomeEvent.ShowMessage -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 通知許可をリクエストするためのランチャー
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // 許可された場合、リマインダーをスケジュールする
            ReminderScheduler.scheduleDaily(context, 20, 0)
        }
    }

    var showRationale by remember { mutableStateOf(false) } // 通知許可の根拠を示すダイアログの表示状態

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
            // このボタンはViewModelにイベントを通知するだけで、UI側ではロジックを持たない
            Button(onClick = { vm.onStartQuizClicked() }) {
                Text(stringResource(R.string.home_start_quiz))
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onUpgrade) { Text(stringResource(R.string.home_upgrade_to_premium)) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                // Android 13 (TIRAMISU) 以降では、通知許可を求めるダイアログを表示
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showRationale = true
                } else {
                    // それ以前のバージョンでは、許可なしでリマインダーを設定
                    ReminderScheduler.scheduleDaily(context, 20, 0)
                }
            }) { Text(stringResource(R.string.home_set_reminder)) }
        }
    }

    // リワード広告の提案ダイアログ
    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            title = { Text(stringResource(R.string.dialog_rewarded_ad_title)) },
            text = { Text(stringResource(R.string.dialog_rewarded_ad_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showOfferDialog = false
                    // 広告表示の実行を外部に委譲
                    onShowRewardedAd()
                }) { Text(stringResource(R.string.dialog_rewarded_ad_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showOfferDialog = false }) { Text(stringResource(R.string.dialog_rewarded_ad_dismiss)) }
            }
        )
    }

    // 通知許可の根拠を示すダイアログ
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringResource(R.string.dialog_notification_permission_title)) },
            text = { Text(stringResource(R.string.dialog_notification_permission_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text(stringResource(R.string.dialog_permission_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text(stringResource(R.string.dialog_permission_dismiss)) }
            }
        )
    }
}
