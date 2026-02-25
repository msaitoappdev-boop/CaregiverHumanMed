package com.msaitodev.quiz.core.domain.model

data class ScoreEntry(
    val id: Long = 0L,
    val timestamp: Long,
    val score: Int,
    val total: Int,
    val percent: Int
)
