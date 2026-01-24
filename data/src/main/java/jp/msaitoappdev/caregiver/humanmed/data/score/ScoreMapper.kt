package jp.msaitoappdev.caregiver.humanmed.data.score

import jp.msaitoappdev.caregiver.humanmed.domain.model.ScoreEntry

fun ScoreRecord.toDomain(): ScoreEntry =
    ScoreEntry(id = id, timestamp = timestamp, score = score, total = total, percent = percent)

fun ScoreEntry.toEntity(): ScoreRecord =
    ScoreRecord(id = id, timestamp = timestamp, score = score, total = total, percent = percent)
