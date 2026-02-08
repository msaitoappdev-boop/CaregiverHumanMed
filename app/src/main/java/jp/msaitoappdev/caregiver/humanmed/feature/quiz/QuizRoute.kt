package jp.msaitoappdev.caregiver.humanmed.feature.quiz

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PremiumViewModel

@Composable
fun QuizRoute(navController: NavController) {
    val TAG = "QuizRoute"

    val vm: QuizViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val pvm: PremiumViewModel = hiltViewModel()
    val isPremium by pvm.isPremium.collectAsStateWithLifecycle(initialValue = false)

    val backStackEntry = navController.currentBackStackEntry
    LaunchedEffect(backStackEntry) {
        val action = backStackEntry?.savedStateHandle?.get<String>("action")
        if (action != null) {
            Log.d(TAG, "Received action: $action")
            when (action) {
                "loadNext" -> vm.loadNextSet()
                "reset_same_order" -> vm.reset(false)
            }
            backStackEntry.savedStateHandle.remove<String>("action")
        }
    }

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
        canShowFullExplanation = isPremium,
        onUpgrade = goToPaywall
    )
}
