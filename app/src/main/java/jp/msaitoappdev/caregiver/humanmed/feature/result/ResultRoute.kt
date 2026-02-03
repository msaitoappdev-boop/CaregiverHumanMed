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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.firebase.remoteconfig.ktx.remoteConfig
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeVM

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

    // スコア保存（既存どおり）
    val saver: ScoreSaverVM = hiltViewModel()
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

    // ⚠️ セット完了の +1 は NavHost 側に統一するため、ResultRoute からは削除
    // val quotaSaver: QuotaSaverVM = hiltViewModel()
    // LaunchedEffect(Unit) { quotaSaver.markFinished() }

    val ctx = LocalContext.current
    val activity = ctx as Activity

    // RC（インタースティシャル制御）— 既存のまま
    val rc = com.google.firebase.ktx.Firebase.remoteConfig
    val enabled = rc.getBoolean("interstitial_enabled")
    val cap = rc.getLong("interstitial_cap_per_session").toInt()
    val intervalSec = rc.getLong("inter_session_interval_sec")

    // 事前ロード & 表示（既存のまま）
    LaunchedEffect(Unit) {
        jp.msaitoappdev.caregiver.humanmed.ads.InterstitialHelper.preload(ctx)
    }
    LaunchedEffect(score to total) {
        jp.msaitoappdev.caregiver.humanmed.ads.InterstitialHelper.tryShow(
            activity = activity,
            enabled = enabled,
            sessionCap = cap,
            minIntervalSec = intervalSec,
            onNotShown = { /* 何もしない */ }
        )
    }

    // 🔸 枠ゲート：canStart を購読
    val homeVm: HomeVM = hiltViewModel()
    val canStart by homeVm.canStartFlow.collectAsStateWithLifecycle()

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(32.dp))

            // 再挑戦（シャッフル）— 枠ゲート
            Button(
                enabled = canStart,
                onClick = {
                    if (!canStart) {
                        Toast.makeText(ctx, "本日の枠は終了しました", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val quizEntry = runCatching { navController.getBackStackEntry("quiz") }.getOrNull()
                    quizEntry?.savedStateHandle?.set("reshuffle", true)
                    quizEntry?.savedStateHandle?.set("reshuffleTick", System.currentTimeMillis())
                    navController.popBackStack("quiz", inclusive = false)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("再挑戦（新しい順番でシャッフル）") }

            Spacer(Modifier.height(12.dp))

            // 同じ順番で復習 — 枠ゲート
            OutlinedButton(
                enabled = canStart,
                onClick = {
                    if (!canStart) {
                        Toast.makeText(ctx, "本日の枠は終了しました", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    val quizEntry = runCatching { navController.getBackStackEntry("quiz") }.getOrNull()
                    quizEntry?.savedStateHandle?.set("reshuffle", false)
                    quizEntry?.savedStateHandle?.set("reshuffleTick", System.currentTimeMillis())
                    navController.popBackStack("quiz", inclusive = false)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("同じ順番で復習する") }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate("review") {
                        popUpTo("quiz") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("復習一覧を見る") }

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
            ) { Text("ホームへ戻る") }
        }
    }
}