package jp.msaitoappdev.caregiver.humanmed.feature.result

import android.app.Activity
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeVM
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultRoute(
    navController: NavController,
    score: Int,
    total: Int
) {
    val pct: Int = if (total == 0) 0 else ((score.toFloat() / total) * 100).toInt()
    val message = when {
        pct >= 90 -> "素晴らしい！ほぼ完璧です。"
        pct >= 70 -> "とても良いです。あと少し！"
        pct >= 50 -> "合格ライン目前。復習しましょう。"
        else      -> "まずは基礎から振り返ってみましょう。"
    }

    val saver: ScoreSaverVM = hiltViewModel()
    val homeVm: HomeVM = hiltViewModel()

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

    val canStart by homeVm.canStartFlow.collectAsStateWithLifecycle(initialValue = false)
    val rewardedCountToday by homeVm.rewardedCountToday.collectAsStateWithLifecycle(initialValue = 0)
    val ui by homeVm.uiState.collectAsStateWithLifecycle()
    val canWatchReward = ui.rewardedEnabled && rewardedCountToday < 1
    
    var showOffer by remember { mutableStateOf(false) }
    var reshuffleOnReward by remember { mutableStateOf(true) } 
    val scope = rememberCoroutineScope()

    val onRetry: (reshuffle: Boolean) -> Unit = {
        if (canStart) {
            val quizEntry = runCatching { navController.getBackStackEntry(NavRoutes.QUIZ) }.getOrNull()
            quizEntry?.savedStateHandle?.set("reshuffle", it)
            quizEntry?.savedStateHandle?.set("reshuffleTick", System.currentTimeMillis())
            navController.popBackStack(NavRoutes.QUIZ, inclusive = false)
        } else {
            reshuffleOnReward = it
            homeVm.showInterstitialAdIfNeeded(activity) {
                showOffer = true
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

            Button(
                onClick = { onRetry(true) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("再挑戦（新しい順番でシャッフル）") }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onRetry(false) },
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

    if (showOffer) {
        if (!canWatchReward) {
            LaunchedEffect(Unit) {
                showOffer = false
                Toast.makeText(activity, "本日は動画視聴による付与は上限です", Toast.LENGTH_SHORT).show()
            }
        } else {
            AlertDialog(
                onDismissRequest = { showOffer = false },
                title = { Text("今日は無料分が終了しました") },
                text = { Text("動画を視聴すると +1 セット解放できます。視聴しますか？") },
                confirmButton = {
                    TextButton(onClick = {
                        showOffer = false
                        jp.msaitoappdev.caregiver.humanmed.ads.RewardedHelper.show(
                            activity = activity,
                            canShowToday = { rewardedCountToday < 1 },
                            onEarned = { _ ->
                                scope.launch {
                                    val ok = homeVm.tryGrantDailyPlusOne()
                                    if (ok) {
                                        val quizEntry = runCatching { navController.getBackStackEntry(NavRoutes.QUIZ) }.getOrNull()
                                        quizEntry?.savedStateHandle?.set("reshuffle", reshuffleOnReward)
                                        quizEntry?.savedStateHandle?.set("reshuffleTick", System.currentTimeMillis())
                                        navController.popBackStack(NavRoutes.QUIZ, inclusive = false)
                                    } else {
                                        Toast.makeText(activity, "本日はすでに付与済みです", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onFail = {
                                Toast.makeText(activity, "動画を読み込めませんでした（ネットワーク/在庫/初期化）", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }) { Text("動画を視聴して +1 セット") }
                },
                dismissButton = {
                    TextButton(onClick = { showOffer = false }) { Text("キャンセル") }
                }
            )
        }
    }
}
