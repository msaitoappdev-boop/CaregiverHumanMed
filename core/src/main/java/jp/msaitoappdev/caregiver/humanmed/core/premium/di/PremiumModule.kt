package jp.msaitoappdev.caregiver.humanmed.core.premium.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.msaitoappdev.caregiver.humanmed.core.premium.PremiumRepositoryImpl
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PremiumModule {
    @Binds
    @Singleton
    abstract fun bindPremiumRepository(
        impl: PremiumRepositoryImpl
    ): PremiumRepository
}