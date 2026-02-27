package com.msaitodev.quiz.core.common.config

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class AdUnits @Inject constructor(
    val interstitialWeaktrainComplete: String,
    val rewardedWeaktrainPlusOne: String
)
