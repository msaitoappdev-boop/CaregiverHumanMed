package jp.msaitoappdev.caregiver.humanmed.feature.home

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.R
import jp.msaitoappdev.caregiver.humanmed.ads.InterstitialHelper
import jp.msaitoappdev.caregiver.humanmed.core.session.QuotaState
import jp.msaitoappdev.caregiver.humanmed.core.session.StudyQuotaRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// sealed interface をトップレベルに定義し、外部からの可視性を確保
sealed interface HomeEffect {
    object NavigateToQuiz : HomeEffect
    data class NavigateToResult(val score: Int, val total: Int) : HomeEffect
    object LoadNextQuizSet : HomeEffect
    object ShowRewardedAdOffer : HomeEffect
    data class ShowMessage(val message: String) : HomeEffect
    object RewardGrantedAndNavigate : HomeEffect
}

/**
 * ホーム画面、および関連する学習フロー全体のビジネスロジックを管理する ViewModel。
 * この ViewModel は、単一の真実の源 (Single Source of Truth) として機能し、
 * レースコンディションを避けるため、状態の更新とそれに基づく判断を直列的に行う責務を持つ。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val premiumRepo: PremiumRepository,
    private val interstitialHelper: InterstitialHelper
) : ViewModel() {

    private val TAG = "HomeViewModel"
    private val rc: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        rc.setDefaultsAsync(R.xml.remote_config_defaults)
        rc.fetchAndActivate()
    }

    // --- Data Flow: リアクティブなデータストリームの構築 ---

    /** ユーザーがプレミアム会員であるかどうかの状態。 [PremiumRepository] からの真実の源。 */
    private val isPremium: StateFlow<Boolean> = premiumRepo.isPremium

    /**
     * 現在の学習ノルマの状態をDBから監視する Flow。
     * isPremium の状態が変化すると、自動的に正しい上限値で監視を再開する。
     */
    private val quotaFlow: StateFlow<QuotaState?> = isPremium.flatMapLatest { isPremium ->
        val limitKey = if (isPremium) "premium_daily_sets" else "free_daily_sets"
        val limit = rc.getLong(limitKey).toInt()
        Log.d(TAG, "New quota limit observed. isPremium: $isPremium, limit: $limit")
        quotaRepo.observe { limit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Helper to read the latest quota directly from repository (avoids stale stateIn value)
    private suspend fun fetchLatestQuota(): QuotaState? {
        val limitKey = if (isPremium.value) "premium_daily_sets" else "free_daily_sets"
        val limit = rc.getLong(limitKey).toInt()
        return quotaRepo.observe { limit }.first()
    }

    // --- UI State: 画面に表示するための状態 ---

    /**
     * UI が表示すべき、最小限かつ信頼できる状態のみを保持するデータクラス。
     */
    data class HomeUiState(
        val canStart: Boolean = false,
        val isLoading: Boolean = false,
        val canShowFullExplanation: Boolean = false // 課金状態に基づき、解説を全文表示できるか
    )

    /** UI に公開する、現在の画面状態。 */
    val uiState: StateFlow<HomeUiState> = combine(quotaFlow, isPremium) { quota, isPremiumValue ->
        if (quota == null) {
            HomeUiState(isLoading = true)
        } else {
            HomeUiState(
                canStart = quota.canStart,
                canShowFullExplanation = isPremiumValue
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    /** UI への一度きりのイベントを通知するための SharedFlow。 */
    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    // onNextSetClicked の重複実行を防ぐためのフラグ
    private var isNextSetProcessing = false

    // ---- Event Handlers: UIからのアクションを処理する、唯一の窓口 ----

    /**
     * ホーム画面の「クイズを開始」ボタンがクリックされたときに呼び出される。
     * 現在の学習ノルマを **同期的** に評価し、適切なアクションを実行する。
     */
    fun onStartQuizClicked() {
        viewModelScope.launch {
            val currentQuota = quotaFlow.first() ?: return@launch
            Log.d(TAG, "onStartQuizClicked: canStart=${currentQuota.canStart}")
            if (currentQuota.canStart) {
                _effect.emit(HomeEffect.NavigateToQuiz)
            } else {
                handleQuotaExceeded(currentQuota)
            }
        }
    }

    /**
     * クイズが終了したタイミングで呼び出す。View (QuizRoute) はこの関数を呼び、
     * ViewModel は必要に応じてインタースティシャル広告を表示し、
     * 表示完了後に `HomeEffect.NavigateToResult` を発行する。
     */
    fun onQuizFinished(activity: Activity, score: Int, total: Int) {
        viewModelScope.launch {
            Log.d(TAG, "onQuizFinished: Marking set as finished then checking whether to show interstitial")
            // Ensure the completed set is recorded before we decide about ads/rewards
            try {
                quotaRepo.markSetFinished()
            } catch (e: Exception) {
                Log.e(TAG, "onQuizFinished: Failed to mark set finished", e)
                // Continue; we still want to navigate to result even if saving failed
            }

            val interstitialEnabled = rc.getBoolean("interstitial_enabled") && !isPremium.value
            if (!interstitialEnabled) {
                _effect.emit(HomeEffect.NavigateToResult(score, total))
                return@launch
            }
            val cap = rc.getLong("interstitial_cap_per_session").toInt()
            val intervalSec = rc.getLong("inter_session_interval_sec")
            // Await the suspend tryShow so we only navigate after the ad flow completes
            val shown = interstitialHelper.tryShow(activity, true, cap, intervalSec)
            Log.d(TAG, "onQuizFinished: interstitial shown=$shown")
            _effect.emit(HomeEffect.NavigateToResult(score, total))
        }
    }

    /**
     * 結果画面の「次の3問へ」ボタンがクリックされたときに呼び出される。
     * 進捗はクイズ完了時に記録済みなので、ここでは最新のクオータを参照して判断するのみ。
     * 重複実行を防ぐため、isNextSetProcessing フラグで多重呼び出しを排除する。
     */
    fun onNextSetClicked(activity: Activity) {
        Log.d(TAG, "onNextSetClicked: called, isNextSetProcessing=$isNextSetProcessing")
        // 既に処理中であれば、重複呼び出しを無視する
        if (isNextSetProcessing) {
            Log.d(TAG, "onNextSetClicked: Already processing, ignoring duplicate call")
            return
        }
        isNextSetProcessing = true
        Log.d(TAG, "onNextSetClicked: Set isNextSetProcessing=true")

        viewModelScope.launch {
            try {
                Log.d(TAG, "onNextSetClicked: Inside viewModelScope.launch, checking quota to load next set")

                val newQuota = fetchLatestQuota()
                Log.d(TAG, "onNextSetClicked: fetchLatestQuota returned: $newQuota")
                if (newQuota?.canStart == true) {
                    Log.d(TAG, "onNextSetClicked: canStart=true, emitting LoadNextQuizSet effect")
                    // Optional: show interstitial here as well if desired, but primary interstitial is shown at quiz finish
                    _effect.emit(HomeEffect.LoadNextQuizSet)
                } else {
                    Log.w(TAG, "onNextSetClicked: canStart=false, handling quota exceeded")
                    if (newQuota != null) {
                        handleQuotaExceeded(newQuota)
                    }
                }
            } finally {
                // 処理終了後、フラグをリセット
                isNextSetProcessing = false
                Log.d(TAG, "onNextSetClicked: Set isNextSetProcessing=false (in finally)")
            }
        }
    }

    /**
     * リワード広告視聴による学習回数付与を試みる。
     * 状態のチェックは、UIの古い状態に依存せず、この関数内でDBの最新状態を直接取得して行う。
     * @return true: 付与に成功 / false: 付与に失敗
     */
    suspend fun tryGrantDailyPlusOne(): Boolean {
        return try {
            Log.d(TAG, "tryGrantDailyPlusOne: Starting")
            val currentQuota = fetchLatestQuota()
            Log.d(TAG, "tryGrantDailyPlusOne: fetchLatestQuota returned: $currentQuota")
            if (currentQuota == null) {
                Log.w(TAG, "tryGrantDailyPlusOne: currentQuota is null, returning false")
                return false
            }
            Log.d(TAG, "tryGrantDailyPlusOne: isPremium=${isPremium.value}, rewardedGranted=${currentQuota.rewardedGranted}")
            if (isPremium.value || currentQuota.rewardedGranted >= 1) {
                Log.w(TAG, "tryGrantDailyPlusOne: Granting denied (premium or already granted).")
                return false
            }
            Log.d(TAG, "tryGrantDailyPlusOne: Granting reward.")
            quotaRepo.grantByReward()
            Log.d(TAG, "tryGrantDailyPlusOne: quotaRepo.grantByReward() completed successfully")
            // grant succeeded — return true; caller (UI) is responsible for navigation
            true
        } catch (t: Throwable) {
            Log.e(TAG, "tryGrantDailyPlusOne: Failed to grant reward.", t)
            false
        }
    }

    /**
     * 学習ノルマ上限に到達した際の共通処理。
     * @param quota 判断の基準となる最新の学習ノルマ状態。
     */
    private suspend fun handleQuotaExceeded(quota: QuotaState) {
        Log.d(TAG, "handleQuotaExceeded: isPremium=${isPremium.value}, rewardedGranted=${quota.rewardedGranted}")
        if (isPremium.value) {
            _effect.emit(HomeEffect.ShowMessage("本日の学習上限に達しました。"))
        } else {
            if (quota.rewardedGranted < 1) {
                _effect.emit(HomeEffect.ShowRewardedAdOffer)
            } else {
                _effect.emit(HomeEffect.ShowMessage("本日は動画視聴による付与は上限です。"))
            }
        }
    }

    /**
     * インタースティシャル広告の表示を、条件付きで試みる。
     */
    private fun showInterstitialAdIfNeeded(activity: Activity, onAdClosed: () -> Unit) {
        viewModelScope.launch {
            val interstitialEnabled = rc.getBoolean("interstitial_enabled") && !isPremium.value
            Log.d(TAG, "showInterstitialAdIfNeeded: interstitialEnabled=$interstitialEnabled")
            if (!interstitialEnabled) {
                onAdClosed()
                return@launch
            }
            val cap = rc.getLong("interstitial_cap_per_session").toInt()
            val intervalSec = rc.getLong("inter_session_interval_sec")
            // Use the new suspend tryShow and wait for its result
            val shown = interstitialHelper.tryShow(activity, true, cap, intervalSec)
            Log.d(TAG, "showInterstitialAdIfNeeded: tryShow returned: $shown")
            // Either ad was shown or not—we proceed by invoking the callback to continue flow
            onAdClosed()
        }
    }

    /**
     * リワード広告視聴完了時に UI から呼び出される。
     * ViewModel 側で報酬付与を行い、完了後に NavigateToResult Effect を発行する。
     * これにより、Compose スコープに依存せず、確実に処理が実行される。
     */
    fun onRewardedAdEarned() {
        Log.d(TAG, "onRewardedAdEarned: Starting reward grant from ViewModel")
        viewModelScope.launch {
            try {
                Log.d(TAG, "onRewardedAdEarned: Inside viewModelScope.launch")
                val ok = tryGrantDailyPlusOne()
                Log.d(TAG, "onRewardedAdEarned: tryGrantDailyPlusOne returned: $ok")
                if (ok) {
                    Log.d(TAG, "onRewardedAdEarned: Reward granted successfully, emitting RewardGrantedAndNavigate")
                    _effect.emit(HomeEffect.RewardGrantedAndNavigate)
                } else {
                    Log.w(TAG, "onRewardedAdEarned: Failed to grant reward")
                }
            } catch (e: Exception) {
                Log.e(TAG, "onRewardedAdEarned: Exception occurred", e)
            }
        }
    }
}
