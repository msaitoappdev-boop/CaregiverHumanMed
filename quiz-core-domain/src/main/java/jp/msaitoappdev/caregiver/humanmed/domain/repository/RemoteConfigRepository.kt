package jp.msaitoappdev.caregiver.humanmed.domain.repository

interface RemoteConfigRepository {
    fun getLong(key: String): Long
    fun getBoolean(key: String): Boolean
}
