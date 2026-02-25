package com.msaitodev.quiz.core.data.mapper

import com.msaitodev.quiz.core.data.local.db.ScoreRecord
import com.msaitodev.quiz.core.domain.model.ScoreEntry

fun ScoreRecord.toDomain(): ScoreEntry =
    ScoreEntry(id = id, timestamp = timestamp, score = score, total = total, percent = percent)

fun ScoreEntry.toEntity(): ScoreRecord =
    ScoreRecord(id = id, timestamp = timestamp, score = score, total = total, percent = percent)
