package com.msaitodev.quiz.feature.result

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.domain.repository.StudyQuotaRepository
import javax.inject.Inject

@HiltViewModel
class QuotaSaverViewModel @Inject constructor(
    private val repo: StudyQuotaRepository
): ViewModel() {
    suspend fun markFinished() { repo.markSetFinished() }
}
