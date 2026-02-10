package jp.msaitoappdev.caregiver.humanmed.notifications

import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.*

class DailyReminderWorkerTest {

    private val mockContext = mock(android.content.Context::class.java)
    private val mockWorkerParams = mock(WorkerParameters::class.java)
    private val worker = DailyReminderWorker(mockContext, mockWorkerParams)

    @Test
    fun `test do work`() {
        // Arrange
        `when`(worker.doWork()).thenReturn(ListenableWorker.Result.success())

        // Act
        val result = worker.doWork()

        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
    }
}
