package jp.msaitoappdev.caregiver.humanmed.feature.home

import android.Manifest
import android.app.Activity
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
    onUpgrade: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val vm: HomeViewModel = hiltViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()
    var showOffer by remember { mutableStateOf(false) }
    val act = LocalContext.current as Activity
    val context = LocalContext.current

    // ViewModelからの一度きりのイベント(Effect)を監視・処理する
    LaunchedEffect(vm.effect) {
        vm.effect.collect {
            when (it) {
                is HomeEffect.NavigateToQuiz -> onStartQuiz()
                is HomeEffect.RewardGrantedAndNavigate -> onStartQuiz() // ★ Handle reward
                is HomeEffect.ShowRewardedAdOffer -> showOffer = true
                is HomeEffect.ShowMessage -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                else -> Unit
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
    val rewardedAdError = stringResource(id = R.string.common_error_rewarded_ad)

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

    // ViewModelからの指示があった場合にのみ、リワード広告の提案ダイアログを表示する
    if (showOffer) {
        AlertDialog(
            onDismissRequest = { showOffer = false },
            title = { Text(stringResource(R.string.dialog_rewarded_ad_title)) },
            text = { Text(stringResource(R.string.dialog_rewarded_ad_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showOffer = false
                    jp.msaitoappdev.caregiver.humanmed.ads.RewardedHelper.show(
                        activity = act,
                        // このダイアログが表示される時点で、広告表示の可否はViewModelで判断済み。
                        // そのため、UI側では常にtrueを渡す。
                        canShowToday = { true },
                        onEarned = { _ ->
                            vm.onRewardedAdEarned() // ★ Simply call the ViewModel method
                        },
                        onFail = {
                            Toast.makeText(context, rewardedAdError, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) { Text(stringResource(R.string.dialog_rewarded_ad_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showOffer = false }) { Text(stringResource(R.string.dialog_rewarded_ad_dismiss)) }
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
