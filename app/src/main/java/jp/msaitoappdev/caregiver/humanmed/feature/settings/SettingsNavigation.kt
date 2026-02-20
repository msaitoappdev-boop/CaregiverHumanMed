package jp.msaitoappdev.caregiver.humanmed.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.settingsGraph(onBack: () -> Unit) {
    composable(NavRoutes.SETTINGS) {
        SettingsRoute(onBack = onBack)
    }
}
