package com.msaitodev.quiz.core.domain.config

/**
 * Firebase Remote Config で使用するパラメータキーの定義。
 * 量産時に Firebase Console で設定が必要な項目を一元管理する。
 */
object RemoteConfigKeys {
    /** 無料ユーザーの1日の最大セット数 */
    const val FREE_DAILY_SETS = "free_daily_sets"
    
    /** プレミアムユーザーの1日の最大セット数 */
    const val PREMIUM_DAILY_SETS = "premium_daily_sets"
    
    /** 1セットあたりの問題数 */
    const val SET_SIZE = "set_size"
    
    /** インタースティシャル広告の有効/無効 */
    const val INTERSTITIAL_ENABLED = "interstitial_enabled"
    
    /** 1セッションあたりのインタースティシャル広告の最大表示回数 */
    const val INTERSTITIAL_CAP_PER_SESSION = "interstitial_cap_per_session"
    
    /** インタースティシャル広告表示の最小間隔（秒） */
    const val INTER_SESSION_INTERVAL_SEC = "inter_session_interval_sec"
}
