package jp.msaitoappdev.caregiver.humanmed.core.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * 画面ルートの定義を一元化。
 */
object NavRoutes {
    const val HOME = "home"
    const val QUIZ = "quiz"
    const val PAYWALL = "paywall"
    const val SETTINGS = "settings"
    const val HISTORY = "history"

    object Result {
        private const val ROUTE = "result"
        const val ARG_SCORE = "score"
        const val ARG_TOTAL = "total"
        const val ARG_QUESTIONS_JSON = "questionsJson"
        const val ARG_ANSWERS_JSON = "answersJson"

        val routeWithArgs = "$ROUTE/{$ARG_SCORE}/{$ARG_TOTAL}?$ARG_QUESTIONS_JSON={$ARG_QUESTIONS_JSON}&$ARG_ANSWERS_JSON={$ARG_ANSWERS_JSON}"
        val arguments = listOf(
            navArgument(ARG_SCORE) { type = NavType.IntType },
            navArgument(ARG_TOTAL) { type = NavType.IntType },
            navArgument(ARG_QUESTIONS_JSON) { type = NavType.StringType; nullable = true },
            navArgument(ARG_ANSWERS_JSON) { type = NavType.StringType; nullable = true },
        )

        fun build(score: Int, total: Int, questionsJson: String? = null, answersJson: String? = null): String {
            var route = "$ROUTE/$score/$total"
            if (questionsJson != null && answersJson != null) {
                route += "?$ARG_QUESTIONS_JSON=$questionsJson&$ARG_ANSWERS_JSON=$answersJson"
            }
            return route
        }
    }

    object Review {
        private const val ROUTE = "review"
        const val ARG_QUESTIONS_JSON = "questionsJson"
        const val ARG_ANSWERS_JSON = "answersJson"

        val routeWithArgs = "$ROUTE/{$ARG_QUESTIONS_JSON}/{$ARG_ANSWERS_JSON}"
        val arguments = listOf(
            navArgument(ARG_QUESTIONS_JSON) { type = NavType.StringType },
            navArgument(ARG_ANSWERS_JSON) { type = NavType.StringType },
        )

        fun build(questionsJson: String, answersJson: String): String = "$ROUTE/$questionsJson/$answersJson"
    }
}
