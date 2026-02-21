package jp.msaitoappdev.caregiver.humanmed.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.msaitoappdev.caregiver.humanmed.data.repository.*
import jp.msaitoappdev.caregiver.humanmed.domain.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindQuestionRepository(impl: QuestionRepositoryImpl): QuestionRepository

    @Binds
    @Singleton
    fun bindScoreRepository(impl: ScoreRepositoryImpl): ScoreRepository

    @Binds
    fun bindPremiumRepository(impl: PremiumRepositoryImpl): PremiumRepository

    @Binds
    fun bindRemoteConfigRepository(impl: RemoteConfigRepositoryImpl): RemoteConfigRepository

    @Binds
    fun bindStudyQuotaRepository(impl: StudyQuotaRepositoryImpl): StudyQuotaRepository

    @Binds
    fun bindRewardQuotaRepository(impl: RewardQuotaRepositoryImpl): RewardQuotaRepository
}
