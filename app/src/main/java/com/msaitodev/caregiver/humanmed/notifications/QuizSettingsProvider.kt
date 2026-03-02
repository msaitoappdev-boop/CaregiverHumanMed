package com.msaitodev.caregiver.humanmed.notifications

import android.content.Context
import com.msaitodev.caregiver.humanmed.R
import com.msaitodev.feature.settings.SettingsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * クイズアプリ向けの設定画面ポリシー実装。
 * 文字列リソースは Hub (:app) の strings.xml から取得します。
 */
@Singleton
class QuizSettingsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsProvider {
    override val privacyPolicyUrl: String = context.getString(R.string.privacy_policy_url)
    override val subscriptionManagementUrl: String = context.getString(R.string.subscription_management_url)
}
