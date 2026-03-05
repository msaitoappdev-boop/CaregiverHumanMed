package com.msaitodev.quiz.feature.analysis

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.msaitodev.quiz.core.navigation.AnalysisDestination

/**
 * 学習分析画面のナビゲーション・グラフを構築する拡張関数。
 */
fun NavGraphBuilder.analysisGraph(
    navController: NavController
) {
    composable(route = AnalysisDestination.route) {
        AnalysisRoute(
            onBack = { navController.popBackStack() }
        )
    }
}
