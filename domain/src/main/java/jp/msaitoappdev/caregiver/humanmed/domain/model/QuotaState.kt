package jp.msaitoappdev.caregiver.humanmed.domain.model

data class QuotaState(
    val todayKey: String,
    val usedSets: Int,
    val rewardedGranted: Int,
    val freeDailySets: Int
) {
    val totalAllowance: Int get() = freeDailySets + rewardedGranted
    val canStart: Boolean get() = usedSets < totalAllowance
}
