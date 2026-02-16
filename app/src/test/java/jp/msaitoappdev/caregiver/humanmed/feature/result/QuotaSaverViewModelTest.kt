package jp.msaitoappdev.caregiver.humanmed.feature.result

import jp.msaitoappdev.caregiver.humanmed.domain.repository.StudyQuotaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class QuotaSaverViewModelTest {

    private val studyQuotaRepository: StudyQuotaRepository = mock()

    @Test
    fun `markFinished calls repository`() = runTest {
        // Arrange
        val viewModel = QuotaSaverViewModel(studyQuotaRepository)

        // Act
        viewModel.markFinished()

        // Assert
        // ViewModelのmarkFinishedはsuspendなので、verifyもsuspend対応である必要があるが、
        // mockito-kotlinではsuspend関数のverifyは追加設定なしでそのまま書ける
        verify(studyQuotaRepository).markSetFinished()
    }
}
