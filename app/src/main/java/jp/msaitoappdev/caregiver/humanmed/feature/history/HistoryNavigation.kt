package jp.msaitoappdev.caregiver.humanmed.feature.history

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.historyGraph(navController: NavController) {
    composable(NavRoutes.HISTORY) {
        HistoryRoute(navController)
    }
}
