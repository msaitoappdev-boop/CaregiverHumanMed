
package jp.msaitoappdev.caregiver.humanmed.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import jp.msaitoappdev.caregiver.humanmed.data.QuestionRepository

@Composable
fun QuizRoute(navController: NavController) {
    val vm: QuizViewModel =
        viewModel(factory = QuizViewModelFactory(QuestionRepository(navController.context)))
    val state by vm.uiState.collectAsStateWithLifecycle()

    // ★ 現在の BackStackEntry を Compose の State として購読
    val currentEntry by navController.currentBackStackEntryAsState()

    // ★ “NavBackStackEntry をキー” にして remember し、getBackStackEntry("quiz") を保持
    val quizEntry = remember(currentEntry) {
        // 存在しないケースは通常ありませんが、保険で runCatching
        runCatching { navController.getBackStackEntry("quiz") }.getOrNull()
    }
    val savedStateHandle = quizEntry?.savedStateHandle


    // ★ tick の StateFlow を監視（0L 初期値）
    //    savedStateHandle が null の場合は監視しない
    val tickFlow = remember(savedStateHandle) {
        savedStateHandle?.getStateFlow("reshuffleTick", 0L)
    }

    // Flow が null の可能性があるので、0L をデフォルトに合成
    val tick by (tickFlow?.collectAsState(initial = 0L) ?: remember { mutableStateOf(0L) })

    // ★ tick が変わるたびに reset を実行
    LaunchedEffect(tick) {
        if (tick != 0L && savedStateHandle != null) {
            val reshuffle = savedStateHandle.get<Boolean>("reshuffle")
            if (reshuffle != null) {
                vm.reset(reshuffle = reshuffle)
                // 再実行防止に消しておく（tick は履歴として残ってよい）
                savedStateHandle.remove<Boolean>("reshuffle")
            }
        }
    }

    // 最終到達で結果へ（quiz は残す）
    LaunchedEffect(state.finished) {
        if (state.finished) {
            navController.navigate("result/${state.correctCount}/${state.total}") {
                popUpTo("quiz") { inclusive = false }
            }
            // ★ 遷移直後に消費して finished=false に戻す
            vm.markResultNavigated()
        }
    }

    BackHandler(enabled = !state.isLoading && state.currentIndex > 0) {
        vm.prev()
    }

    val onNavUp: () -> Unit = {
        if (state.currentIndex > 0) vm.prev() else navController.popBackStack()
    }

    QuizScreen(
        state = state,
        onSelect = vm::selectOption,
        onNext = vm::next,
        onPrev = vm::prev,
        onNavUp = onNavUp
    )
}
