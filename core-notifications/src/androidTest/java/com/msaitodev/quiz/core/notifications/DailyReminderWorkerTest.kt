package com.msaitodev.quiz.core.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.msaitodev.core.notifications.DailyReminderWorker
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class DailyReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun doWork_showsNotification_andReturnsSuccess() {
        // GIVEN
        // Use try-with-resources for the static mock, which is available via mockito-inline
        Mockito.mockStatic(ReminderNotifier::class.java).use { mockedNotifier ->
            val worker = TestListenableWorkerBuilder<DailyReminderWorker>(context).build()

            // WHEN
            val result = runBlocking { worker.doWork() }

            // THEN
            // Verify that the static method was called
            mockedNotifier.verify {
                ReminderNotifier.show(worker.applicationContext)
            }
            // Verify that the work was successful
            assertThat(result).isEqualTo(ListenableWorker.Result.success())
        }
    }
}
