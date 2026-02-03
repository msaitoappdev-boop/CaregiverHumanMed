
package jp.msaitoappdev.caregiver.humanmed

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderScheduler
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.QuizRoute
import jp.msaitoappdev.caregiver.humanmed.feature.result.ResultRoute
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PaywallScreen
import jp.msaitoappdev.caregiver.humanmed.feature.settings.SettingsRoute
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

import androidx.lifecycle.lifecycleScope
import jp.msaitoappdev.caregiver.humanmed.feature.history.HistoryRoute
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PremiumViewModel
import jp.msaitoappdev.caregiver.humanmed.feature.review.ReviewRoute

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeVM
import com.google.firebase.analytics.ktx.analytics

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var premiumRepo: jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("MainActivity", "onCreate called")

        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // フォアグラウンド復帰のタイミングで、購入状態を軽く同期
        lifecycleScope.launch {
            premiumRepo.refreshFromBilling()
        }
    }
}

@Composable
private fun AppNavHost() {
    val ctx = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        // UMP同意 → 同意OKの時のみ Ads/Analytics を有効化
        jp.msaitoappdev.caregiver.humanmed.privacy.ConsentManager.obtain(ctx) {
            com.google.android.gms.ads.MobileAds.initialize(ctx.applicationContext)
            com.google.firebase.ktx.Firebase.analytics.setAnalyticsCollectionEnabled(true)
        }
    }

    val navController = rememberNavController()
    NavHost(navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            HomeScreen(
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
            ResultRoute(navController, score, total)
        }
        composable(NavRoutes.REVIEW) {
            ReviewRoute(navController)
        }
        composable(NavRoutes.HISTORY) {
            HistoryRoute(navController)
        }

        composable(NavRoutes.PAYWALL) {
            val vm: PremiumViewModel = hiltViewModel()
            val ctx = LocalContext.current as Activity
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            // 任意: ViewModel からのメッセージを Snackbar に表示
            LaunchedEffect(Unit) {
                vm.uiMessage.collect { msg ->
                    snackbarHostState.showSnackbar(message = msg)
                }
            }

            // UI 本体
            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
                Box(Modifier.padding(inner)) {
                    PaywallScreen(
                        onUpgradeClicked = { vm.startPurchase(ctx) }
                    )
                }
            }
        }
        composable(NavRoutes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartQuiz: () -> Unit,
    onUpgrade: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // ★ 追加：当日枠ゲート用 VM / 状態
    val vm: HomeVM = hiltViewModel()
    val rewardedCountToday by vm.rewardedCountToday.collectAsStateWithLifecycle()
    val canStart by vm.canStartFlow.collectAsStateWithLifecycle()
    var showOffer by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val act = LocalContext.current as Activity

    val context = LocalContext.current

    // POST_NOTIFICATIONS の許可ランチャ
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // 権限付与後に毎日20:00でスケジュール
            ReminderScheduler.scheduleDaily(context, 20, 0)
        }
    }

    var showRationale by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ホーム") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "設定")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            // ★ 修正：枠があれば開始、なければ +1 提案へ
            Button(onClick = {
                if (canStart) onStartQuiz() else showOffer = true
            }) {
                Text("クイズを開始")
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onUpgrade) { Text("プレミアムへアップグレード") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                // Android 13+ のみランタイム許可が必要
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showRationale = true  // 事前説明→許可
                } else {
                    ReminderScheduler.scheduleDaily(context, 20, 0)
                }
            }) { Text("毎日20:00にリマインドを設定") }
            // ---- HomeScreen() のボタン群の下あたりに一時的に追記 ----
//            if (BuildConfig.DEBUG) {
//                androidx.compose.material3.Divider()
//                androidx.compose.material3.Text("広告デバッグパネル", color = androidx.compose.ui.graphics.Color.Gray)
//                jp.msaitoappdev.caregiver.humanmed.debug.AdsDebugPanel()
//            }
        }
    }

    // ★ 追加：枠不足時の +1 提案ダイアログ（Rewarded）
    if (showOffer) {
        AlertDialog(
            onDismissRequest = { showOffer = false },
            title = { Text("今日は無料分が終了しました") },
            text  = { Text("動画を視聴すると +1 セット解放できます。視聴しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    showOffer = false
                    jp.msaitoappdev.caregiver.humanmed.ads.RewardedHelper.show(
                        activity = act,
                        canShowToday = { rewardedCountToday < 1 },
                        onEarned = { _ ->
                            scope.launch {
                                val ok = vm.tryGrantDailyPlusOne()
                                if (ok) {
                                    onStartQuiz()
                                } else {
                                    // 既に本日は付与済み：SnackBar/Toastなど
                                }
                            }
                        }
                        ,
                        onFail = { /* 何もしない（キャンセル） */ }
                    )
                }) { Text("動画を視聴して +1 セット") }
            },
            dismissButton = {
                TextButton(onClick = { showOffer = false }) { Text("キャンセル") }
            }
        )
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("通知の許可が必要です") },
            text = { Text("毎日20:00に『今日の3問』をお知らせします。通知を許可してください。") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text("許可する") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text("後で") }
            }
        )
    }
}
