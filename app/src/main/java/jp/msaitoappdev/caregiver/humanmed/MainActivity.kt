package jp.msaitoappdev.caregiver.humanmed

import android.app.Activity
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
import androidx.navigation.compose.*
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import jp.msaitoappdev.caregiver.humanmed.ads.InterstitialHelper
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import com.msaitodev.quiz.feature.history.historyGraph
import jp.msaitoappdev.caregiver.humanmed.feature.home.homeGraph
import jp.msaitoappdev.caregiver.humanmed.feature.premium.paywallGraph
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.quizGraph
import jp.msaitoappdev.caregiver.humanmed.feature.result.resultGraph
import jp.msaitoappdev.caregiver.humanmed.feature.review.reviewGraph
import jp.msaitoappdev.caregiver.humanmed.feature.settings.settingsGraph
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
        homeGraph(
            onStartQuiz = { navController.navigate(NavRoutes.QUIZ) },
            onUpgrade = { navController.navigate(NavRoutes.PAYWALL) },
            onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) }
        )
        quizGraph(navController)
        resultGraph(navController)
        reviewGraph(navController)
        historyGraph(navController)
        paywallGraph()
        settingsGraph(onBack = { navController.popBackStack() })
    }
}
