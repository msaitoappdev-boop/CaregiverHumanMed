package jp.msaitoappdev.caregiver.humanmed.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    // dataモジュールのDataModuleに責務を移譲したため、このファイルは空になります。
}
