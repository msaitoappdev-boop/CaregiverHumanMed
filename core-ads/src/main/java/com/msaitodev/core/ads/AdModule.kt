package com.msaitodev.core.ads

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.msaitodev.core.common.config.AdUnits
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.adPrefsStore: DataStore<Preferences> by preferencesDataStore(name = "ad_settings")

@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {

    @Binds
    @Singleton
    abstract fun bindInterstitialAdRepository(
        impl: InterstitialAdRepositoryImpl
    ): InterstitialAdRepository

    @Binds
    @Singleton
    abstract fun bindRewardedAdRepository(
        impl: RewardedAdRepositoryImpl
    ): RewardedAdRepository

    companion object {
        const val NAME_INTERSTITIAL_AD_ID = "interstitial_ad_id"
        const val NAME_REWARDED_AD_ID = "rewarded_ad_id"
        const val QUALIFIER_AD_DATASTORE = "ad_data_store"

        @Provides
        @Singleton
        fun provideAdUnits(
            @Named(NAME_INTERSTITIAL_AD_ID) interstitialAdId: String,
            @Named(NAME_REWARDED_AD_ID) rewardedAdId: String
        ): AdUnits {
            return AdUnits(
                interstitialUnitA = interstitialAdId,
                rewardedUnitA = rewardedAdId
            )
        }

        @Provides
        @Singleton
        @Named(QUALIFIER_AD_DATASTORE)
        fun provideAdDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.adPrefsStore
        }
    }
}
