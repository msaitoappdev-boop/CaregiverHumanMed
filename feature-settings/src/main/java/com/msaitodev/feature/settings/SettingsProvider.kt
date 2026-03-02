package com.msaitodev.feature.settings

/**
 * 設定画面に必要なアプリケーション固有の情報を供給するプロバイダー。
 * これを実装することで、Settingsモジュールをドメインフリーに保ちます。
 */
interface SettingsProvider {
    /** プライバシーポリシーのURL */
    val privacyPolicyUrl: String

    /** Google Play の定期購入管理画面へのURL（通常は固定だが、将来的な拡張性を考慮） */
    val subscriptionManagementUrl: String
}
