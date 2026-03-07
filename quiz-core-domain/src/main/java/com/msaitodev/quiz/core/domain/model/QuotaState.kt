package com.msaitodev.quiz.core.domain.model

/**
 * 学習ノルマ（Quota）の状態を保持するモデル。
 * 純粋に学習セットの消化状況のみを管理する。
 */
data class QuotaState(
    val todayKey: String,
    val usedSets: Int,
    val freeDailySets: Int
) {
    /** 広告等の追加特典を除いた、標準的な開始可否判定 */
    val canStart: Boolean get() = usedSets < freeDailySets
}
