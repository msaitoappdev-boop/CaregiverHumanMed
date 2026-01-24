package jp.msaitoappdev.caregiver.humanmed.data.score

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScoreDbModule {

    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.get(context)

    @Provides @Singleton
    fun provideScoreDao(db: AppDatabase): ScoreDao = db.scoreDao()
}
