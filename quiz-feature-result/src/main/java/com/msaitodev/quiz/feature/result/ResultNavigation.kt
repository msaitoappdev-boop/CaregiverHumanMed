package com.msaitodev.quiz.feature.result

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.msaitodev.core.ads.RewardedHelper
import com.msaitodev.quiz.core.navigation.ResultDestination

fun NavGraphBuilder.resultGraph(
    navController: NavController,
    rewardedHelper: RewardedHelper,
    onNextSet: () -> Unit,
    onReview: (questionsJson: String, answersJson: String) -> Unit,
    onReviewSameOrder: () -> Unit,
    onShowScoreHistory: () -> Unit,
    onBackToHome: () -> Unit
) {
    composable(
        route = ResultDestination.routeWithArgs,
        arguments = ResultDestination.arguments
    ) { backStackEntry ->
        val score = backStackEntry.arguments?.getInt(ResultDestination.ARG_SCORE) ?: 0
        val total = backStackEntry.arguments?.getInt(ResultDestination.ARG_TOTAL) ?: 0
        ResultRoute(
            score = score,
            total = total,
            rewardedHelper = rewardedHelper,
            onNextSet = onNextSet,
            onReview = onReview,
            onReviewSameOrder = onReviewSameOrder,
            onShowScoreHistory = onShowScoreHistory,
            onBackToHome = onBackToHome
        )
    }
}
