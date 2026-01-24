package jp.msaitoappdev.caregiver.humanmed.ui.history

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ObserveScoresUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.ClearScoresUseCase
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class HistoryVM @Inject constructor(
    private val observeScores: ObserveScoresUseCase,
    private val clearScores: ClearScoresUseCase
) : ViewModel() {
    fun observe(): Flow<List<ScoreEntry>> = observeScores()
    suspend fun clear() = clearScores()
}
