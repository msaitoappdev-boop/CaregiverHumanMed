package jp.msaitoappdev.caregiver.humanmed.feature.result

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.msaitoappdev.caregiver.humanmed.domain.usecase.SaveScoreUseCase

@HiltViewModel
class ScoreSaverVM @Inject constructor(
    val save: SaveScoreUseCase
) : ViewModel()
