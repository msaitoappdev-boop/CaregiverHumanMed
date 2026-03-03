package com.msaitodev.quiz.core.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object ReviewDestination {
    private const val ROUTE = "review"
    const val ARG_QUESTIONS_JSON = "questionsJson"
    const val ARG_ANSWERS_JSON = "answersJson"

    val routeWithArgs = "$ROUTE/{$ARG_QUESTIONS_JSON}/{$ARG_ANSWERS_JSON}"
    val arguments = listOf(
        navArgument(ARG_QUESTIONS_JSON) { type = NavType.Companion.StringType },
        navArgument(ARG_ANSWERS_JSON) { type = NavType.Companion.StringType },
    )

    fun build(questionsJson: String, answersJson: String): String = "$ROUTE/$questionsJson/$answersJson"
}
