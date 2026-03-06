package com.msaitodev.quiz.feature.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaitodev.feature.settings.SettingsProvider
import com.msaitodev.quiz.core.domain.model.LearningAnalysis
import com.msaitodev.quiz.core.domain.model.TrendPeriod
import com.msaitodev.quiz.core.domain.usecase.GetLearningAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val analysis: LearningAnalysis? = null,
    val currentPeriod: TrendPeriod = TrendPeriod.DAILY
)

sealed interface AnalysisEvent {
    data object NavigateToSettings : AnalysisEvent
    data class NavigateToHistory(val dateKey: String) : AnalysisEvent
}

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val getLearningAnalysis: GetLearningAnalysisUseCase,
    private val settingsProvider: SettingsProvider
) : ViewModel() {

    private val _currentPeriod = MutableStateFlow(TrendPeriod.DAILY)

    private val _event = MutableSharedFlow<AnalysisEvent>()
    val event: SharedFlow<AnalysisEvent> = _event.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalysisUiState> = _currentPeriod.flatMapLatest { period ->
        getLearningAnalysis(period).map { analysis ->
            AnalysisUiState(
                isLoading = false,
                analysis = analysis,
                currentPeriod = period
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState(isLoading = true)
    )

    fun onPeriodSelected(period: TrendPeriod) {
        _currentPeriod.value = period
    }

    fun onCategoryClicked(categoryId: String) {
        viewModelScope.launch {
            settingsProvider.updateWeaknessMode(enabled = true, categoryId = categoryId)
            _event.emit(AnalysisEvent.NavigateToSettings)
        }
    }

    fun onDateClicked(dateKey: String) {
        viewModelScope.launch {
            _event.emit(AnalysisEvent.NavigateToHistory(dateKey))
        }
    }
}
