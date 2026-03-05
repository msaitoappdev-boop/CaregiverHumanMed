package com.msaitodev.core.ads

import kotlinx.coroutines.flow.Flow

/**
 * 広告の表示制限（1日の上限、表示間隔）を管理するための共通インターフェース。
 * インタースティシャル、リワード広告のリポジトリはこのコントラクトを遵守する。
 */
interface BaseAdRepository {
    /** 本日の実行（表示または獲得）回数。日付が変わっていれば 0 を返す。 */
    val countDaily: Flow<Int>

    /** 最後に実行したエポック秒（表示間隔の制御に使用）。 */
    val lastShownEpochSec: Flow<Long>

    /** 回数を 1 増やす。日付が変わっていればリセットしてからインクリメントする。 */
    suspend fun incrementCount()

    /** 最後に実行した時刻を現在時刻に更新する。 */
    suspend fun updateLastShownTimestamp()
}
