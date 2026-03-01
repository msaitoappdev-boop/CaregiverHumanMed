package com.msaitodev.quiz.core.common.config

/**
 * クイズデータのロードに必要なアセット設定。
 * 指定された [quizDataRootDirectory] 配下の全 JSON ファイルを自動的にロードする。
 */
data class QuizAssetConfig(
    val quizDataRootDirectory: String
)
