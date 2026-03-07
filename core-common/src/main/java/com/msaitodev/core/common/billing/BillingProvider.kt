package com.msaitodev.core.common.billing

/**
 * アプリ固有の課金設定を提供するためのインターフェース。
 * 各アプリ（Hub）はこのインターフェースを実装し、DI経由で提供する。
 */
interface BillingProvider {
    val productIdPremium: String
    val basePlanId: String
    
    /**
     * ペイウォール画面に表示する設定情報。
     */
    val paywallConfig: PaywallConfig

    /**
     * BillingManagerで使用するエラーメッセージ群。
     * Coreモジュールが特定のドメイン言語に依存しないよう、外部から注入する。
     */
    val errorOfferNotFound: String
    val errorAcknowledgeFailed: String
    val errorPending: String
    val errorGeneral: String
}
