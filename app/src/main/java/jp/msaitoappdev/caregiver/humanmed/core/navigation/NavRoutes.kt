
package jp.msaitoappdev.caregiver.humanmed.core.navigation

/**
 * 画面ルートの定義を一元化。
 * - パターン（arguments付きのroute）と、遷移用に組み立てる関数を分離。
 * - まずは MainActivity から使用開始（他画面は追って段階移行）。
 */
object NavRoutes {
    const val HOME = "home"
    const val QUIZ = "quiz"
    const val PAYWALL = "paywall"
    const val SETTINGS = "settings"
    const val REVIEW = "review"
    const val HISTORY = "history"

    object Result {
        /** NavHost に登録するパターン（arguments 付き） */
        const val PATTERN = "result/{score}/{total}"

        /** 画面遷移時に使う文字列（例: result/2/3） */
        fun build(score: Int, total: Int): String = "result/$score/$total"
    }
}
