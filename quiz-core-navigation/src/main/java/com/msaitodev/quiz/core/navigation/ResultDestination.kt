package com.msaitodev.quiz.core.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object ResultDestination {
    private const val ROUTE = "result"
    const val ARG_SCORE = "score"
    const val ARG_TOTAL = "total"
    const val ARG_IS_REVIEW = "isReview"
    const val ARG_QUESTIONS_JSON = "questionsJson"
    const val ARG_ANSWERS_JSON = "answersJson"

    val routeWithArgs = "$ROUTE/{$ARG_SCORE}/{$ARG_TOTAL}/{$ARG_IS_REVIEW}?$ARG_QUESTIONS_JSON={$ARG_QUESTIONS_JSON}&$ARG_ANSWERS_JSON={$ARG_ANSWERS_JSON}"
    val arguments = listOf(
        navArgument(ARG_SCORE) { type = NavType.Companion.IntType },
        navArgument(ARG_TOTAL) { type = NavType.Companion.IntType },
        navArgument(ARG_IS_REVIEW) { type = NavType.Companion.BoolType; defaultValue = false },
        navArgument(ARG_QUESTIONS_JSON) {
            type = NavType.Companion.StringType; nullable = true
        },
        navArgument(ARG_ANSWERS_JSON) { type = NavType.Companion.StringType; nullable = true },
    )

    fun build(score: Int, total: Int, isReview: Boolean, questionsJson: String? = null, answersJson: String? = null): String {
        var route = "$ROUTE/$score/$total/$isReview"
        if (questionsJson != null && answersJson != null) {
            route += "?$ARG_QUESTIONS_JSON=$questionsJson&$ARG_ANSWERS_JSON=$answersJson"
        }
        return route
    }
}
