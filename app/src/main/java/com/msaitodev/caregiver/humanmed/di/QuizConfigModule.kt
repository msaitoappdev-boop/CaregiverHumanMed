package com.msaitodev.caregiver.humanmed.di

import com.msaitodev.quiz.core.common.config.QuizAssetConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuizConfigModule {

    @Provides
    @Singleton
    fun provideQuizAssetConfig(): QuizAssetConfig {
        return QuizAssetConfig(
            questionsJsonPath = "questions.json"
        )
    }
}
