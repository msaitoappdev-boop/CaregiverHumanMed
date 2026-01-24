package jp.msaitoappdev.caregiver.humanmed.data.score

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 質問系エンティティ & DAO
import jp.msaitoappdev.caregiver.humanmed.data.db.QuestionEntity
import jp.msaitoappdev.caregiver.humanmed.data.db.UserRecordEntity
import jp.msaitoappdev.caregiver.humanmed.data.db.QuestionDao
import jp.msaitoappdev.caregiver.humanmed.data.db.UserRecordDao


@Database(
    entities = [
        ScoreRecord::class,
        QuestionEntity::class,
        UserRecordEntity::class
    ],
    version = 2,
    exportSchema = true
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun questionDao(): QuestionDao
    abstract fun userRecordDao(): UserRecordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // questions: 初回導入時のみ作成
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS questions(
              id INTEGER NOT NULL PRIMARY KEY,
              category TEXT NOT NULL,
              type TEXT NOT NULL,
              text TEXT NOT NULL,
              choicesJson TEXT,
              correctAnswer TEXT NOT NULL,
              explanationSimple TEXT NOT NULL,
              explanationFull TEXT NOT NULL,
              tags TEXT,
              difficulty INTEGER NOT NULL
            )
            """.trimIndent()
                )

                // user_records: 初回導入時のみ作成
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS user_records(
              id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              questionId INTEGER NOT NULL,
              isCorrect INTEGER NOT NULL,
              answeredAt INTEGER NOT NULL,
              elapsedSec INTEGER NOT NULL
            )
            """.trimIndent()
                )
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "humanmed.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
    }
}
