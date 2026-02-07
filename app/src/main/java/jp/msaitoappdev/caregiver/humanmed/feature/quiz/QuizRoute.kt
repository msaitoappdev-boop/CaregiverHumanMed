package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeVM
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PremiumViewModel

/**
 * クイズ画面のルート。
 * - 「再挑戦」（Result からの戻り）を savedStateHandle で受け取り、
 *   canStart（当日の残枠）が true のときだけ vm.reset(...) を実行する（二重防御）
 * - セット完了時は Result へナビゲート（NavHost 側で usedSets += 1 を計上する前提）
 */
@Composable
fun QuizRoute(navController: NavController) {
    val TAG = "QuizRoute"
    val context = LocalContext.current

    // --- Quiz VM / 状態 ---
    val vm: QuizViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    // --- Premium 状態 ---
    val pvm: PremiumViewModel = hiltViewModel()
    val isPremium by pvm.isPremium.collectAsStateWithLifecycle(initialValue = false)

    // ★★★ isPremium の状態変化をログで追跡 ★★★
    LaunchedEffect(isPremium) {
        Log.d(TAG, "isPremium state changed to: $isPremium")
    }

    // --- 当日の残枠（canStart）を監視（Result→Quiz の再挑戦ガード用） ---
    val homeVm: HomeVM = hiltViewModel()
    val canStart by homeVm.canStartFlow.collectAsStateWithLifecycle()

    // --- 「再挑戦」やり取り（Result → Quiz）：savedStateHandle を介す既存ロジック ---
    val currentEntry by navController.currentBackStackEntryAsState()
    val quizEntry = remember(currentEntry) {
        runCatching { navController.getBackStackEntry(NavRoutes.QUIZ) }.getOrNull()
    }
    val savedStateHandle = quizEntry?.savedStateHandle
    val tickFlow = remember(savedStateHandle) {
        savedStateHandle?.getStateFlow("reshuffleTick", 0L)
    }
    val tick by (tickFlow?.collectAsStateWithLifecycle(initialValue = 0L)
        ?: remember { mutableStateOf(0L) })

    // Result 側から tick が届いたら、当日の残枠があるときだけ reset() を実行
    LaunchedEffect(tick) {
        if (tick != 0L && savedStateHandle != null) {
            val reshuffle = savedStateHandle.get<Boolean>("reshuffle")
            if (reshuffle != null) {
                if (canStart) {
                    Log.d(TAG, "Reset quiz. reshuffle=$reshuffle (canStart=true)")
                    vm.reset(reshuffle = reshuffle)
                } else {
                    Log.d(TAG, "Skip reset (canStart=false). Show toast.")
                    Toast.makeText(context, "本日の枠は終了しました", Toast.LENGTH_SHORT).show()
                }
                // 再発火を防ぐために必ず削除
                savedStateHandle.remove<Boolean>("reshuffle")
            }
        }
    }

    // --- セット完了 → 結果へ遷移（NavHost 側で usedSets += 1 計上） ---
    LaunchedEffect(state.finished) {
        if (state.finished) {
            navController.navigate(
                NavRoutes.Result.build(state.correctCount, state.total)
            ) {
                // quiz は残す（VM維持のため）
                popUpTo(NavRoutes.QUIZ) { inclusive = false }
            }
            vm.markResultNavigated()
        }
    }

    // --- 戻るキー制御（1問目以外で戻る＝前問へ） ---
    BackHandler(enabled = !state.isLoading && state.currentIndex > 0) { vm.prev() }
    val onNavUp: () -> Unit = {
        if (state.currentIndex > 0) vm.prev() else navController.popBackStack()
    }

    // --- Paywall 遷移クロージャ（ログ付き） ---
    val goToPaywall: () -> Unit = {
        val exists = navController.graph.findNode(NavRoutes.PAYWALL) != null
        Log.d(TAG, "onUpgrade clicked. paywall exists=$exists -> navigate(\"paywall\")")
        navController.navigate(NavRoutes.PAYWALL) {
            launchSingleTop = true
            // quiz は残す（VM維持のため）
            // popUpTo("quiz") { inclusive = false }
        }
    }

    // --- UI 本体 ---
    QuizScreen(
        state = state,
        onSelect = vm::selectOption,
        onNext   = vm::next,
        onPrev   = vm::prev,
        onNavUp  = onNavUp,
        // プレミアム：解説の全文表示
        canShowFullExplanation = isPremium,
        // 非プレミアム → Paywall 誘導
        onUpgrade = goToPaywall
    )
}
