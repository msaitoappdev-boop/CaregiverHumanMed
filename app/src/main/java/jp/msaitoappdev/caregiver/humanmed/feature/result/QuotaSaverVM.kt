package jp.msaitoappdev.caregiver.humanmed.feature.result

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.msaitoappdev.caregiver.humanmed.core.session.StudyQuotaRepository

@HiltViewModel
class QuotaSaverVM @Inject constructor(
    private val repo: StudyQuotaRepository
): ViewModel() {
    suspend fun markFinished() { repo.markSetFinished() }
}
