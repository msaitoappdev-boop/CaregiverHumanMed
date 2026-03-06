package com.msaitodev.quiz.core.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 以前は FirebaseRemoteConfig を提供していましたが、
 * 責務を :core-cloud-sync へ移譲したため現在は空です。
 */
@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    // 空のモジュール。将来的な設定拡張のために残置。
}
