package com.msaitodev.caregiver.humanmed.di

import com.msaitodev.core.common.config.AppAssetConfig
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
    fun provideAppAssetConfig(): AppAssetConfig {
        return AppAssetConfig(
            assetDataDirectory = "quiz_data"
        )
    }
}
