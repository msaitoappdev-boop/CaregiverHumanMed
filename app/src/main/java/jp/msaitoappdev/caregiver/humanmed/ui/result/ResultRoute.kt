
package jp.msaitoappdev.caregiver.humanmed.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.roundToInt

import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import jp.msaitoappdev.caregiver.humanmed.data.score.AppDatabase
import jp.msaitoappdev.caregiver.humanmed.data.score.ScoreRecord
import jp.msaitoappdev.caregiver.humanmed.data.score.ScoreRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultRoute(
    navController: NavController,
    score: Int,
    total: Int
) {
    val pct: Int = if (total == 0) 0 else ((score.toFloat() / total) * 100).roundToInt()
    val message = when {
        pct >= 90 -> "素晴らしい！ほぼ完璧です。"
        pct >= 70 -> "とても良いです。あと少し！"
        pct >= 50 -> "合格ライン目前。復習しましょう。"
        else      -> "まずは基礎から振り返ってみましょう。"
    }


    // ★ 追加: 保存処理（初回 Compose 時に1回だけ）
    val context = LocalContext.current
    val scoreRepo = remember {
        ScoreRepository(AppDatabase.get(context).scoreDao())
    }
    LaunchedEffect(Unit) {
        scoreRepo.add(
            ScoreRecord(
                timestamp = System.currentTimeMillis(),
                score = score,
                total = total,
                percent = pct
            )
        )
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
                progress = {(pct / 100f)},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(32.dp))

            // 再挑戦（新しい順番でシャッフル）
            Button(

                onClick = {
                    val quizEntry = runCatching { navController.getBackStackEntry("quiz") }.getOrNull()
                    quizEntry?.savedStateHandle?.set("reshuffle", true)
                    // ★ tick を毎回更新して発火を保証
                    quizEntry?.savedStateHandle?.set("reshuffleTick", System.currentTimeMillis())
                    navController.popBackStack("quiz", inclusive = false)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("再挑戦（新しい順番でシャッフル）")
            }

            Spacer(Modifier.height(12.dp))

            // 同じ順番で復習（シャッフルせず最初から）
            OutlinedButton(

                onClick = {
                    val quizEntry = runCatching { navController.getBackStackEntry("quiz") }.getOrNull()
                    quizEntry?.savedStateHandle?.set("reshuffle", false)
                    // ★ こちらも tick を更新
                    quizEntry?.savedStateHandle?.set("reshuffleTick", System.currentTimeMillis())
                    navController.popBackStack("quiz", inclusive = false)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("同じ順番で復習する")
            }

            Spacer(Modifier.height(12.dp))

            // 途中のボタン群の前後など、適切な位置に追加
            Button(
                onClick = {
                    // quiz は backstack に残っている前提（popUpTo("quiz", inclusive=false)）
                    navController.navigate("review") {
                        popUpTo("quiz") { inclusive = false } // quiz を残す（VM維持）
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("復習一覧を見る")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate("history") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("スコア履歴を見る") }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = { navController.popBackStack("home", inclusive = false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ホームへ戻る")
            }
        }
    }
}
