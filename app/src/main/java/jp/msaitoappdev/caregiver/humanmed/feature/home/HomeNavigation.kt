package jp.msaitoappdev.caregiver.humanmed.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.homeGraph(
    onStartQuiz: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenSettings: () -> Unit
) {
    composable(NavRoutes.HOME) {
        HomeRoute(
            onStartQuiz = onStartQuiz,
            onUpgrade = onUpgrade,
            onOpenSettings = onOpenSettings
        )
    }
}
