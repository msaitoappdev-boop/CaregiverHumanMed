package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.quizGraph(navController: NavController) {
    composable(
        route = NavRoutes.QUIZ,
        deepLinks = listOf(navDeepLink { uriPattern = "caregiver://reminder" })
    ) { 
        QuizRoute(navController)
    }
}
