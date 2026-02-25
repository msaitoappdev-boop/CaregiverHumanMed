package jp.msaitoappdev.caregiver.humanmed.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.data.repository.ScoreRepositoryImpl
import com.msaitodev.quiz.core.data.local.db.AppDatabase
import com.msaitodev.quiz.core.data.local.db.ScoreDao
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScoreRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var scoreDao: ScoreDao
    private lateinit var repository: ScoreRepositoryImpl

    // 各テストの前に、インメモリデータベースを構築する
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // テストではメインスレッドでのクエリを許可する
            .build()
        scoreDao = database.scoreDao()
        repository = ScoreRepositoryImpl(scoreDao)
    }

    // 各テストの後に、データベースを閉じる
    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun add_and_history_returnsSavedEntry() = runTest {
        // GIVEN: 保存するスコアデータを作成
        val newEntry = ScoreEntry(score = 8, total = 10, percent = 80, timestamp = System.currentTimeMillis())

        // WHEN: データを追加する
        repository.add(newEntry)

        // THEN: history()を購読し、追加したデータが正しく流れてくることを確認
        repository.history().test {
            val historyList = awaitItem()
            assertThat(historyList).hasSize(1)

            val savedEntry = historyList[0]
            assertThat(savedEntry.score).isEqualTo(8)
            assertThat(savedEntry.total).isEqualTo(10)
            assertThat(savedEntry.percent).isEqualTo(80)
        }
    }
}
