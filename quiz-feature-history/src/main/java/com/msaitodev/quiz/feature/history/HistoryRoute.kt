package com.msaitodev.quiz.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * スコア履歴画面の Route。
 * ViewModel の取得、状態の購読、イベントの橋渡しを担当する。
 * @param dateKey フィルタリングする日付 (yyyyMMdd)。オプション。
 */
@Composable
fun HistoryRoute(
    navController: NavController,
    dateKey: String? = null
) {
    val vm: HistoryViewModel = hiltViewModel()
    
    // dateKey が変更されたら ViewModel のフィルターを更新
    LaunchedEffect(dateKey) {
        vm.setDateFilter(dateKey)
    }

    // UI 状態の収集 (LCO 準拠)
    val list by vm.history.collectAsStateWithLifecycle(initialValue = emptyList())

    // タイトルの生成
    val baseTitle = stringResource(R.string.history_title)
    val displayTitle = if (dateKey != null && dateKey.length == 8) {
        try {
            val inputSdf = SimpleDateFormat("yyyyMMdd", Locale.US)
            val outputSdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val date = inputSdf.parse(dateKey)
            if (date != null) {
                "$baseTitle (${outputSdf.format(date)})"
            } else {
                baseTitle
            }
        } catch (_: Exception) {
            baseTitle
        }
    } else {
        baseTitle
    }

    // 表示専任の Screen へ委譲
    HistoryScreen(
        historyList = list,
        displayTitle = displayTitle,
        onBack = { navController.popBackStack() }
    )
}
