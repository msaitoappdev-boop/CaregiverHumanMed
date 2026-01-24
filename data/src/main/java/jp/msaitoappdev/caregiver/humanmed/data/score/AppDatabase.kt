
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
    version = 3,                      // 2→3に上げる
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scoreDao(): ScoreDao // ★ スコアDAOのみ

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
            }
        }

        // ★ 2→3 は no-op（不要テーブルを使っていないため）
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 必要ならDROPする（今回はno-opでOK）
                // db.execSQL("DROP TABLE IF EXISTS questions")
                // db.execSQL("DROP TABLE IF EXISTS user_records")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "humanmed.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // 新規インストールには影響なし
                    .build().also { INSTANCE = it }
            }
    }
}
