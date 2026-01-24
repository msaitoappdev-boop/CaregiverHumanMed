
package jp.msaitoappdev.caregiver.humanmed.data.score

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ScoreRecord::class,           // ★ スコアのみ
    ],
    version = 2,                      // 既存の v2 を維持
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scoreDao(): ScoreDao // ★ スコアDAOのみ

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // v1→v2 の移行（過去ビルドからの互換用、無害なので残してOK）
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ここは文字列SQLなので、型解決に関係しません。
                // 旧テーブルが必要なら作成、使わないならそのままでも問題ありません。
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS score_records(
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        score INTEGER NOT NULL,
                        total INTEGER NOT NULL,
                        percent INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                // 旧テーブル作成（不要なら削ってもOK）
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
                    .addMigrations(MIGRATION_1_2) // 新規インストールには影響なし
                    .build().also { INSTANCE = it }
            }
    }
}
