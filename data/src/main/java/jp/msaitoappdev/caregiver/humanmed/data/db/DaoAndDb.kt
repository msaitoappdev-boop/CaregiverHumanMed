
package jp.msaitoappdev.caregiver.humanmed.data.db

import androidx.room.*

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandom(limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    suspend fun getByCategory(category: String, limit: Int): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<QuestionEntity>)
}

@Dao
interface UserRecordDao {
    @Insert
    suspend fun insert(r: UserRecordEntity)
}

@Database(entities = [QuestionEntity::class, UserRecordEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun userRecordDao(): UserRecordDao
}
