package com.msaitodev.quiz.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ClearScoresUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ObserveScoresUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeScores: ObserveScoresUseCase,
    private val clearScores: ClearScoresUseCase
) : ViewModel() {
    fun observe(): Flow<List<ScoreEntry>> = observeScores()
    fun clearHistory() {
        viewModelScope.launch {
            clearScores()
        }
    }
}
