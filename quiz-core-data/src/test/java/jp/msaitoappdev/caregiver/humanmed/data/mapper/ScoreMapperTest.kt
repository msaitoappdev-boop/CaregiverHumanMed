package jp.msaitoappdev.caregiver.humanmed.data.mapper

import com.google.common.truth.Truth.assertThat
import com.msaitodev.quiz.core.data.mapper.toDomain
import com.msaitodev.quiz.core.data.mapper.toEntity
import com.msaitodev.quiz.core.data.local.db.ScoreRecord
import com.msaitodev.quiz.core.domain.model.ScoreEntry
import org.junit.Test

class ScoreMapperTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        // GIVEN: 変換元のScoreRecordを作成
        val record = ScoreRecord(
            id = 1L,
            timestamp = 12345L,
            score = 8,
            total = 10,
            percent = 80
        )

        // WHEN: toDomain()拡張関数を呼び出す
        val entry = record.toDomain()

        // THEN: 全てのフィールドが正しくマッピングされていることを確認
        assertThat(entry.id).isEqualTo(1L)
        assertThat(entry.timestamp).isEqualTo(12345L)
        assertThat(entry.score).isEqualTo(8)
        assertThat(entry.total).isEqualTo(10)
        assertThat(entry.percent).isEqualTo(80)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        // GIVEN: 変換元のScoreEntryを作成
        val entry = ScoreEntry(
            id = 1L,
            timestamp = 12345L,
            score = 8,
            total = 10,
            percent = 80
        )

        // WHEN: toEntity()拡張関数を呼び出す
        val record = entry.toEntity()

        // THEN: 全てのフィールドが正しくマッピングされていることを確認
        assertThat(record.id).isEqualTo(1L)
        assertThat(record.timestamp).isEqualTo(12345L)
        assertThat(record.score).isEqualTo(8)
        assertThat(record.total).isEqualTo(10)
        assertThat(record.percent).isEqualTo(80)
    }
}
