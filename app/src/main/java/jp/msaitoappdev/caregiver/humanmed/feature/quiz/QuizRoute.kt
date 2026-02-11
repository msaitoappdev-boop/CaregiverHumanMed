package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeEffect
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeViewModel

@Composable
fun QuizRoute(navController: NavController) {
    val vm: QuizViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val homeVm: HomeViewModel = hiltViewModel()
    val homeState by homeVm.uiState.collectAsStateWithLifecycle()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val actionTick = savedStateHandle?.get<Long>("action_tick")
    val reshuffleTick = savedStateHandle?.get<Long>("reshuffleTick")

    LaunchedEffect(actionTick, reshuffleTick) {
        val action = savedStateHandle?.get<String>("action")
        val reshuffle = savedStateHandle?.get<Boolean>("reshuffle")
        val isReview = savedStateHandle?.get<Boolean>("is_review")

        vm.processEntryPoint(action, reshuffle, isReview)

        // 一度処理したら、ハンドルからフラグを削除
        savedStateHandle?.remove<String>("action")
        savedStateHandle?.remove<Long>("action_tick")
        savedStateHandle?.remove<Boolean>("reshuffle")
        savedStateHandle?.remove<Long>("reshuffleTick")
        savedStateHandle?.remove<Boolean>("is_review")
    }

    // クイズが終了したら HomeVM 経由で広告表示と結果画面遷移を行う
    LaunchedEffect(state.finished) {
        if (state.finished) {
            val activity = navController.context as? Activity
            if (activity != null) {
                homeVm.onQuizFinished(activity, state.correctCount, state.total, vm.isReviewSession())
            } else {
                navController.navigate(
                    NavRoutes.Result.build(state.correctCount, state.total)
                ) {
                    popUpTo(NavRoutes.QUIZ) { inclusive = false }
                }
                vm.markResultNavigated()
            }
        }
    }

    // HomeVM の Effect を監視し、NavigateToResult を受け取ったら遷移する
    LaunchedEffect(homeVm.effect) {
        homeVm.effect.collect { effect ->
            when (val currentEffect = effect) {
                is HomeEffect.NavigateToResult -> {
                    navController.navigate(
                        NavRoutes.Result.build(currentEffect.score, currentEffect.total)
                    ) {
                        popUpTo(NavRoutes.QUIZ) { inclusive = false }
                    }
                    vm.markResultNavigated()
                }
                else -> Unit
            }
        }
    }

    BackHandler(enabled = !state.isLoading && state.currentIndex > 0) { vm.prev() }
    val onNavUp: () -> Unit = {
        if (state.currentIndex > 0) vm.prev() else navController.popBackStack()
    }

    val goToPaywall: () -> Unit = {
        navController.navigate(NavRoutes.PAYWALL) {
            launchSingleTop = true
        }
    }

    QuizScreen(
        state = state,
        onSelect = vm::selectOption,
        onNext   = vm::next,
        onPrev   = vm::prev,
        onNavUp  = onNavUp,
        canShowFullExplanation = homeState.canShowFullExplanation,
        onUpgrade = goToPaywall
    )
}
