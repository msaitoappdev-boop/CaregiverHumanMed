package jp.msaitoappdev.caregiver.humanmed

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
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.core.navigation.QuizActions
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.RemoteConfigRepository
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeRoute
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeViewModel
import com.msaitodev.quiz.feature.billing.paywallGraph
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.QuizResult
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.quizGraph
import jp.msaitoappdev.caregiver.humanmed.feature.result.resultGraph
import com.msaitodev.quiz.feature.review.reviewGraph
import jp.msaitoappdev.caregiver.humanmed.feature.settings.settingsGraph
import com.msaitodev.quiz.core.ads.ConsentManager
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

        navController.navigate(NavRoutes.Result.build(result.score, result.total, result.isReview, questionsJson, answersJson))
        quizResultForProcessing = null // Prevent re-processing
    }

    LaunchedEffect(Unit) {
        ConsentManager.obtain(activity) {
            MobileAds.initialize(activity.applicationContext)
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
            interstitialHelper.preload()
        }
    }

    NavHost(navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            val rewardedAdError = stringResource(id = R.string.common_error_rewarded_ad)

            HomeRoute(
                onStartQuiz = { navController.navigate(NavRoutes.QUIZ) },
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
                onUpgrade = { navController.navigate(NavRoutes.PAYWALL) },
                onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) }
            )
        }

        quizGraph(
            navController = navController,
            onQuizFinished = { result ->
                quizResultForProcessing = result
            },
            onUpgrade = { navController.navigate(NavRoutes.PAYWALL) }
        )

        resultGraph(
            navController = navController,
            rewardedHelper = rewardedHelper,
            onNextSet = {
                navController.previousBackStackEntry?.savedStateHandle?.set(QuizActions.KEY_QUIZ_ACTION, QuizActions.ACTION_START_NEW)
                navController.popBackStack()
            },
            onReview = { questionsJson, answersJson ->
                navController.navigate(NavRoutes.Review.build(questionsJson, answersJson))
            },
            onReviewSameOrder = {
                navController.previousBackStackEntry?.savedStateHandle?.set(QuizActions.KEY_QUIZ_ACTION, QuizActions.ACTION_RESTART_SAME_ORDER)
                navController.popBackStack()
            },
            onShowScoreHistory = { navController.navigate(NavRoutes.HISTORY) },
            onBackToHome = { navController.popBackStack(NavRoutes.HOME, inclusive = false) }
        )

        reviewGraph(navController)
        historyGraph(navController)
        paywallGraph()
        settingsGraph(onBack = { navController.popBackStack() })
    }
}
