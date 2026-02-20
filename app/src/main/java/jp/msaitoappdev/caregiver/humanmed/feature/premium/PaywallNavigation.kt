package jp.msaitoappdev.caregiver.humanmed.feature.premium

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

fun NavGraphBuilder.paywallGraph() {
    composable(NavRoutes.PAYWALL) {
        PaywallRoute()
    }
}
