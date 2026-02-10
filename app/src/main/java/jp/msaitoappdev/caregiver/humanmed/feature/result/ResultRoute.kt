package jp.msaitoappdev.caregiver.humanmed.feature.result

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeEffect
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * クイズ1セット完了後の結果を表示する画面のルート Composable。
 * この画面は UI の表示と、ユーザーのアクションを ViewModel に通知する責務のみを持つ。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultRoute(
    navController: NavController,
    score: Int,
    total: Int
) {
    val TAG = "ResultRoute"
    val pct: Int = if (total == 0) 0 else ((score.toFloat() / total) * 100).toInt()
    val message = when {
        pct >= 90 -> "素晴らしい！ほぼ完璧です。"
        pct >= 70 -> "とても良いです。あと少し！"
        pct >= 50 -> "合格ライン目前。復習しましょう。"
        else      -> "まずは基礎から振り返ってみましょう。"
    }

    val saver: ScoreSaverViewModel = hiltViewModel()
    val homeVm: HomeViewModel = hiltViewModel()

    // この画面が表示された最初のタイミングで、今回のスコアをDBに保存する
    LaunchedEffect(Unit) {
        saver.save(
            ScoreEntry(
                timestamp = System.currentTimeMillis(),
                score = score,
                total = total,
                percent = pct
            )
        )
    }

    val activity = LocalContext.current as Activity
    val context = LocalContext.current
    var showOffer by remember { mutableStateOf(false) } // リワード広告提案ダイアログの表示状態

    // ViewModel からの一度きりのイベント (Effect) を監視・処理する
    LaunchedEffect(homeVm.effect) {
        homeVm.effect.collectLatest {
            Log.d(TAG, "Effect received: $it")
            when (it) {
                is HomeEffect.LoadNextQuizSet -> {
                    // ViewModel の指示に従い、次のクイズセットをロードするために前の画面に戻る
                    Log.d(TAG, "LoadNextQuizSet event received. Navigating back.")
                    navController.previousBackStackEntry?.savedStateHandle?.set("action", "loadNext")
                    navController.popBackStack()
                }
                is HomeEffect.RewardGrantedAndNavigate -> {
                    // リワード広告視聴後、報酬付与に成功したため、次のクイズセットをロードして遷移する
                    Log.d(TAG, "RewardGrantedAndNavigate event received. Navigating back to load next set.")
                    navController.previousBackStackEntry?.savedStateHandle?.set("action", "loadNext")
                    navController.popBackStack()
                }
                is HomeEffect.ShowRewardedAdOffer -> {
                    Log.d(TAG, "ShowRewardedAdOffer event received. Displaying dialog.")
                    showOffer = true
                }
                is HomeEffect.ShowMessage -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("結果") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score / $total",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(text = "$pct%", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (pct / 100f) },
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(32.dp))

            // 「次の3問へ」ボタンは、クリックイベントを ViewModel に通知するだけ
            Button(
                onClick = { homeVm.onNextSetClicked(activity) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("次の3問へ") }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("action", "reset_same_order")
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("同じ順番で復習する") }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate(NavRoutes.REVIEW) {
                        popUpTo(NavRoutes.QUIZ) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("復習一覧を見る") }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate(NavRoutes.HISTORY) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("スコア履歴を見る") }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = { navController.popBackStack(NavRoutes.HOME, inclusive = false) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ホームへ戻る") }
        }
    }

    // ViewModelからの指示があった場合にのみ、リワード広告の提案ダイアログを表示する
    if (showOffer) {
        var isProcessing by remember { mutableStateOf(false) } // 広告視聴処理中フラグ
        AlertDialog(
            onDismissRequest = { if (!isProcessing) showOffer = false },
            title = { Text("今日は無料分が終了しました") },
            text = { Text("動画を視聴すると +1 セット解放できます。視聴しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isProcessing = true // 処理開始
                        showOffer = false
                        jp.msaitoappdev.caregiver.humanmed.ads.RewardedHelper.show(
                            activity = activity,
                            // このダイアログが表示される時点で、広告表示の可否はViewModelで判断済み。
                            // そのため、UI側では常にtrueを渡す。
                            canShowToday = { true },
                            onEarned = { _ ->
                                Log.d(TAG, "onEarned callback triggered, calling homeVm.onRewardedAdEarned()")
                                // Compose スコープに依存せず、ViewModel に直接処理を委譲する
                                homeVm.onRewardedAdEarned()
                            },
                            onFail = {
                                isProcessing = false // 処理失敗時、フラグをリセット
                                Toast.makeText(activity, "動画を読み込めませんでした（ネットワーク/在庫/初期化）", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !isProcessing // 処理中はボタン無効化
                ) { Text("動画を視聴して +1 セット") }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isProcessing) showOffer = false },
                    enabled = !isProcessing // 処理中はボタン無効化
                ) { Text("キャンセル") }
            }
        )
    }
}
