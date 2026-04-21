package com.msaitodev.caregiver.humanmed.ui

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.msaitodev.core.ads.ConsentManager
import com.msaitodev.core.ads.InterstitialHelper
import com.msaitodev.core.ads.RewardedHelper
import com.msaitodev.core.common.navigation.AppActions
import com.msaitodev.core.navigation.PaywallDestination
import com.msaitodev.core.navigation.SettingsDestination
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import com.msaitodev.quiz.core.navigation.AnalysisDestination
import com.msaitodev.quiz.core.navigation.HistoryDestination
import com.msaitodev.quiz.core.navigation.HomeDestination
import com.msaitodev.quiz.core.navigation.QuizDestination
import com.msaitodev.quiz.core.navigation.ResultDestination
import com.msaitodev.quiz.core.navigation.ReviewDestination
import com.msaitodev.feature.billing.paywallGraph
import com.msaitodev.quiz.feature.analysis.analysisGraph
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
    remoteConfigRepo: RemoteConfigRepository,
    premiumRepo: PremiumRepository,
) {
    val activity = LocalContext.current as Activity

    val navController = rememberNavController()
    var quizResultForProcessing by remember { mutableStateOf<QuizResult?>(null) }

    val isPremium by premiumRepo.isPremium.collectAsState(initial = true)
    
    // 現在の画面情報を監視
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(quizResultForProcessing) {
        val result = quizResultForProcessing ?: return@LaunchedEffect

        val questionsJson = URLEncoder.encode(Json.encodeToString(result.questions), StandardCharsets.UTF_8.toString())
        val answersJson = URLEncoder.encode(Json.encodeToString(result.answers), StandardCharsets.UTF_8.toString())

        navController.navigate(ResultDestination.build(result.score, result.total, result.isReview, questionsJson, answersJson))
        quizResultForProcessing = null // Prevent re-processing
    }

    LaunchedEffect(isPremium) {
        // アナリティクスはユーザーの状態に関わらず有効化
        Firebase.analytics.setAnalyticsCollectionEnabled(true)

        // 無料ユーザーの場合のみ広告を初期化・プリロードする
        if (!isPremium) {
            ConsentManager.obtain(activity) {
                MobileAds.initialize(activity.applicationContext)
                interstitialHelper.preload()
                rewardedHelper.preload()
            }
        }
    }

    NavHost(navController, startDestination = HomeDestination.route) {
        composable(HomeDestination.route) {
            // ホーム画面が表示されるたびに（戻ってきた時も含む）ヒントを抽選する
            var currentHint by remember { mutableStateOf("") }
            
            LaunchedEffect(currentRoute, isPremium) {
                if (currentRoute == HomeDestination.route) {
                    val key = if (isPremium) "app_hints_premium_user" else "app_hints_free_user"
                    val rawJson = remoteConfigRepo.getString(key)
                    
                    if (rawJson.isNotEmpty()) {
                        currentHint = try {
                            val hints = Json.decodeFromString<List<String>>(rawJson)
                            // シャッフルして先頭を取ることでランダム性を高める
                            hints.shuffled().firstOrNull() ?: ""
                        } catch (e: Exception) {
                            rawJson
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    HomeRoute(
                        rewardedHelper = rewardedHelper,
                        onStartQuiz = { 
                            navController.navigate(QuizDestination.route) 
                        },
                        onViewHistory = { navController.navigate(HistoryDestination.buildRoute()) },
                        onViewAnalysis = { navController.navigate(AnalysisDestination.route) },
                        onUpgrade = { navController.navigate(PaywallDestination.route) },
                        onOpenSettings = { navController.navigate(SettingsDestination.route) }
                    )
                }
                if (currentHint.isNotEmpty()) {
                    AppHintBanner(message = currentHint)
                }
            }
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
            onBackToHome = { navController.popBackStack(HomeDestination.route, inclusive = false) }
        )

        reviewGraph(navController)
        
        historyGraph(navController)

        analysisGraph(
            navController = navController,
            onNavigateToSettings = {
                navController.navigate(SettingsDestination.route) {
                    popUpTo(AnalysisDestination.route) { inclusive = true }
                }
            },
            onNavigateToHistory = { dateKey ->
                navController.navigate(HistoryDestination.buildRoute(dateKey))
            }
        )

        paywallGraph(onBack = { navController.popBackStack() })

        settingsGraph(onBack = { navController.popBackStack() })
    }
}

@Composable
fun AppHintBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
