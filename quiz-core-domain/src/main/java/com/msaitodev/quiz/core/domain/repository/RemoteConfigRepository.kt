package com.msaitodev.quiz.core.domain.repository

interface RemoteConfigRepository {
    fun getLong(key: String): Long
    fun getBoolean(key: String): Boolean
}
