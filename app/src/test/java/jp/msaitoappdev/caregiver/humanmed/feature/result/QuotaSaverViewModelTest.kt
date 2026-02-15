package jp.msaitoappdev.caregiver.humanmed.feature.result

import io.mockk.coVerify
import io.mockk.mockk
import jp.msaitoappdev.caregiver.humanmed.data.repository.StudyQuotaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class QuotaSaverViewModelTest {

    private val studyQuotaRepository: StudyQuotaRepository = mockk(relaxed = true)

    @Test
    fun `markFinished calls repository`() = runTest {
        // Arrange
        val viewModel = QuotaSaverViewModel(studyQuotaRepository)

        // Act
        viewModel.markFinished()

        // Assert
        coVerify { studyQuotaRepository.markSetFinished() }
    }
}
