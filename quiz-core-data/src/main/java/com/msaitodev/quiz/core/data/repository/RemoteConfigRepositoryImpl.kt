package com.msaitodev.quiz.core.data.repository

import com.msaitodev.core.cloudsync.CloudConfigClient
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigRepositoryImpl @Inject constructor(
    private val cloudConfigClient: CloudConfigClient
) : RemoteConfigRepository {

    override fun getLong(key: String): Long {
        return cloudConfigClient.getLong(key)
    }

    override fun getBoolean(key: String): Boolean {
        return cloudConfigClient.getBoolean(key)
    }
}
