package jp.msaitoappdev.caregiver.humanmed

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import jp.msaitoappdev.caregiver.humanmed.ads.InterstitialHelper
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import jp.msaitoappdev.caregiver.humanmed.feature.history.HistoryRoute
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeRoute
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PaywallRoute
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.QuizRoute
import jp.msaitoappdev.caregiver.humanmed.feature.result.ResultRoute
import jp.msaitoappdev.caregiver.humanmed.feature.review.ReviewRoute
import jp.msaitoappdev.caregiver.humanmed.feature.settings.SettingsRoute
import jp.msaitoappdev.caregiver.humanmed.privacy.ConsentManager
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var premiumRepo: PremiumRepository
    @Inject lateinit var interstitialHelper: InterstitialHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("MainActivity", "onCreate called")

        // ライフサイクルイベントの監視
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                // フォアグラウンド復帰のタイミングで、ユーザーの現在の購入状態を最新に同期する
                lifecycleScope.launch {
                    Log.d("BugHunt-Premium", "MainActivity.onResume: Kicking off refreshFromBilling()")
                    premiumRepo.refreshFromBilling()
                }
            }
        })

        setContent {
            MaterialTheme {
                // Pass interstitialHelper into the NavHost so we can preload after consent & MobileAds.initialize
                AppNavHost(interstitialHelper)
            }
        }
    }
}

@Composable
private fun AppNavHost(interstitialHelper: InterstitialHelper) {
    val activity = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        // UMP同意ダイアログを表示し、ユーザーの選択に応じて広告とアナリティクスを初期化する
        ConsentManager.obtain(activity) {
            com.google.android.gms.ads.MobileAds.initialize(activity.applicationContext)
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
            // AdMob is initialized; now safe to preload interstitial ads (consent respected)
            interstitialHelper.preload()
        }
    }

    val navController = rememberNavController()
    NavHost(navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            HomeRoute(
                onStartQuiz = { navController.navigate(NavRoutes.QUIZ) },
                onUpgrade = { navController.navigate(NavRoutes.PAYWALL) },
                onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) }
            )
        }
        composable(
            route = NavRoutes.QUIZ,
            deepLinks = listOf(navDeepLink { uriPattern = "caregiver://reminder" })
        ) { QuizRoute(navController) }
        composable(
            route = NavRoutes.Result.PATTERN,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0

            // ResultRouteは表示に専念し、ロジックはViewModelに集約されている
            ResultRoute(navController, score, total)
        }
        composable(NavRoutes.REVIEW) {
            ReviewRoute(navController)
        }
        composable(NavRoutes.HISTORY) {
            HistoryRoute(navController)
        }

        composable(NavRoutes.PAYWALL) {
            PaywallRoute()
        }
        composable(NavRoutes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
