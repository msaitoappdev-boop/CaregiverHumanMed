package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PremiumViewModel
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes

@Composable
fun QuizRoute(navController: NavController) {
    val TAG = "QuizRoute"

    // --- Quiz VM / 状態 ---
    val vm: QuizViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    // --- Premium 状態を購読 ---
    val pvm: PremiumViewModel = hiltViewModel()
    val isPremium by pvm.isPremium.collectAsState(initial = false)

    // --- 「再挑戦」やり取り（既存ロジック維持） ---
    val currentEntry by navController.currentBackStackEntryAsState()
    val quizEntry = remember(currentEntry) {
        runCatching { navController.getBackStackEntry(NavRoutes.QUIZ) }.getOrNull()
    }
    val savedStateHandle = quizEntry?.savedStateHandle
    val tickFlow = remember(savedStateHandle) { savedStateHandle?.getStateFlow("reshuffleTick", 0L) }
    val tick by (tickFlow?.collectAsState(initial = 0L) ?: remember { mutableStateOf(0L) })

    LaunchedEffect(tick) {
        if (tick != 0L && savedStateHandle != null) {
            val reshuffle = savedStateHandle.get<Boolean>("reshuffle")
            if (reshuffle != null) {
                vm.reset(reshuffle = reshuffle)
                savedStateHandle.remove<Boolean>("reshuffle")
            }
        }
    }

    // --- 結果画面へ遷移 ---
    LaunchedEffect(state.finished) {
        if (state.finished) {
            navController.navigate(
                NavRoutes.Result.build(state.correctCount, state.total)
            ) {
                popUpTo(NavRoutes.QUIZ) { inclusive = false }
            }
            vm.markResultNavigated()
        }
    }

    // --- 戻るキー制御 ---
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

    QuizScreen(
        state = state,
        onSelect = vm::selectOption,
        onNext   = vm::next,
        onPrev   = vm::prev,
        onNavUp  = onNavUp,
        // ★ プレミアム権限（解説の全文表示）
        canShowFullExplanation = isPremium,
        // ★ 非プレミアム → Paywall へ誘導
        onUpgrade = goToPaywall
    )
}
