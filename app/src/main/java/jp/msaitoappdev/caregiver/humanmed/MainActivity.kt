
package jp.msaitoappdev.caregiver.humanmed

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingConstants
import jp.msaitoappdev.caregiver.humanmed.core.billing.BillingManager
import jp.msaitoappdev.caregiver.humanmed.ui.result.ResultRoute
import jp.msaitoappdev.caregiver.humanmed.ui.quiz.QuizRoute
import jp.msaitoappdev.caregiver.humanmed.ui.screens.PaywallScreen

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

        // ← Activity が起動しているか まずここで必ず出るはず
        Log.i("MainActivity", "onCreate called")

        setContent {
            /* Compose UI */
            //Text(greeter.message())

            MaterialTheme {AppNavHost(billing)}
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
                onUpgrade = { navController.navigate("paywall") }
                )
        }
        composable("quiz") { QuizRoute(navController) }
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
            // 画面入場時に接続（初回のみ）
            LaunchedEffect(Unit) { scope.launch { billing.connect() } }
            PaywallScreen(
                onUpgradeClicked = {
                    scope.launch {
                        val pd = billing.getProductDetails(BillingConstants.PRODUCT_ID_PREMIUM_MONTHLY)
                        if (pd != null) billing.launchPurchase(ctx, pd)
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onStartQuiz: () -> Unit,
    onUpgrade: () -> Unit
) {
    val context = LocalContext.current

    // 画面に何も出ないと真っ暗に見えるので Scaffold を噛ませておく
    Scaffold { padding ->
//        Button(
//            onClick = onStartQuiz,
//            modifier = androidx.compose.ui.Modifier.padding(padding)
//        ) {
//            Text("クイズを開始")
//        }
        Column(modifier = Modifier.padding(padding)) {
            Button(onClick = onStartQuiz) { Text("クイズを開始") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onUpgrade) { Text("プレミアムへアップグレード") }
        }
    }
}
