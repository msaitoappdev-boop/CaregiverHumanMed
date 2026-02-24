package jp.msaitoappdev.caregiver.humanmed.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import javax.inject.Singleton

private val Context._dataStore by preferencesDataStore(name = "premium_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

// BillingClient は BillingManager 側で一元管理するため提供しません
// （二重生成を避ける）
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context._dataStore

    @Provides
    @Singleton
    @Named("AppScope")
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
