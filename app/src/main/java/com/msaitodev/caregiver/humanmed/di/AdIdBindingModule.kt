package com.msaitodev.caregiver.humanmed.di

import android.content.Context
import com.msaitodev.quiz.core.ads.AdModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.msaitodev.caregiver.humanmed.R
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AdIdBindingModule {

    @Provides
    @Named(AdModule.NAME_INTERSTITIAL_AD_ID)
    fun provideInterstitialAdId(@ApplicationContext context: Context): String {
        return context.getString(R.string.ad_unit_interstitial_weaktrain_complete)
    }

    @Provides
    @Named(AdModule.NAME_REWARDED_AD_ID)
    fun provideRewardedAdId(@ApplicationContext context: Context): String {
        return context.getString(R.string.ad_unit_rewarded_weaktrain_plus1)
    }
}
