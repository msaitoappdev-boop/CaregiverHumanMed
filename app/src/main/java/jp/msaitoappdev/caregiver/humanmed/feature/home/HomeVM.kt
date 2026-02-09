package jp.msaitoappdev.caregiver.humanmed.feature.home

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.msaitoappdev.caregiver.humanmed.R
import jp.msaitoappdev.caregiver.humanmed.ads.InterstitialHelper
import jp.msaitoappdev.caregiver.humanmed.core.session.StudyQuotaRepository
import jp.msaitoappdev.caregiver.humanmed.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class HomeVM @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val premiumRepo: PremiumRepository,
    private val interstitialHelper: InterstitialHelper
) : ViewModel() {

    private val TAG = "HomeVM"

    // ---- Remote Config Keys ----
    private companion object {
        const val KEY_FREE_DAILY_SETS = "free_daily_sets"
        const val KEY_PREMIUM_DAILY_SETS = "premium_daily_sets"
        const val KEY_SET_SIZE = "set_size"
        const val KEY_REWARDED_ENABLED = "rewarded_enabled"
        const val KEY_INTERSTITIAL_ENABLED = "interstitial_enabled"
        const val KEY_INTERSTITIAL_CAP_PER_SESSION = "interstitial_cap_per_session"
        const val KEY_INTER_SESSION_INTERVAL_SEC = "inter_session_interval_sec"
    }

    // ---- Remote Config ----
    private val rc: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private val _freeDailySets = MutableStateFlow(1)
    private val _premiumDailySets = MutableStateFlow(10)
    private val _setSize = MutableStateFlow(3)
    private val _rewardedEnabled = MutableStateFlow(true)
    private val _interstitialEnabled = MutableStateFlow(true)

    private val freeDailySetsFlow = _freeDailySets.asStateFlow()
    private val premiumDailySetsFlow = _premiumDailySets.asStateFlow()
    private val setSizeFlow = _setSize.asStateFlow()

    init {
        // defaults を設定（res/xml/remote_config_defaults.xml）
        rc.setDefaultsAsync(R.xml.remote_config_defaults)
        // まずは defaults/キャッシュを即時反映
        readRcIntoState()
        // 非同期 fetch & activate 後に再反映
        rc.fetchAndActivate().addOnCompleteListener { readRcIntoState() }
    }

    private fun readRcIntoState() {
        val free = rc.getLong(KEY_FREE_DAILY_SETS).toInt().coerceAtLeast(0)
        val premium = rc.getLong(KEY_PREMIUM_DAILY_SETS).toInt().coerceAtLeast(1)
        val size = rc.getLong(KEY_SET_SIZE).toInt().coerceAtLeast(1)
        val rewarded = rc.getBoolean(KEY_REWARDED_ENABLED)
        val interstitial = rc.getBoolean(KEY_INTERSTITIAL_ENABLED)

        _freeDailySets.value = free
        _premiumDailySets.value = premium
        _setSize.value = size
        _rewardedEnabled.value = rewarded
        _interstitialEnabled.value = interstitial
    }

    // ---- Premium Status ----
    val isPremium: StateFlow<Boolean> = premiumRepo.isPremium

    // ---- Premium × RC で有効な設定値を算出 ----
    private val effectiveFreeDailySetsFlow: StateFlow<Int> =
        combine(
            freeDailySetsFlow,
            premiumDailySetsFlow,
            isPremium
        ) { free, premium, isPremiumValue ->
            val result = if (isPremiumValue) premium else free
            Log.d("BugHunt-Quota", "effectiveFreeDailySetsFlow updated: isPremium=$isPremiumValue, free=$free, premium=$premium, result=$result")
            result
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    private val effectiveRewardedEnabledFlow: StateFlow<Boolean> =
        combine(
            _rewardedEnabled.asStateFlow(),
            isPremium
        ) { enabled, isPremiumValue ->
            val result = enabled && !isPremiumValue
            Log.d(TAG, "effectiveRewardedEnabledFlow calculated: remoteConfigEnabled=$enabled, isPremium=$isPremiumValue, result=$result")
            result
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val effectiveInterstitialEnabledFlow: StateFlow<Boolean> =
        combine(
            _interstitialEnabled.asStateFlow(),
            isPremium
        ) { enabled, isPremiumValue ->
            val result = enabled && !isPremiumValue
            Log.d(TAG, "effectiveInterstitialEnabledFlow calculated: remoteConfigEnabled=$enabled, isPremium=$isPremiumValue, result=$result")
            result
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ---- Repository 連携（freeDailySets の変化に追従）----

    private val quotaFlow = effectiveFreeDailySetsFlow
        .map { it.coerceAtLeast(0) }
        .flatMapLatest { effectiveFree -> quotaRepo.observe { effectiveFree } }

    // ---- UI State（必要であれば HomeScreen 以外で利用）----
    data class HomeUiState(
        val todayKey: String = "",
        val usedSets: Int = 0,
        val rewardedGranted: Int = 0,
        val freeDailySets: Int = 1,
        val totalAllowance: Int = 1,
        val remainingSets: Int = 1,
        val canStart: Boolean = false,
        val questionsPerSet: Int = 3,
        val rewardedEnabled: Boolean = true,
        val interstitialEnabled: Boolean = true,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    val uiState: StateFlow<HomeUiState> =
        combine(
            quotaFlow,
            setSizeFlow.map { it.coerceAtLeast(1) },
            effectiveRewardedEnabledFlow,
            effectiveInterstitialEnabledFlow
        ) { quota, setSize, rewardedEnabled, interstitialEnabled ->
            val total = quota.totalAllowance
            val remaining = (total - quota.usedSets).coerceAtLeast(0)
            HomeUiState(
                todayKey = quota.todayKey,
                usedSets = quota.usedSets,
                rewardedGranted = quota.rewardedGranted,
                freeDailySets = quota.freeDailySets,
                totalAllowance = total,
                remainingSets = remaining,
                canStart = quota.canStart,
                questionsPerSet = setSize, // RC set_size（デフォルト3）
                rewardedEnabled = rewardedEnabled,
                interstitialEnabled = interstitialEnabled,
                isLoading = false,
                errorMessage = null
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )

    // ---- HomeScreen が期待している公開API ----
    /** 本日付与済みのリワード回数（= StudyQuotaRepository.rewardedGranted） */
    val rewardedCountToday: StateFlow<Int> =
        quotaFlow.map { it.rewardedGranted }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val grantMutex = Mutex()

    /**
     * Rewarded 動画視聴後、+1 セットを当日に付与する。
     * - 1日1回まで（rewardedGranted < 1 のときのみ付여）
     * - RC の rewarded_enabled=false or Premium の場合は付与しない
     *
     * @return true: 付与した / false: 付与しなかった（停止中 or 既に上限 or Premium）
     */
    suspend fun tryGrantDailyPlusOne(): Boolean = grantMutex.withLock {
        val state = uiState.value
        if (!state.rewardedEnabled) return false
        if (state.rewardedGranted >= 1) return false

        return try {
            quotaRepo.grantByReward()
            true
        } catch (t: Throwable) {
            false
        }
    }

    fun onTrainingSetFinished(activity: Activity) {
        viewModelScope.launch {
            Log.d(TAG, "onTrainingSetFinished called. Checking for interstitial.")
            showInterstitialAdIfNeeded(activity) {
                viewModelScope.launch {
                    try {
                        quotaRepo.markSetFinished()
                    } catch (_: Throwable) {
                        _effect.emit(HomeEffect.ShowMessage("進捗の保存に失敗しました"))
                    }
                }
            }
        }
    }

    /**
     * This function is now only called when the user CANNOT start a quiz.
     */
    fun onStartQuizClicked() {
        viewModelScope.launch {
            if (isPremium.value) {
                _effect.emit(HomeEffect.ShowMessage("本日の学習上限に達しました。"))
            } else {
                _effect.emit(HomeEffect.ShowRewardedAdOffer)
            }
        }
    }

    fun onNextSetClicked(activity: Activity, canStart: Boolean) {
        Log.d(TAG, "onNextSetClicked called with canStart: $canStart")
        viewModelScope.launch {
            if (canStart) {
                Log.d(TAG, "canStart is true, showing interstitial ad.")
                showInterstitialAdIfNeeded(activity) {
                    viewModelScope.launch {
                        _effect.emit(HomeEffect.LoadNextQuizSet)
                    }
                }
            } else {
                Log.d(TAG, "canStart is false, checking for premium status.")
                if (isPremium.value) {
                    _effect.emit(HomeEffect.ShowMessage("本日の学習上限に達しました。"))
                } else {
                    _effect.emit(HomeEffect.ShowRewardedAdOffer)
                }
            }
        }
    }

    private fun showInterstitialAdIfNeeded(activity: Activity, onAdClosed: () -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "showInterstitialAdIfNeeded: Checking conditions...")
            val enabled = _interstitialEnabled.value && !isPremium.value
            Log.d(TAG, "showInterstitialAdIfNeeded: isPremium=${isPremium.value}, remoteConfigInterstitialEnabled=${_interstitialEnabled.value}, finalEnabled=$enabled")
            if (!enabled) {
                Log.d(TAG, "showInterstitialAdIfNeeded: Interstitial is disabled for this user. Skipping.")
                onAdClosed()
                return@launch
            }

            val cap = rc.getLong(KEY_INTERSTITIAL_CAP_PER_SESSION).toInt()
            val intervalSec = rc.getLong(KEY_INTER_SESSION_INTERVAL_SEC)
            Log.d(TAG, "showInterstitialAdIfNeeded: Calling helper with cap=$cap, interval=$intervalSec")
            interstitialHelper.tryShow(
                activity = activity,
                enabled = true, // Already checked by this function
                sessionCap = cap,
                minIntervalSec = intervalSec,
                onAdClosed = onAdClosed
            )
        }
    }

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()
}

// ---- One-shot effects（必要なら HomeScreen から collect）----
public sealed interface HomeEffect {
    object NavigateToQuiz : HomeEffect
    object LoadNextQuizSet : HomeEffect
    object ShowRewardedAdOffer : HomeEffect
    data class ShowMessage(val message: String) : HomeEffect
}
