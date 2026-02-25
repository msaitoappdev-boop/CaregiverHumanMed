package com.msaitodev.quiz.core.ads

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.msaitodev.quiz.core.common.config.AdUnits
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    const val NAME_INTERSTITIAL_AD_ID = "interstitial_ad_id"
    const val NAME_REWARDED_AD_ID = "rewarded_ad_id"

    @Provides
    @Singleton
    fun provideAdUnits(
        @Named(NAME_INTERSTITIAL_AD_ID) interstitialAdId: String,
        @Named(NAME_REWARDED_AD_ID) rewardedAdId: String
    ): AdUnits {
        return AdUnits(
            interstitialWeaktrainComplete = interstitialAdId,
            rewardedWeaktrainPlusOne = rewardedAdId
        )
    }
}
