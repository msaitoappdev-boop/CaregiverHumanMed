package jp.msaitoappdev.caregiver.humanmed.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import jp.msaitoappdev.caregiver.humanmed.data.score.AppDatabase
import jp.msaitoappdev.caregiver.humanmed.data.score.ScoreRecord
import jp.msaitoappdev.caregiver.humanmed.data.score.ScoreRepository
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRoute(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { ScoreRepository(AppDatabase.get(context).scoreDao()) }

    var list by remember { mutableStateOf<List<ScoreRecord>>(emptyList()) }

    // 履歴を購読
    LaunchedEffect(Unit) {
        repo.history.collectLatest { list = it }
    }

    val sdf = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("スコア履歴") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // クリア
                        // LaunchedEffect内ではないので rememberCoroutineScope を使ってもOK
                    }) { /* 後述でボタン実装 */ }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (list.isEmpty()) {
                Text("履歴はまだありません")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(list) { rec ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${rec.score} / ${rec.total} （${rec.percent}%）",
                                    style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(sdf.format(Date(rec.timestamp)),
                                    style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // クリアボタン
            val scope = rememberCoroutineScope()
            Button(
                onClick = { scope.launch { repo.clear() } },  // ← コルーチン内で呼ぶ
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("履歴を全て削除")
            }

        }
    }
}
