package com.msaitodev.quiz.feature.analysis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AnalysisRoute(
    onBack: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalysisScreen(
        uiState = uiState,
        onBack = onBack,
        onPeriodChange = { period ->
            viewModel.onPeriodSelected(period)
        }
    )
}
