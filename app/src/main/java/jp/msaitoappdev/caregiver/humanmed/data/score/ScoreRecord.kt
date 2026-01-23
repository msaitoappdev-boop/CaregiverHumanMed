package jp.msaitoappdev.caregiver.humanmed.data.score

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_records")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,  // epoch millis
    val score: Int,
    val total: Int,
    val percent: Int
    // 必要なら後から項目追加: durationMillis, shuffled, seed など
)
