package com.msaitodev.quiz.feature.history

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.msaitodev.quiz.core.navigation.HistoryDestination

/**
 * スコア履歴画面のナビゲーション・グラフを構築する。
 */
fun NavGraphBuilder.historyGraph(navController: NavController) {
    composable(
        route = HistoryDestination.route,
        arguments = HistoryDestination.arguments
    ) { backStackEntry ->
        val dateKey = backStackEntry.arguments?.getString(HistoryDestination.argDateKey)
        HistoryRoute(
            navController = navController,
            dateKey = dateKey
        )
    }
}
