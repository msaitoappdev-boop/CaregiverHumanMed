package com.msaitodev.caregiver.humanmed.di

import com.msaitodev.caregiver.humanmed.notifications.QuizSettingsProvider
import com.msaitodev.feature.settings.SettingsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsBindingModule {

    @Binds
    @Singleton
    abstract fun bindSettingsProvider(
        impl: QuizSettingsProvider
    ): SettingsProvider
}
