package jp.msaitoappdev.caregiver.humanmed.data.mapper

import jp.msaitoappdev.caregiver.humanmed.data.local.db.ScoreRecord
import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry

fun ScoreRecord.toDomain(): ScoreEntry =
    ScoreEntry(id = id, timestamp = timestamp, score = score, total = total, percent = percent)

fun ScoreEntry.toEntity(): ScoreRecord =
    ScoreRecord(id = id, timestamp = timestamp, score = score, total = total, percent = percent)
