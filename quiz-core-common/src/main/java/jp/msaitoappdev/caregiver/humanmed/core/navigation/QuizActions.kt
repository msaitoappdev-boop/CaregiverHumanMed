package jp.msaitoappdev.caregiver.humanmed.core.navigation

object QuizActions {
    const val KEY_QUIZ_ACTION = "quiz_action"

    /** 新しいクイズセット（未回答優先）を開始する */
    const val ACTION_START_NEW = "start_new"

    /** 現在のクイズセットを、同じ問題・同じ順番でもう一度やり直す */
    const val ACTION_RESTART_SAME_ORDER = "restart_same_order"
}
