package com.msaitodev.core.common.config

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 広告ユニットIDを保持する共通クラス。
 * 量産化を容易にするため、特定の機能名に依存しない抽象的な名前を使用します。
 */
@Singleton
data class AdUnits @Inject constructor(
    val interstitialUnitA: String,
    val rewardedUnitA: String
)