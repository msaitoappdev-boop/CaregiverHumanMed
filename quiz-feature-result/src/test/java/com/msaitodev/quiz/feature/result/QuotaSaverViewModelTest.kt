package com.msaitodev.quiz.feature.result

import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class QuotaSaverViewModelTest {

    private val studyQuotaRepository: StudyQuotaRepository = mock()
    private lateinit var viewModel: QuotaSaverViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = QuotaSaverViewModel(studyQuotaRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `markFinished calls repository`() = runTest {
        // Act
        viewModel.markFinished()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify(studyQuotaRepository).markSetFinished()
    }
}
