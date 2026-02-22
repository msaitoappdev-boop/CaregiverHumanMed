package com.msaitodev.quiz.feature.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun ReviewRoute(navController: NavController) {
    val vm: ReviewViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    ReviewScreen(
        uiState = uiState,
        onNavUp = { navController.popBackStack() }
    )
}
