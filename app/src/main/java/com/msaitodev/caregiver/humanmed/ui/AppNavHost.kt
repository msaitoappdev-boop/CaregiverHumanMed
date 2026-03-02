package com.msaitodev.caregiver.humanmed.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.msaitodev.core.ads.ConsentManager
import com.msaitodev.core.ads.InterstitialHelper
import com.msaitodev.core.ads.RewardedHelper
import com.msaitodev.core.common.navigation.AppActions
import com.msaitodev.core.navigation.HistoryDestination
import com.msaitodev.core.navigation.HomeDestination
import com.msaitodev.core.navigation.PaywallDestination
import com.msaitodev.core.navigation.QuizDestination
import com.msaitodev.core.navigation.ResultDestination
import com.msaitodev.core.navigation.ReviewDestination
import com.msaitodev.core.navigation.SettingsDestination
import com.msaitodev.feature.billing.paywallGraph
import com.msaitodev.quiz.feature.history.historyGraph
import com.msaitodev.quiz.feature.main.home.HomeRoute
import com.msaitodev.quiz.feature.main.quiz.QuizResult
import com.msaitodev.quiz.feature.main.quiz.quizGraph
import com.msaitodev.quiz.feature.result.resultGraph
import com.msaitodev.quiz.feature.review.reviewGraph
import com.msaitodev.feature.settings.settingsGraph
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
internal fun AppNavHost(
    interstitialHelper: InterstitialHelper,
    rewardedHelper: RewardedHelper,
) {
    val activity = LocalContext.current as Activity

    val navController = rememberNavController()
    var quizResultForProcessing by remember { mutableStateOf<QuizResult?>(null) }

    LaunchedEffect(quizResultForProcessing) {
        val result = quizResultForProcessing ?: return@LaunchedEffect

        val questionsJson = URLEncoder.encode(Json.encodeToString(result.questions), StandardCharsets.UTF_8.toString())
        val answersJson = URLEncoder.encode(Json.encodeToString(result.answers), StandardCharsets.UTF_8.toString())

        navController.navigate(ResultDestination.build(result.score, result.total, result.isReview, questionsJson, answersJson))
        quizResultForProcessing = null // Prevent re-processing
    }

    LaunchedEffect(Unit) {
        ConsentManager.obtain(activity) {
            MobileAds.initialize(activity.applicationContext)
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
            interstitialHelper.preload()
        }
    }

    NavHost(navController, startDestination = HomeDestination.route) {
        composable(HomeDestination.route) {
            HomeRoute(
                rewardedHelper = rewardedHelper,
                onStartQuiz = { navController.navigate(QuizDestination.route) },
                onViewHistory = { navController.navigate(HistoryDestination.route) },
                onUpgrade = { navController.navigate(PaywallDestination.route) },
                onOpenSettings = { navController.navigate(SettingsDestination.route) }
            )
        }

        quizGraph(
            navController = navController,
            onQuizFinished = { result ->
                quizResultForProcessing = result
            },
            onUpgrade = { navController.navigate(PaywallDestination.route) }
        )

        resultGraph(
            navController = navController,
            rewardedHelper = rewardedHelper,
            onNextSet = {
                navController.previousBackStackEntry?.savedStateHandle?.set(AppActions.KEY_ACTION, AppActions.ACTION_START_NEW)
                navController.popBackStack()
            },
            onReview = { questionsJson, answersJson ->
                navController.navigate(ReviewDestination.build(questionsJson, answersJson))
            },
            onReviewSameOrder = {
                navController.previousBackStackEntry?.savedStateHandle?.set(AppActions.KEY_ACTION, AppActions.ACTION_RESTART_SAME_ORDER)
                navController.popBackStack()
            },
            onShowScoreHistory = { navController.navigate(HistoryDestination.route) },
            onBackToHome = { navController.popBackStack(HomeDestination.route, inclusive = false) }
        )

        reviewGraph(navController)
        historyGraph(navController)
        paywallGraph()
        settingsGraph(onBack = { navController.popBackStack() })
    }
}