package com.msaitodev.quiz.feature.main.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.msaitodev.quiz.core.common.navigation.QuizActions

@Composable
fun QuizRoute(
    navController: NavController,
    onQuizFinished: (result: QuizResult) -> Unit,
    onUpgrade: () -> Unit
) {
    val vm: QuizViewModel = hiltViewModel()
    vm.init(onQuizFinished)

    val state by vm.uiState.collectAsStateWithLifecycle()

    // Result画面からのアクションを処理する
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            ?.getLiveData<String>(QuizActions.KEY_QUIZ_ACTION)?.observe(navController.currentBackStackEntry!!) {
                vm.processAction(it)
                savedStateHandle.remove<String>(QuizActions.KEY_QUIZ_ACTION)
            }
    }

    BackHandler(enabled = !state.isLoading && state.currentIndex > 0) {
        vm.prev()
    }

    QuizScreen(
        state = state,
        onSelect = vm::selectOption,
        onNext = vm::next,
        onPrev = vm::prev,
        onNavUp = { navController.popBackStack() },
        canShowFullExplanation = state.canShowFullExplanation,
        onUpgrade = onUpgrade
    )
}
