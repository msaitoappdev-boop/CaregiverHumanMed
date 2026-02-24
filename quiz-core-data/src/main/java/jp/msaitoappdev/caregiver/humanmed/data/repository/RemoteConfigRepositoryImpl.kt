package jp.msaitoappdev.caregiver.humanmed.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import jp.msaitoappdev.caregiver.humanmed.domain.repository.RemoteConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigRepositoryImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigRepository {

    override fun getLong(key: String): Long {
        return remoteConfig.getLong(key)
    }

    override fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }
}