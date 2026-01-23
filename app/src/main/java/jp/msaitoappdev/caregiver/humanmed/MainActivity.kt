
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
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingConfig
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderScheduler
import jp.msaitoappdev.caregiver.humanmed.ui.quiz.QuizRoute
import jp.msaitoappdev.caregiver.humanmed.ui.result.ResultRoute
import jp.msaitoappdev.caregiver.humanmed.ui.screens.PaywallScreen
import jp.msaitoappdev.caregiver.humanmed.ui.settings.SettingsRoute

// 任意の依存（@Inject 付きコンストラクタで十分）
class Greeter @Inject constructor() {
    fun message(): String = "Hello, Hilt + KSP!"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var greeter: Greeter
    @Inject lateinit var billing: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("MainActivity", "onCreate called")

        setContent {
            MaterialTheme {
                AppNavHost(billing)
            }
        }
    }
}

@Composable
private fun AppNavHost(billing: BillingManager) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartQuiz = { navController.navigate("quiz") },
                onUpgrade = { navController.navigate("paywall") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable(
            route = "quiz",
            deepLinks = listOf(navDeepLink { uriPattern = "caregiver://reminder" })
        ) { QuizRoute(navController) }
        composable(
            route = "result/{score}/{total}",
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            ResultRoute(navController, score, total)
        }
        composable("review") {
            jp.msaitoappdev.caregiver.humanmed.ui.review.ReviewRoute(navController)
        }
        composable("history") {
            jp.msaitoappdev.caregiver.humanmed.ui.history.HistoryRoute(navController)
        }
        composable("paywall") {
            val scope = rememberCoroutineScope()
            val ctx = LocalContext.current as Activity
            // 画面入場時に一度だけ接続
            LaunchedEffect(Unit) { scope.launch { billing.connect() } }

            // 簡易トースト表示ヘルパ
            fun toast(msg: String) =
                android.widget.Toast.makeText(ctx, msg, android.widget.Toast.LENGTH_SHORT).show()

            // Billing のイベントもログに出す（成功/失敗の可視化）
            LaunchedEffect(Unit) {
                scope.launch {
                    billing.purchaseEvents.collect { e ->
                        when (e) {
                            is jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager.PurchaseEvent.Success -> {
                                android.util.Log.d("Paywall", "purchase success: ${e.purchase.products}")
                                toast("購入が完了しました")
                                // 購入後の挙動（例：前の画面に戻る）
                                // navController.popBackStack() など
                            }
                            jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager.PurchaseEvent.Canceled -> {
                                android.util.Log.d("Paywall", "purchase canceled")
                                toast("購入をキャンセルしました")
                            }
                            jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager.PurchaseEvent.AlreadyOwned -> {
                                android.util.Log.d("Paywall", "already owned")
                                toast("すでに購入済みです")
                            }
                            is jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager.PurchaseEvent.Error -> {
                                android.util.Log.e("Paywall", "purchase error: ${e.message}")
                                toast("購入エラー: ${e.message}")
                            }
                        }
                    }
                }
            }

            PaywallScreen(
                onUpgradeClicked = {
                    scope.launch {
                        val pd = billing.getProductDetails(
                            BillingConfig.PRODUCT_ID_PREMIUM_MONTHLY)

                        if (pd == null) {
                            android.util.Log.e("Paywall", "ProductDetails is null. Check Play Console config & tester setup.")
                            toast("商品情報を取得できません。Play コンソール設定/テスター設定をご確認ください。")
                            return@launch
                        }
                        billing.launchPurchase(ctx, pd) // → ここで Google Play の購入ダイアログが出るのが正
                    }
                }
            )
        }
        composable("settings") {
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
            Button(onClick = onStartQuiz) { Text("クイズを開始") }
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
        }
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
