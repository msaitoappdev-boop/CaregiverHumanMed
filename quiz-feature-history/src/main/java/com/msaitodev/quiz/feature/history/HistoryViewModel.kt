package com.msaitodev.quiz.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.usecase.ClearScoresUseCase
import com.msaitodev.quiz.core.domain.usecase.ObserveScoresUseCase
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
