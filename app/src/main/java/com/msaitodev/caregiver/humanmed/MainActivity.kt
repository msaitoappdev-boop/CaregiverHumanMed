package com.msaitodev.caregiver.humanmed

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.msaitodev.quiz.core.ads.RewardedHelper
import com.msaitodev.quiz.feature.history.historyGraph
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import com.msaitodev.quiz.core.ads.InterstitialHelper
import com.msaitodev.quiz.core.ads.ConsentManager
import com.msaitodev.quiz.core.navigation.HistoryDestination
import com.msaitodev.quiz.core.navigation.HomeDestination
import com.msaitodev.quiz.core.navigation.PaywallDestination
import com.msaitodev.quiz.core.navigation.QuizDestination
import com.msaitodev.quiz.core.navigation.ResultDestination
import com.msaitodev.quiz.core.navigation.ReviewDestination
import com.msaitodev.quiz.core.navigation.SettingsDestination
import com.msaitodev.quiz.feature.billing.paywallGraph
import com.msaitodev.quiz.feature.main.home.HomeRoute
import com.msaitodev.quiz.feature.main.home.HomeViewModel
import com.msaitodev.quiz.feature.main.quiz.QuizResult
import com.msaitodev.quiz.feature.main.quiz.quizGraph
import com.msaitodev.quiz.feature.result.resultGraph
import com.msaitodev.quiz.feature.review.reviewGraph
import com.msaitodev.quiz.feature.settings.settingsGraph
import com.msaitodev.quiz.core.common.navigation.QuizActions
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var premiumRepo: PremiumRepository

    @Inject
    lateinit var interstitialHelper: InterstitialHelper

    @Inject
    lateinit var rewardedHelper: RewardedHelper

    @Inject
    lateinit var remoteConfigRepo: RemoteConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("MainActivity", "onCreate called")

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                lifecycleScope.launch {
                    premiumRepo.refreshFromBilling()
                }
            }
        })

        setContent {
            MaterialTheme {
                AppNavHost(interstitialHelper, rewardedHelper)
            }
        }
    }
}

@Composable
private fun AppNavHost(
    interstitialHelper: InterstitialHelper,
    rewardedHelper: RewardedHelper,
) {
    val activity = LocalContext.current as Activity
    val context = LocalContext.current

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
            val vm: HomeViewModel = hiltViewModel()
            val rewardedAdError = stringResource(id = R.string.common_error_rewarded_ad)

            HomeRoute(
                onStartQuiz = { navController.navigate(QuizDestination.route) },
                onShowRewardedAd = {
                    rewardedHelper.show(
                        activity = activity,
                        canShowToday = { true },
                        onEarned = { vm.onRewardedAdEarned() },
                        onFail = {
                            Toast.makeText(context, rewardedAdError, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
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
                navController.previousBackStackEntry?.savedStateHandle?.set(QuizActions.KEY_QUIZ_ACTION, QuizActions.ACTION_START_NEW)
                navController.popBackStack()
            },
            onReview = { questionsJson, answersJson ->
                navController.navigate(ReviewDestination.build(questionsJson, answersJson))
            },
            onReviewSameOrder = {
                navController.previousBackStackEntry?.savedStateHandle?.set(QuizActions.KEY_QUIZ_ACTION, QuizActions.ACTION_RESTART_SAME_ORDER)
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
