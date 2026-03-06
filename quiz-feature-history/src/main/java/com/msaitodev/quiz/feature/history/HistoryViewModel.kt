package com.msaitodev.quiz.feature.history

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.usecase.ObserveScoresUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeScores: ObserveScoresUseCase,
    private val premiumRepo: PremiumRepository
) : ViewModel() {

    private val _dateFilter = MutableStateFlow<String?>(null)

    /**
     * 指定された日付（オプション）でフィルタリングされた履歴を監視します。
     * 無料ユーザーの場合は、引数に関わらず全期間の履歴を返します。
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val history: Flow<List<ScoreEntry>> = combine(
        premiumRepo.isPremium,
        _dateFilter
    ) { isPremium, dateKey ->
        isPremium to dateKey
    }.flatMapLatest { (isPremium, dateKey) ->
        if (isPremium) {
            observeScores(dateKey)
        } else {
            observeScores(null)
        }
    }

    fun setDateFilter(dateKey: String?) {
        _dateFilter.value = dateKey
    }
}
