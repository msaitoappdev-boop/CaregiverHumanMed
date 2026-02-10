package jp.msaitoappdev.caregiver.humanmed

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.Manifest
import android.widget.Toast
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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import jp.msaitoappdev.caregiver.humanmed.ads.InterstitialHelper
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderScheduler
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.QuizRoute
import jp.msaitoappdev.caregiver.humanmed.feature.result.ResultRoute
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PaywallScreen
import jp.msaitoappdev.caregiver.humanmed.feature.settings.SettingsRoute
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

import androidx.lifecycle.lifecycleScope
import jp.msaitoappdev.caregiver.humanmed.feature.history.HistoryRoute
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeEffect
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PremiumViewModel
import jp.msaitoappdev.caregiver.humanmed.feature.review.ReviewRoute

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeVM
import com.google.firebase.analytics.ktx.analytics

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var premiumRepo: jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
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
        jp.msaitoappdev.caregiver.humanmed.privacy.ConsentManager.obtain(activity) {
            com.google.android.gms.ads.MobileAds.initialize(activity.applicationContext)
            com.google.firebase.ktx.Firebase.analytics.setAnalyticsCollectionEnabled(true)
            // AdMob is initialized; now safe to preload interstitial ads (consent respected)
            interstitialHelper.preload()
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
            val vm: PremiumViewModel = hiltViewModel()
            val ctx = LocalContext.current as Activity
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                vm.uiMessage.collect { msg ->
                    snackbarHostState.showSnackbar(message = msg)
                }
            }

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
    val vm: HomeVM = hiltViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()
    var showOffer by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val act = LocalContext.current as Activity
    val context = LocalContext.current

    // ViewModelからの一度きりのイベント(Effect)を監視・処理する
    LaunchedEffect(vm.effect) {
        vm.effect.collect {
            when (it) {
                is HomeEffect.NavigateToQuiz -> onStartQuiz()
                is HomeEffect.ShowRewardedAdOffer -> showOffer = true
                is HomeEffect.ShowMessage -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    // 通知許可をリクエストするためのランチャー
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // 許可された場合、リマインダーをスケジュールする
            ReminderScheduler.scheduleDaily(context, 20, 0)
        }
    }

    var showRationale by remember { mutableStateOf(false) } // 通知許可の根拠を示すダイアログの表示状態

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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // このボタンはViewModelにイベントを通知するだけで、UI側ではロジックを持たない
            Button(onClick = { vm.onStartQuizClicked() }) {
                Text("クイズを開始")
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onUpgrade) { Text("プレミアムへアップグレード") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                // Android 13 (TIRAMISU) 以降では、通知許可を求めるダイアログを表示
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showRationale = true
                } else {
                    // それ以前のバージョンでは、許可なしでリマインダーを設定
                    ReminderScheduler.scheduleDaily(context, 20, 0)
                }
            }) { Text("毎日20:00にリマインドを設定") }
        }
    }

    // ViewModelからの指示があった場合にのみ、リワード広告の提案ダイアログを表示する
    if (showOffer) {
        AlertDialog(
            onDismissRequest = { showOffer = false },
            title = { Text("今日は無料分が終了しました") },
            text = { Text("動画を視聴すると +1 セット解放できます。視聴しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    showOffer = false
                    jp.msaitoappdev.caregiver.humanmed.ads.RewardedHelper.show(
                        activity = act,
                        // このダイアログが表示される時点で、広告表示の可否はViewModelで判断済み。
                        // そのため、UI側では常にtrueを渡す。
                        canShowToday = { true },
                        onEarned = { _ ->
                            scope.launch {
                                val ok = vm.tryGrantDailyPlusOne()
                                if (ok) {
                                    // 報酬獲得後、再度クイズ開始ロジックをトリガーする
                                    // これにより、ViewModelは最新の状態でクイズ開始可否を判断する
                                    vm.onStartQuizClicked()
                                }
                            }
                        },
                        onFail = {
                            Toast.makeText(context, "動画を読み込めませんでした（ネットワーク/在庫/初期化）", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) { Text("動画を視聴して +1 セット") }
            },
            dismissButton = {
                TextButton(onClick = { showOffer = false }) { Text("キャンセル") }
            }
        )
    }

    // 通知許可の根拠を示すダイアログ
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
