package com.msaitodev.quiz.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.msaitodev.quiz.core.data.local.db.AppDatabase
import com.msaitodev.quiz.core.data.local.db.ScoreDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScoreDbModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.Companion.get(context)

    @Provides
    @Singleton
    fun provideScoreDao(db: AppDatabase): ScoreDao = db.scoreDao()
}