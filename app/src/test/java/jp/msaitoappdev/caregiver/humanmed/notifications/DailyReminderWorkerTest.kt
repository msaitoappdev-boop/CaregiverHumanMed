package jp.msaitoappdev.caregiver.humanmed.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mockStatic
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DailyReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `doWork should show notification and return success`() {
        // ReminderNotifier objectの静的メソッド呼び出しをモックする
        mockStatic(ReminderNotifier::class.java).use { mockedNotifier ->
            val worker = TestListenableWorkerBuilder<DailyReminderWorker>(context).build()
            runBlocking {
                // Act: テスト対象のdoWork()を実際に呼び出す
                val result = worker.doWork()

                // Assert
                // 1. ReminderNotifier.show()が正しいcontextで1回呼ばれたことを検証
                mockedNotifier.verify {
                    ReminderNotifier.show(worker.applicationContext)
                }

                // 2. Workerの実行結果がsuccessであることを検証
                assertEquals(ListenableWorker.Result.success(), result)
            }
        }
    }
}
