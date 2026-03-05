package com.msaitodev.feature.settings

import kotlinx.coroutines.flow.Flow

/**
 * 設定画面に必要なアプリケーション固有の情報を供給するプロバイダー。
 */
interface SettingsProvider {
    /** プライバシーポリシーのURL */
    val privacyPolicyUrl: String

    /** Google Play の定期購入管理画面へのURL */
    val subscriptionManagementUrl: String

    /** 弱点特訓モードの表示名（Hub から供給） */
    val weaknessModeTitle: String

    /** 弱点特訓モードの説明文（Hub から供給） */
    val weaknessModeDescription: String

    /** 弱点特訓モードが有効かどうか */
    val isWeaknessMode: Flow<Boolean>

    /** 弱点特訓モードのカテゴリー制限（null の場合は全カテゴリー） */
    val weaknessCategoryId: Flow<String?>

    /** 弱点特訓モードのカテゴリー名（null の場合は全カテゴリー） */
    val weaknessCategoryName: Flow<String?>

    /** 弱点特訓モードの有効・無効を更新する */
    suspend fun updateWeaknessMode(enabled: Boolean, categoryId: String? = null)
}
