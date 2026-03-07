package com.msaitodev.core.common.navigation

/**
 * 画面遷移時に渡すアクションを定義する定数クラス。
 */
object AppActions {
    /** 遷移アクションを指定するためのキー */
    const val KEY_ACTION = "app_action"

    /** 新しいセッション（未回答優先）を開始する */
    const val ACTION_START_NEW = "start_new"

    /** 現在のセッションを、同じ内容・同じ順番でもう一度やり直す */
    const val ACTION_RESTART_SAME_ORDER = "restart_same_order"
}
