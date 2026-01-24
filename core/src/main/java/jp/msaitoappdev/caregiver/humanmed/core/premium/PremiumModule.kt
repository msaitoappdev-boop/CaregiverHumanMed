package jp.msaitoappdev.caregiver.humanmed.core.premium

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class PremiumModule {
    @Binds
    @Singleton
    abstract fun bindPremiumRepository(
        impl: PremiumRepositoryImpl
    ): PremiumRepository
}
