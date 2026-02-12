package jp.msaitoappdev.caregiver.humanmed.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.msaitoappdev.caregiver.humanmed.data.config.RemoteConfigRepositoryImpl
import jp.msaitoappdev.caregiver.humanmed.domain.repository.RemoteConfigRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindRemoteConfigRepository(impl: RemoteConfigRepositoryImpl): RemoteConfigRepository
}
