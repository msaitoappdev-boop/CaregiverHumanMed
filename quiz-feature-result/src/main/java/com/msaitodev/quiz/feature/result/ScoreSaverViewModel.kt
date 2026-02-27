package com.msaitodev.quiz.feature.result

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.msaitodev.quiz.core.domain.usecase.SaveScoreUseCase
import javax.inject.Inject

@HiltViewModel
class ScoreSaverViewModel @Inject constructor(
    val save: SaveScoreUseCase
) : ViewModel()
