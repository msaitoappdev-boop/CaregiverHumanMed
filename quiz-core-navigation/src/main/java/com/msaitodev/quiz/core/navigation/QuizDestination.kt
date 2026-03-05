package com.msaitodev.quiz.core.navigation

/**
 * クイズ画面へのナビゲーション定義。
 * モード（通常・弱点特訓）は SettingsProvider の状態に依存するため、
 * 遷移時の引数指定は不要になりました。
 */
object QuizDestination {
    const val route = "quiz"
}
