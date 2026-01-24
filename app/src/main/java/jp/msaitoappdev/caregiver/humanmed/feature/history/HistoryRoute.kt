package jp.msaitoappdev.caregiver.humanmed.feature.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRoute(navController: NavController) {
    val vm: HistoryVM = hiltViewModel()
    var list by remember { mutableStateOf<List<ScoreEntry>>(emptyList()) }

    // 履歴を購読
    LaunchedEffect(Unit) {
        vm.observe().collect { list = it }
    }

    val sdf = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                    // 履歴があるときのみ削除アイコンを表示
                    if (list.isNotEmpty()) {
                        IconButton(onClick = { showConfirm = true }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = "履歴を全て削除"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                                Text(
                                    "${rec.score} / ${rec.total} （${rec.percent}%）",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    sdf.format(Date(rec.timestamp)),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
            // ★ 下部の「履歴を全て削除」ボタンは撤去（TopBar 集約のため）
        }
    }

    // 確認ダイアログ
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("履歴を全て削除しますか？") },
            text = { Text("この操作は元に戻せません。") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    scope.launch {
                        vm.clear()
                        snackbarHostState.showSnackbar("履歴を削除しました")
                    }
                }) { Text("削除する") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("キャンセル") }
            }
        )
    }
}
