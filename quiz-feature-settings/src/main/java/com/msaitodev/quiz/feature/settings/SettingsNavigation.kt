package com.msaitodev.quiz.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.settingsGraph(onBack: () -> Unit) {
    composable(NavRoutes.SETTINGS) {
        SettingsRoute(onBack = onBack)
    }
}
