
package jp.msaitoappdev.caregiver.humanmed.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val category: String,
    val type: String,
    val text: String,
    val choicesJson: String?,
    val correctAnswer: String,
    val explanationSimple: String,
    val explanationFull: String,
    val tags: String?,
    val difficulty: Int
)

@Entity(tableName = "user_records")
data class UserRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionId: Int,
    val isCorrect: Boolean,
    val answeredAt: Long,
    val elapsedSec: Int
)
