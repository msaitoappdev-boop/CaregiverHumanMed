package com.msaitodev.quiz.core.data.di

import com.msaitodev.quiz.core.data.repository.PremiumRepositoryImpl
import com.msaitodev.quiz.core.data.repository.QuestionRepositoryImpl
import com.msaitodev.quiz.core.data.repository.RemoteConfigRepositoryImpl
import com.msaitodev.quiz.core.data.repository.RewardQuotaRepositoryImpl
import com.msaitodev.quiz.core.data.repository.ScoreRepositoryImpl
import com.msaitodev.quiz.core.data.repository.StudyQuotaRepositoryImpl
import com.msaitodev.quiz.core.data.repository.SyncRepositoryImpl
import com.msaitodev.quiz.core.data.repository.WrongAnswerRepositoryImpl
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.QuestionRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.domain.repository.RewardQuotaRepository
import com.msaitodev.quiz.core.domain.repository.ScoreRepository
import com.msaitodev.quiz.core.domain.repository.StudyQuotaRepository
import com.msaitodev.quiz.core.domain.repository.SyncRepository
import com.msaitodev.quiz.core.domain.repository.WrongAnswerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindQuestionRepository(impl: QuestionRepositoryImpl): QuestionRepository

    @Binds
    fun bindScoreRepository(impl: ScoreRepositoryImpl): ScoreRepository

    @Binds
    fun bindPremiumRepository(impl: PremiumRepositoryImpl): PremiumRepository

    @Binds
    fun bindRemoteConfigRepository(impl: RemoteConfigRepositoryImpl): RemoteConfigRepository

    @Binds
    fun bindStudyQuotaRepository(impl: StudyQuotaRepositoryImpl): StudyQuotaRepository

    @Binds
    fun bindRewardQuotaRepository(impl: RewardQuotaRepositoryImpl): RewardQuotaRepository

    @Binds
    fun bindWrongAnswerRepository(impl: WrongAnswerRepositoryImpl): WrongAnswerRepository

    @Binds
    @Singleton
    fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
