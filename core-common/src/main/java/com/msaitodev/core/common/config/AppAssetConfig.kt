package com.msaitodev.core.common.config

/**
 * アプリケーションデータのロードおよび試験仕様に必要な設定。
 */
data class AppAssetConfig(
    val assetDataDirectory: String,
    val totalExamQuestions: Int = 120,
    val passingScoreThreshold: Float = 0.6f
)
