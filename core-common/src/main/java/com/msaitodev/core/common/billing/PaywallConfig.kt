package com.msaitodev.core.common.billing

/**
 * ペイウォール画面に表示するテキスト群を定義するデータクラス。
 * アプリごとに異なる訴求内容を表示するために使用する。
 */
data class PaywallConfig(
    val title: String,
    val headline: String,
    val benefits: List<String>,
    val planTitle: String,
    val planPrice: String,
    val purchaseButtonText: String,
    val purchasedButtonText: String,
    val description: String
)
