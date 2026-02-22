package jp.msaitoappdev.caregiver.humanmed.feature.review

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.reviewGraph(navController: NavController) {
    composable(
        route = NavRoutes.Review.routeWithArgs,
        arguments = NavRoutes.Review.arguments
    ) {
        ReviewRoute(navController)
    }
}
