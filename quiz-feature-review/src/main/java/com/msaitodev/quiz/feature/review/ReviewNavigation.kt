package com.msaitodev.quiz.feature.review

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.msaitodev.quiz.core.navigation.ReviewDestination

fun NavGraphBuilder.reviewGraph(navController: NavController) {
    composable(
        route = ReviewDestination.routeWithArgs,
        arguments = ReviewDestination.arguments
    ) {
        ReviewRoute(navController)
    }
}
