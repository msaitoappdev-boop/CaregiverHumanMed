package jp.msaitoappdev.caregiver.humanmed.feature.result

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.resultGraph(
    navController: NavController,
    onNextSet: () -> Unit,
    onReview: (questionsJson: String, answersJson: String) -> Unit,
    onReviewSameOrder: () -> Unit,
    onShowScoreHistory: () -> Unit,
    onBackToHome: () -> Unit
) {
    composable(
        route = NavRoutes.Result.routeWithArgs,
        arguments = NavRoutes.Result.arguments
    ) { backStackEntry ->
        val score = backStackEntry.arguments?.getInt(NavRoutes.Result.ARG_SCORE) ?: 0
        val total = backStackEntry.arguments?.getInt(NavRoutes.Result.ARG_TOTAL) ?: 0
        ResultRoute(
            score = score,
            total = total,
            onNextSet = onNextSet,
            onReview = onReview,
            onReviewSameOrder = onReviewSameOrder,
            onShowScoreHistory = onShowScoreHistory,
            onBackToHome = onBackToHome
        )
    }
}
