package com.msaitodev.quiz.feature.main.quiz

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.msaitodev.quiz.core.navigation.NavRoutes

fun NavGraphBuilder.quizGraph(
    navController: NavController,
    onQuizFinished: (result: QuizResult) -> Unit,
    onUpgrade: () -> Unit
) {
    composable(
        route = NavRoutes.QUIZ,
        deepLinks = listOf(navDeepLink { uriPattern = "caregiver://reminder" })
    ) {
        QuizRoute(
            navController = navController,
            onQuizFinished = onQuizFinished,
            onUpgrade = onUpgrade
        )
    }
}
