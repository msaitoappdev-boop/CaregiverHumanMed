package com.msaitodev.quiz.core.common.config

/**
 * クイズデータのロードに必要なアセット設定。
 * アプリ（Hub）ごとに異なるファイル名やパスを指定できるように DI で注入する。
 */
data class QuizAssetConfig(
    val questionsJsonPath: String
)
