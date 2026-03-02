package com.msaitodev.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.msaitodev.core.navigation.SettingsDestination

fun NavGraphBuilder.settingsGraph(onBack: () -> Unit) {
    composable(SettingsDestination.route) {
        SettingsRoute(onBack = onBack)
    }
}