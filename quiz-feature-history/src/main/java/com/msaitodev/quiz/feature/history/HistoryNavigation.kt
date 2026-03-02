package com.msaitodev.quiz.feature.history

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.msaitodev.core.navigation.HistoryDestination

fun NavGraphBuilder.historyGraph(navController: NavController) {
    composable(HistoryDestination.route) {
        HistoryRoute(navController)
    }
}
