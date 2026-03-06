package com.msaitodev.core.cloudsync

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * リモート設定の低レベル操作を担うクライアント。
 */
interface CloudConfigClient {
    fun getLong(key: String): Long
    fun getBoolean(key: String): Boolean
}

@Singleton
class CloudConfigClientImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : CloudConfigClient {
    override fun getLong(key: String): Long = remoteConfig.getLong(key)
    override fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)
}
