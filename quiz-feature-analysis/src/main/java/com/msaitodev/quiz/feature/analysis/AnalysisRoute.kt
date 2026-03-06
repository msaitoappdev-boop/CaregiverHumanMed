package com.msaitodev.quiz.feature.analysis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AnalysisRoute(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: (String) -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.event) {
        viewModel.event.collect { event ->
            when (event) {
                is AnalysisEvent.NavigateToSettings -> onNavigateToSettings()
                is AnalysisEvent.NavigateToHistory -> onNavigateToHistory(event.dateKey)
            }
        }
    }

    AnalysisScreen(
        uiState = uiState,
        onBack = onBack,
        onPeriodChange = { period ->
            viewModel.onPeriodSelected(period)
        },
        onCategoryClick = { categoryId ->
            viewModel.onCategoryClicked(categoryId)
        },
        onDateClick = { dateKey ->
            viewModel.onDateClicked(dateKey)
        }
    )
}
