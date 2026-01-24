package jp.msaitoappdev.caregiver.humanmed.data.score

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ScoreRecord)

    @Query("SELECT * FROM score_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ScoreRecord>>

    @Query("DELETE FROM score_records")
    suspend fun clear()
}