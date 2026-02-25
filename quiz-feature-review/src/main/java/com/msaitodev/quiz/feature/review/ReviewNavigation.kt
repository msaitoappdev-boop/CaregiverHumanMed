package com.msaitodev.quiz.feature.review

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.msaitodev.quiz.core.navigation.NavRoutes

fun NavGraphBuilder.reviewGraph(navController: NavController) {
    composable(
        route = NavRoutes.Review.routeWithArgs,
        arguments = NavRoutes.Review.arguments
    ) {
        ReviewRoute(navController)
    }
}
