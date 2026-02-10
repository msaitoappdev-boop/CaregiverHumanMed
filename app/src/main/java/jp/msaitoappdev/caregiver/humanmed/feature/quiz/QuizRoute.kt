package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.feature.home.HomeViewModel

/**
 * クイズ実行画面のルート Composable。
 * 課金状態などの外部の状態は [HomeViewModel] から受け取り、UIロジックに集中する。
 */
@Composable
fun QuizRoute(navController: NavController) {
    val TAG = "QuizRoute"

    val vm: QuizViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    // アプリ全体の状態を管理する HomeViewModel から、UI状態を取得
    val homeVm: HomeViewModel = hiltViewModel()
    val homeState by homeVm.uiState.collectAsStateWithLifecycle()

    val backStackEntry = navController.currentBackStackEntry
    LaunchedEffect(Unit) {
        // このエフェクトは Composable が再構成されるたびに実行される
        // savedStateHandle から action フラグを読み込み、処理する
        Log.d(TAG, "LaunchedEffect(Unit) triggered, checking for action flag")
        Log.d(TAG, "currentBackStackEntry exists: ${backStackEntry != null}")
        val action = backStackEntry?.savedStateHandle?.get<String>("action")
        Log.d(TAG, "Retrieved action from savedStateHandle: $action")
        if (action != null) {
            Log.d(TAG, "Processing action: $action")
            when (action) {
                "loadNext" -> {
                    Log.d(TAG, "Executing vm.loadNextSet()")
                    vm.loadNextSet()
                    Log.d(TAG, "vm.loadNextSet() completed")
                }
                "reset_same_order" -> {
                    Log.d(TAG, "Executing vm.reset(false)")
                    vm.reset(false)
                    Log.d(TAG, "vm.reset(false) completed")
                }
                else -> Log.w(TAG, "Unknown action: $action")
            }
            backStackEntry?.savedStateHandle?.remove<String>("action")
            Log.d(TAG, "Removed action flag from savedStateHandle")
        } else {
            Log.d(TAG, "No action flag found, nothing to do")
        }
    }

    // クイズが終了したら HomeVM 経由で広告表示と結果画面遷移を行う
    LaunchedEffect(state.finished) {
        if (state.finished) {
            // 使用する Activity を取得（NavController の currentBackStackEntry が null の場合に備える）
            val activity = navController.context as? Activity
            if (activity != null) {
                homeVm.onQuizFinished(activity, state.correctCount, state.total)
            } else {
                // Activity が取れない場合は従来通り直接遷移する
                navController.navigate(
                    jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes.Result.build(state.correctCount, state.total)
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
            when (effect) {
                is jp.msaitoappdev.caregiver.humanmed.feature.home.HomeEffect.NavigateToResult -> {
                    navController.navigate(
                        jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes.Result.build(effect.score, effect.total)
                    ) {
                        popUpTo(NavRoutes.QUIZ) { inclusive = false }
                    }
                    vm.markResultNavigated()
                }
                else -> Unit
            }
        }
    }

    // Android の「戻る」ボタンの挙動を制御
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
        // 解説を全文表示できるかの状態は、信頼できる唯一の源である HomeVM から受け取る
        canShowFullExplanation = homeState.canShowFullExplanation,
        onUpgrade = goToPaywall
    )
}
