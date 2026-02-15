package jp.msaitoappdev.caregiver.humanmed.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.msaitoappdev.caregiver.humanmed.R
import jp.msaitoappdev.caregiver.humanmed.core.config.AdUnits
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    @Provides
    @Singleton
    fun provideAdUnits(@ApplicationContext context: Context): AdUnits {
        return AdUnits(
            interstitialWeaktrainComplete = context.getString(R.string.ad_unit_interstitial_weaktrain_complete),
            rewardedWeaktrainPlusOne = context.getString(R.string.ad_unit_rewarded_weaktrain_plus1)
        )
    }
}
