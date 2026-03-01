package com.msaitodev.quiz.feature.history

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import kotlinx.coroutines.launch

/**
 * スコア履歴画面の Route。
 * ViewModel の取得、状態の購読、イベントの橋渡しを担当する。
 */
@Composable
fun HistoryRoute(navController: NavController) {
    val vm: HistoryViewModel = hiltViewModel()
    
    // UI 状態の収集 (collectAsStateWithLifecycle が推奨だが、現状の実装に合わせて collect を使用)
    var list by remember { mutableStateOf<List<ScoreEntry>>(emptyList()) }
    LaunchedEffect(Unit) {
        vm.observe().collect { list = it }
    }

    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // 表示専任の Screen へ委譲
    HistoryScreen(
        historyList = list,
        onBack = { navController.popBackStack() },
        onDeleteAll = { showConfirm = true }
    )

    // 削除確認ダイアログの制御
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("履歴を全て削除しますか？") },
            text = { Text("この操作は元に戻せません。") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    scope.launch {
                        vm.clearHistory()
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
