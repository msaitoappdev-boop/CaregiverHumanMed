package com.msaitodev.core.common.config

/**
 * アプリケーションデータのロードに必要なアセット設定。
 * 指定された [assetDataDirectory] 配下のファイルをロードするために使用します。
 */
data class AppAssetConfig(
    val assetDataDirectory: String
)
