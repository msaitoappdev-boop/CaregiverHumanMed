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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeVM
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.QuizViewModel

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

    val onNextSet: () -> Unit = {
        Log.d(TAG, "onNextSet clicked. Setting action 'loadNext' to previous back stack entry.")
        navController.previousBackStackEntry?.savedStateHandle?.set("action", "loadNext")
        navController.popBackStack()
    }

    val onRetrySame: () -> Unit = {
        Log.d(TAG, "onRetrySame clicked. Setting action 'reset_same_order' to previous back stack entry.")
        navController.previousBackStackEntry?.savedStateHandle?.set("action", "reset_same_order")
        navController.popBackStack()
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
                onClick = onNextSet,
                modifier = Modifier.fillMaxWidth()
            ) { Text("次の3問へ") }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRetrySame,
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
}
