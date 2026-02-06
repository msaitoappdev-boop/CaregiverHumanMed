package jp.msaitoappdev.caregiver.humanmed.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.msaitoappdev.caregiver.humanmed.R
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class HomeVM @Inject constructor(
    private val quotaRepo: StudyQuotaRepository,
    private val premiumRepo: PremiumRepository
) : ViewModel() {

    // ---- Remote Config Keys ----
    private companion object {
        const val KEY_FREE_DAILY_SETS = "free_daily_sets"
        const val KEY_SET_SIZE = "set_size"
        const val KEY_REWARDED_ENABLED = "rewarded_enabled"
        const val KEY_INTERSTITIAL_ENABLED = "interstitial_enabled"
        const val KEY_PREMIUM_DAILY_BONUS = "premium_daily_bonus"
    }

    // ---- Remote Config ----
    private val rc: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private val _freeDailySets = MutableStateFlow(1)
    private val _setSize = MutableStateFlow(3)
    private val _rewardedEnabled = MutableStateFlow(true)
    private val _interstitialEnabled = MutableStateFlow(true)
    private val _premiumDailyBonus = MutableStateFlow(0)

    private val freeDailySetsFlow = _freeDailySets.asStateFlow()
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
        val size = rc.getLong(KEY_SET_SIZE).toInt().coerceAtLeast(1) // 「3問/セット」にも対応
        val rewarded = rc.getBoolean(KEY_REWARDED_ENABLED)
        val interstitial = rc.getBoolean(KEY_INTERSTITIAL_ENABLED)
        val bonus = rc.getLong(KEY_PREMIUM_DAILY_BONUS).toInt().coerceAtLeast(0)

        _freeDailySets.value = free
        _setSize.value = size
        _rewardedEnabled.value = rewarded
        _interstitialEnabled.value = interstitial
        _premiumDailyBonus.value = bonus
    }

    // ---- Premium × RC で有効な設定値を算出 ----

    private val effectiveFreeDailySetsFlow: StateFlow<Int> =
        combine(
            freeDailySetsFlow,
            _premiumDailyBonus.asStateFlow(),
            premiumRepo.isPremiumFlow
        ) { free, bonus, isPremium ->
            if (isPremium) free + bonus else free
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    private val effectiveRewardedEnabledFlow: StateFlow<Boolean> =
        combine(
            _rewardedEnabled.asStateFlow(),
            premiumRepo.isPremiumFlow
        ) { enabled, isPremium ->
            enabled && !isPremium // Premium なら false
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val effectiveInterstitialEnabledFlow: StateFlow<Boolean> =
        combine(
            _interstitialEnabled.asStateFlow(),
            premiumRepo.isPremiumFlow
        ) { enabled, isPremium ->
            enabled && !isPremium // Premium なら false
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

    /** 本日、問題セットを開始できるか（枠が残っているか） */
    val canStartFlow: StateFlow<Boolean> =
        quotaFlow.map { it.canStart }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val grantMutex = Mutex()

    /**
     * Rewarded 動画視聴後、+1 セットを当日に付与する。
     * - 1日1回まで（rewardedGranted < 1 のときのみ付与）
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

    // Quiz 側から呼ぶ想定（set_size 問を解き終えたタイミングで）
    fun onTrainingSetFinished() {
        viewModelScope.launch {
            try {
                quotaRepo.markSetFinished()
            } catch (_: Throwable) {
                _effect.emit(HomeEffect.ShowMessage("進捗の保存に失敗しました"))
            }
        }
    }

    // ---- One-shot effects（必要なら HomeScreen から collect）----
    sealed interface HomeEffect {
        data class NavigateToWeakTraining(val questionsPerSet: Int) : HomeEffect
        object ShowQuotaReached : HomeEffect
        data class ShowMessage(val message: String) : HomeEffect
    }
    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()
}