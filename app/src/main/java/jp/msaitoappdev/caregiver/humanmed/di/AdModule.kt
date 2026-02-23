package jp.msaitoappdev.caregiver.humanmed.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.msaitoappdev.caregiver.humanmed.R
import jp.msaitoappdev.caregiver.humanmed.core.config.AdUnits
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    private const val REWARDED_AD_ID = "rewarded_ad_id"

    @Provides
    @Singleton
    fun provideAdUnits(
        @ApplicationContext context: Context,
        @Named(REWARDED_AD_ID) rewardedAdUnitId: String
    ): AdUnits {
        return AdUnits(
            interstitialWeaktrainComplete = context.getString(R.string.ad_unit_interstitial_weaktrain_complete),
            rewardedWeaktrainPlusOne = rewardedAdUnitId
        )
    }

    @Provides
    @Named(REWARDED_AD_ID)
    fun provideRewardedAdUnitId(@ApplicationContext context: Context): String {
        return context.getString(R.string.ad_unit_rewarded_weaktrain_plus1)
    }
}
