package com.msaitodev.quiz.feature.result

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.SaveScoreUseCase
import javax.inject.Inject

@HiltViewModel
class ScoreSaverViewModel @Inject constructor(
    val save: SaveScoreUseCase
) : ViewModel()
