package jp.msaitoappdev.caregiver.humanmed.feature.result

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.resultGraph(navController: NavController) {
    composable(
        route = NavRoutes.Result.PATTERN,
        arguments = listOf(
            navArgument("score") { type = NavType.IntType },
            navArgument("total") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val score = backStackEntry.arguments?.getInt("score") ?: 0
        val total = backStackEntry.arguments?.getInt("total") ?: 0
        ResultRoute(navController, score, total)
    }
}
